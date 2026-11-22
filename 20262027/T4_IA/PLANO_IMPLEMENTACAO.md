# Problem 3 — MCTS: plano do que falta

Complemento do [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md): o guia explica a matéria e as decisões; este ficheiro lista **o que falta fazer**, por ordem de dependência, com o critério de «pronto» de cada passo.

O código em `src/` é do grupo. Este plano diz **o quê** e **onde**, e a secção 12 dá o **pseudocódigo** de tudo o que falta implementar (não é código Java pronto a colar).

---

## 0. Ponto de situação

| Peça | Estado | Nota |
|---|---|---|
| `Ilayout.java`, `XAgent.java` | do professor | não mexer |
| `MCTS.java` | **feito** | 47 testes a passar (`test/MCTSTests.java`) |
| `Board.java` | **feito** (teste de vencedor + `children()`) | verificado contra as 255 168 partidas possíveis |
| `MCTSAgent.java` + ligação à `Console` | **feito** | estático `play(Ilayout)` para a `Console`, e `choose(Ilayout)` com parâmetros explícitos para testes/experiências |
| `test/BoardTests.java` | **feito** | 27 testes |
| `test/AgentIntegrationTests.java` | **feito** | 16 testes (`Board` real + agente) |
| Experiências (`test/Experiments.java`, `run_experiments.sh`) | **feito** | resultados em `results/` (CSV + `RESUMO.md`) |
| Javadoc | **feito** (`docs/`) | `src/` do grupo sem avisos; os avisos que restam são do esqueleto do professor |
| UML | **feito** (`UML.md`) | Mermaid + PlantUML; falta exportar a imagem para o relatório |
| Relatório e zip | **falta** | item i (grupo e elementos), texto, número do grupo |
| Preparação da defesa | **falta** | |

**Nota sobre os nomes:** nos pseudocódigos da secção 12 o agente tinha `play` de instância; em Java um método estático e um de instância não podem ter a mesma assinatura, por isso ficou `MCTSAgent.play(Ilayout)` (estático, para a `Console`) e `MCTSAgent.choose(Ilayout)` (instância).

**Prazo:** o prazo original era 18 Nov 2026, mas foi **adiado para dia 23 Nov 2026**. Hoje é 20 Nov: tens hoje e mais 3 dias (21, 22 e 23), por isso convém seguir a ordem do plano e deixar o relatório e o zip para os últimos dois dias. Aproveitar para fazer ao docente as perguntas da secção 15 do guia (nº de sucessores expandidos por iteração; simulação da raiz).

---

## 1. Dependências

```
Board (1) ──► testes do Board (2) ──► agente + Console (3) ──► integração (4) ──► experiências (5) ──┐
                                                                                                     ├──► relatório (7) ──► defesa (8)
                                                                  Javadoc + UML (6) ────────────────┘
```

Só o passo 1 bloqueia os outros. O 6 (Javadoc e UML) pode avançar em paralelo a partir do passo 3.

---

## 2. Passo 1 — `Board.java` (bloqueia tudo)

### 1a. Teste de vencedor

Local: o comentário `/** YOUR CODE HERE */` em `move(int, int)`.

- Depois de colocar a peça em `(x, y)`, basta verificar as linhas que passam por essa casa: **linha, coluna e as duas diagonais**.
- Usar `K` para o comprimento do alinhamento e `N`/`M` para os limites, em vez de um 3 fixo.
- Se houver vitória: `winner = playersTurn` e `gameOver = true`.
- **Tem de ficar depois do bloco do empate.** O esqueleto marca empate quando `moveCount == N*M` *antes* deste teste; a jogada que enche o tabuleiro e faz linha tem de ficar como vitória.
- A troca de `playersTurn` mantém-se onde está.

### 1b. `children()`

- Percorrer `getAvailableMoves()` **por ordem crescente** (`HashSet` não garante ordem; sem ordenar, a semente deixa de dar resultados repetíveis).
- Por casa livre: `Board b = (Board) clone(); b.move(casa);` e juntar à lista.
- Estado terminal → lista vazia.
- O tabuleiro original **nunca** é alterado.

### Pronto quando

- `javac src/*.java` compila sem erros (JDK 25).
- Uma partida na `Console` termina corretamente em vitória e em empate.

---

## 3. Passo 2 — testes do `Board`

Ficheiro novo: `test/BoardTests.java`. Lista (secção 10 do guia, «Board / regras»):

- [ ] Os 8 alinhamentos dão vitória a X e a O.
- [ ] Empate (tabuleiro cheio sem linha).
- [ ] A jogada que enche o tabuleiro e faz linha é **vitória**, não empate.
- [ ] Jogar numa casa ocupada devolve `false` e **não altera nada** (nem `moveCount`, nem a vez, nem as casas livres).
- [ ] Jogar depois do fim lança `IllegalStateException`.
- [ ] `getWinner()` antes do fim lança `IllegalStateException`.
- [ ] `children()`: uma cópia por casa livre, por ordem crescente; o original não muda; terminal não tem filhos.
- [ ] Os filhos são independentes (alterar um não afeta os outros).
- [ ] Os jogadores alternam; depois de uma jogada vencedora o estado é terminal.

Em `run_tests.sh`, acrescentar `src/Board.java` à lista de ficheiros compilados.

**Pronto quando:** `./run_tests.sh fast` corre `MCTSTests` + `BoardTests` sem falhas.

---

## 4. Passo 3 — agente e `Console`

- **Classe nova** (por exemplo `MCTSAgent`) com `static int play(Ilayout board)`, mesma assinatura do `XAgent`. Cria o `MCTS` (`iterations`, `c`, `seed`) e devolve `chooseMove(board)`.
- **Não alterar o corpo de `XAgent.play`**: é o adversário aleatório das experiências.
- Na `Console`, trocar só a chamada do jogador que deve ser o MCTS (`XAgent.play(board)` → `MCTSAgent.play(board)`).
- Parâmetros por omissão sugeridos: `c = 0.2` (Fig. 2) ou mais alto, ver secção 7 abaixo; iterações entre 1 000 e 10 000.
- Para a demonstração o guia sugere orçamento por **tempo** (100 a 500 ms); nos testes usar sempre **iterações**. É opcional e fica como variante pequena no `MCTS`.

**Pronto quando:** na `Console`, humano contra MCTS joga uma partida completa e o MCTS nunca joga numa casa ocupada.

---

## 5. Passo 4 — testes de integração

Ficheiro novo: `test/AgentIntegrationTests.java`, com o `Board` **real** e o agente.

- [ ] Vitória em 1 jogada e bloqueio, em posições montadas jogando no `Board`.
- [ ] A jogada é legal (0 a 8, casa livre) e o `Board` recebido **não foi alterado**.
- [ ] Mesma semente + mesmas iterações → mesma jogada.
- [ ] Uma só casa livre → devolve essa casa.
- [ ] MCTS contra aleatório, **como X e como O**: não perde (ou quase nunca).
- [ ] MCTS contra MCTS (`c = 1.0`): empate.
- [ ] Opcional: MCTS contra minimax não perde.

Os testes do `MCTSTests` usam um tabuleiro de referência próprio. Estes novos confirmam que MCTS e `Board` real combinam, incluindo a ordem de `children()`.

---

## 6. Passo 5 — experiências (item viii)

Ficheiro novo: `test/Experiments.java` com `main`, a usar o `Board` real e o minimax que já existe no `MCTSTests` (o minimax é um **oráculo de teste**, não faz parte da solução).

| Eixo | Valores |
|---|---|
| Iterações | 100, 1 000, 10 000 |
| `c` | 0.1, 0.2, 0.5, √2 |
| Adversário | aleatório (`XAgent`); minimax; MCTS contra MCTS |
| Papel do agente | como X **e** como O |

- Cerca de **200 partidas por combinação**, metade como X e metade como O (menos nas de 10 000 iterações, se for lento).
- Registar: vitórias / empates / derrotas, **tempo médio por jogada**, semente, **simulações totais** (≈ `n` da raiz).
- Gravar num CSV em `results/`.
- Definir no relatório o que se conta como «iteração»: com «expandir todos», uma iteração faz até `b` *rollouts*. Por isso comparar também em simulações totais.
- Fixar sementes para os números serem reproduzíveis.

### O que já se sabe (para confirmar com estes números)

Medido em 150 posições aleatórias, 3 000 iterações:

| `c` | 0.1 | 0.2 | 0.5 | 1.0 | √2 |
|---|---|---|---|---|---|
| Jogadas ótimas | 144/150 | 149/150 | 150/150 | 150/150 | 150/150 |

Com `c` baixo a pesquisa explora pouco e pode fixar-se numa jogada má, mesmo com muitas iterações. Material direto para a discussão do efeito de `c`.

### Tabelas e figuras para o relatório

- [ ] Resultado (V/E/D) por `c` × iterações, contra aleatório.
- [ ] Resultado contra minimax (mostra que não perde com `c` adequado).
- [ ] Taxa de jogadas ótimas por `c`.
- [ ] Tempo por jogada vs iterações.
- [ ] Gráfico: qualidade vs iterações para cada `c`.

---

## 7. Passo 6 — Javadoc e UML

### Javadoc (item vi)

- Em **inglês** (o esqueleto está em inglês); relatório em português.
- Todas as classes, construtores e métodos, também privados (`-private`).
- Primeira frase curta, um `@param` por parâmetro, `@return` só se não for `void`, `@throws` quando o contrato lança exceção.
- Os comentários que já existem no `MCTS.java` mantêm-se; o Javadoc acrescenta-se à volta.
- Comando (pasta `T4_IA`, JDK 25): `javadoc -private -d docs src/*.java`
- Verificar que não há avisos de «unknown tag».

### UML (item vii)

Classes: `Ilayout` (+ enum `ID`), `Board`, `MCTS`, `Node` (aninhada em `MCTS`), `MCTSAgent`, `XAgent`, `Console`.
Relações: `Board` implementa `Ilayout`; `MCTS` depende de `Ilayout`; `MCTS` contém `Node`; `Node` tem `parent`/`children`; `MCTSAgent` usa `MCTS`; `Console` usa `Board` e os agentes.
Rascunho na secção 9 do guia.

---

## 8. Passo 7 — relatório e zip

Seguir a ordem i a x do enunciado (checklist na secção 11 do guia).

| # | Conteúdo | Onde buscar |
|---|---|---|
| i | Id do problema (3), nº do grupo, elementos | |
| ii | Tic-Tac-Toe, MCTS (4 fases), UCB/LCB, tree policy de dois agentes | secções 3 a 7 do guia |
| iii | Testes e o que cada um prova | `test/*.java` |
| iv | Decisões de desenho | tabela abaixo |
| v | Código | `src/` |
| vi | Javadoc | `docs/` |
| vii | UML | passo 6 |
| viii | Resultados, análise, discussão | passo 5 |
| ix | Conclusões | |
| x | Referências + **ferramentas de IA usadas** | declarar |

### Decisões a registar em (iv)

- MAX = quem joga na raiz; `maxToPlay` derivado de `getTurn()`.
- `c` e o seu efeito.
- **Expandir todos** os sucessores da folha e simular cada um; alternativa de um filho por iteração e porquê não.
- A raiz recebe uma simulação própria (`n(raiz) = 1 + Σ`).
- Critério de paragem: nº de iterações (e tempo, se implementado).
- Jogada devolvida: filho da raiz **mais visitado**; desempate por maior `w/n`, depois o primeiro.
- Nó terminal na seleção: não se expande, retropropaga-se o resultado real.
- Recompensa sempre do ponto de vista de MAX (1 / 0,5 / 0).
- Jogada de um filho obtida por diferença de `getAvailableMoves()`; `Ilayout` não foi estendido.
- Só uma casa livre → devolve-a sem pesquisar.

### Zip

- Relatório, `src/`, `test/`, `docs/`, UML, `lib/` se os testes precisarem.
- Excluir `.classpath`, `.project`, `bin/`, `out/`, `.vscode/`.
- Confirmar que o projeto compila **a partir do zero** com `javac` (JDK 25).
- Submeter em <http://deei-mooshak.ualg.pt/~jvo/IA/Entregas/> como *assignment 3*, **até dia 23 Nov**.

---

## 9. Passo 8 — defesa

Falhar as perguntas de implementação ou de alternativas invalida o trabalho. Preparar a secção 13 do guia. As mais prováveis:

1. As 4 fases e onde estão no código.
2. O que faz cada termo de `w/n + c√(ln t / n)` e o efeito de `c`.
3. Porque o MIN minimiza `w/n − c√…` (e porque minimizar o UCB inteiro está errado).
4. Porque se devolve o mais visitado e não o de maior `w/n`.
5. Quem é MAX e o que acontece quando o agente joga com O.
6. Como se trata o nó terminal; porque o argumento não é alterado.
7. Porque expandir todos e não um.
8. Onde está o padrão do Lab 1 (`Ilayout` + classe do algoritmo).

Cada membro do grupo tem de conseguir explicar **cada linha** do `MCTS.java` e do `Board.java` sem ajuda.

---

## 10. Checklist final

- [x] `Board`: vencedor + `children()` implementados
- [x] `BoardTests` a passar
- [x] `MCTSAgent` ligado à `Console`; `XAgent` intacto
- [x] `AgentIntegrationTests` a passar
- [x] `./run_tests.sh` completo sem falhas (JDK 25): 90 testes
- [x] Experiências corridas, CSV guardado, sementes registadas (`results/`)
- [x] Javadoc gerado (`docs/`), sem erros
- [x] UML feito (`UML.md`); falta exportar a imagem
- [ ] Relatório i a x completo, com IA declarada
- [ ] Zip limpo, a compilar do zero
- [ ] Submetido até dia 23 Nov
- [ ] Confirmado com o docente: nº de sucessores expandidos, simulação da raiz
- [ ] Defesa preparada

---

## 11. Armadilhas desta fase

- Teste de vencedor antes do bloco do empate no `Board` (a última jogada ganhadora fica como empate).
- `children()` sem ordenar → resultados não repetíveis.
- Substituir o corpo de `XAgent.play` pelo MCTS → fica sem adversário aleatório.
- Experiências sem sementes fixas → números que ninguém reproduz na defesa.
- Esquecer de contar simulações totais → comparação injusta entre orçamentos.
- Entregar com ficheiros de IDE (`bin/`, `.classpath`) dentro do zip.
- Declarar `c = 0.2` (o valor da Fig. 2) como «o melhor» sem o medir: nos testes deu 149/150 jogadas ótimas e, numa posição, fixou-se numa jogada má mesmo com 100 000 iterações. `c ≥ 0,5` deu 150/150.

---

## 12. Pseudocódigo do que falta implementar

Convenções: `//` comenta; `board[y][x]` com `y` = linha e `x` = coluna; casa = `y*M + x` (com `N = M = 3` é igual a `y*N + x`, como no esqueleto). Os nomes batem com o esqueleto e com o `MCTS.java`.

| Pseudocódigo | Corresponde ao passo |
|---|---|
| P1 `Board.move` + teste de vencedor | 1a |
| P2 `Board.children` | 1b |
| P3 `BoardTests` | 2 |
| P4 `MCTSAgent` + `Console` | 3 |
| P5 `AgentIntegrationTests` | 4 |
| P6 `Experiments` (inclui o minimax oracle) | 5 |
| P7 Javadoc, UML e zip | 6 e 7 |

### P1. `Board.move` e teste de vencedor (passo 1a)

```text
move(x, y):
    se gameOver: lançar IllegalStateException
    se board[y][x] != Blank: devolver false          // não altera nada
    board[y][x] = playersTurn
    moveCount++
    movesAvailable.remove(y*N + x)

    se moveCount == N*M:                              // do esqueleto: empate provisório
        winner = Blank
        gameOver = true

    se haLinha(x, y):                                 // TEM de vir depois do empate:
        winner = playersTurn                          // a última jogada pode fazer linha
        gameOver = true

    playersTurn = (playersTurn == X) ? O : X          // do esqueleto: troca sempre
    devolver true

haLinha(x, y):                                        // só olha para a casa que acabou de ser jogada
    p = board[y][x]
    para cada (dx, dy) em [(1,0), (0,1), (1,1), (1,-1)]:   // linha, coluna, as duas diagonais
        total = 1 + contar(x, y, dx, dy, p) + contar(x, y, -dx, -dy, p)
        se total >= K: devolver true
    devolver false

contar(x, y, dx, dy, p):                              // peças consecutivas de p numa direção
    n = 0
    cx = x + dx ; cy = y + dy
    enquanto 0 <= cx < M e 0 <= cy < N e board[cy][cx] == p:
        n++
        cx += dx ; cy += dy
    devolver n
```

Notas:
- Cada direção é contada para os dois lados, por isso uma jogada no **meio** de uma linha também é detetada.
- Depois de uma vitória `playersTurn` também troca (é o que o esqueleto faz). `getTurn()` num estado terminal não deve ser usado para decidir nada.
- `K` em vez de 3 mantém o código geral.

### P2. `Board.children` (passo 1b)

```text
children():
    lista = []
    se gameOver: devolver lista                       // terminal não tem filhos
    para cada casa em ordenar(movesAvailable):        // ordem crescente, determinística
        filho = (Board) clone()                       // cópia independente
        filho.move(casa)
        lista.adicionar(filho)
    devolver lista
```

Notas:
- `ordenar` = copiar para uma lista e ordenar; `movesAvailable` é um `HashSet`.
- `clone()` devolve `Object`, daí o cast.
- Nunca chamar `move` sobre `this`.

### P3. `BoardTests` (passo 2)

Auxiliares:

```text
tabuleiroCom(casas...):                               // joga as casas por ordem num Board novo
    b = new Board()
    para cada casa em casas: b.move(casa)
    devolver b

LINHAS = [ [0,1,2], [3,4,5], [6,7,8],                 // linhas
           [0,3,6], [1,4,7], [2,5,8],                 // colunas
           [0,4,8], [2,4,6] ]                         // diagonais
```

Casos:

```text
testeVitoriaEmCadaLinha:
    para cada linha em LINHAS:
        // X ganha: X joga a linha, O joga duas casas fora dela (duas casas nunca fazem linha de 3)
        outras = casas fora da linha
        b = tabuleiroCom(linha[0], outras[0], linha[1], outras[1], linha[2])
        afirmar b.isGameOver() e b.getWinner() == X

        // O ganha: X joga 3 casas fora da linha SEM formarem linha, O joga a linha
        foras = 3 casas fora da linha que não formem linha (testar combinações)
        b = tabuleiroCom(foras[0], linha[0], foras[1], linha[1], foras[2], linha[2])
        afirmar b.isGameOver() e b.getWinner() == O

testeEmpate:
    // X O X / X O O / O X X
    b = tabuleiroCom(0,1,2,4,3,5,7,6,8)
    afirmar b.isGameOver() e b.getWinner() == Blank

testeUltimaJogadaFazLinha:                            // vitória, não empate
    // X O X / O X O / O X X: X fecha a diagonal 0-4-8 na 9.ª jogada
    b = tabuleiroCom(0,1,2,3,7,5,8,6,4)
    afirmar b.isGameOver() e b.getWinner() == X

testeCasaOcupada:
    b = tabuleiroCom(4)
    antes = b.clone()
    afirmar b.move(4) == false
    afirmar b.equals(antes) e b.getTurn() == O e b.getAvailableMoves().size() == 8

testeJogarDepoisDoFim:
    b = tabuleiroCom(0,3,1,4,2)                       // X ganha
    afirmar que b.move(5) lança IllegalStateException

testeGetWinnerAntesDoFim:
    afirmar que new Board().getWinner() lança IllegalStateException

testeAlternancia:
    b = new Board() ; afirmar b.getTurn() == X
    b.move(0) ; afirmar b.getTurn() == O
    b.move(1) ; afirmar b.getTurn() == X

testeChildren:
    b = tabuleiroCom(4, 0)
    filhos = b.children()
    afirmar filhos.size() == 7
    afirmar que as casas jogadas (diferença de getAvailableMoves) são crescentes
    afirmar que b não mudou (equals a uma cópia feita antes)
    afirmar que filhos[i].getTurn() == b.getTurn() trocado

testeFilhosIndependentes:
    filhos = new Board().children()
    filhos[0].move(8)                                  // alterar um filho
    afirmar que filhos[1] e o original não mudaram

testeChildrenDeTerminal:
    afirmar tabuleiroCom(0,3,1,4,2).children().isEmpty()
```

### P4. `MCTSAgent` e `Console` (passo 3)

```text
classe MCTSAgent:
    constantes por omissão: ITERATIONS = 2000 ; C = 0.5      // C: ver secção 6 (c >= 0.5)

    // mesma assinatura do XAgent.play: serve na Console
    static play(board):
        mcts = new MCTS(ITERATIONS, C, System.nanoTime())     // semente nova: jogo não repetido
        devolver mcts.chooseMove(board)

    // para testes e experiências: configuração e semente explícitas
    construtor MCTSAgent(iterations, c, seed):
        guardar os três valores

    play(board):
        devolver new MCTS(iterations, c, seed).chooseMove(board)
```

```text
Console.playMove():                                   // só muda a linha do jogador automático
    se board.getTurn() == X:
        position = getHumanMove()
    senão:
        position = MCTSAgent.play(board)              // era XAgent.play(board)
    board.move(position)
```

Variante opcional por tempo (para a demonstração), dentro de `MCTS.chooseMove`:

```text
fim = agora() + orcamentoMs
enquanto agora() < fim:                               // em vez de: para it de 0 a iterations
    ... mesmo corpo do ciclo ...
```

Se a fizeres, **guarda as duas versões**: nos testes continuas a usar iterações.

### P5. `AgentIntegrationTests` (passo 4)

Auxiliares:

```text
jogarPartida(agenteX, agenteO):                       // agente: função (Board) -> casa
    b = new Board()
    enquanto não b.isGameOver():
        agente = (b.getTurn() == X) ? agenteX : agenteO
        b.move(agente(b))
    devolver b.getWinner()

contra(agenteMCTS, agenteAdversario, partidas):       // metade como X, metade como O
    vitorias = empates = derrotas = 0
    para i de 0 a partidas-1:
        mctsJogaComX = (i par)
        w = jogarPartida(...)                         // pôr o MCTS em X ou em O conforme i
        contar o resultado do ponto de vista do MCTS
    devolver (vitorias, empates, derrotas)
```

Casos:

```text
testeGanhaEmUma:
    para cada linha em LINHAS, para cada jogador:     // posição com 2 na linha e a 3.ª livre
        b = posição montada com a vez do jogador
        afirmar agente.play(b) == casa livre da linha

testeBloqueia:
    posição em que só o adversário tem 2 na linha e o jogador não tem vitória em 1
    afirmar agente.play(b) == casa que bloqueia

testeJogadaLegalEArgumentoIntacto:
    b = posição qualquer não terminal ; copia = b.clone()
    m = agente.play(b)
    afirmar m está em copia.getAvailableMoves() e b.equals(copia)

testeMesmaSementeMesmaJogada:
    afirmar new MCTSAgent(500, c, 7).play(b) == new MCTSAgent(500, c, 7).play(b)

testeUmaCasaLivre:
    b = tabuleiro com 8 casas jogadas sem fim, e a vez do jogador
    afirmar agente.play(b) == a única casa livre

testeContraAleatorio:
    (v, e, d) = contra(MCTSAgent(1000, 0.5, seed), XAgent::play, 100)
    afirmar d == 0                                    // ou d <= 2 se quiseres margem

testeMCTSContraMCTS:                                  // c = 1.0: força suficiente para empatar
    afirmar jogarPartida(MCTSAgent(2000,1.0,s1), MCTSAgent(2000,1.0,s2)) == Blank
```

### P6. `Experiments` (passo 5)

**Minimax oracle** (só em `test/`, não faz parte da solução):

```text
memo = mapa { (tabuleiro, vez) -> valor }

valor(b):                                             // do ponto de vista de X: +1, 0, -1
    se b.isGameOver():
        devolver (winner == X) ? +1 : (winner == O) ? -1 : 0
    se (b em memo): devolver memo[b]
    valores = [ valor(filho) para filho em b.children() ]
    v = (b.getTurn() == X) ? max(valores) : min(valores)
    memo[b] = v
    devolver v

minimaxAgent(b):                                      // devolve uma jogada ótima
    melhor = nenhuma ; melhorValor = (b.getTurn() == X) ? -inf : +inf
    para cada filho em b.children() (por ordem):
        v = valor(filho)
        se (vez X e v > melhorValor) ou (vez O e v < melhorValor):
            melhorValor = v ; melhor = casa jogada para chegar a filho
    devolver melhor
```

**Ciclo de experiências:**

```text
configuracoes = produto(iteracoes [100, 1000, 10000],
                        c [0.1, 0.2, 0.5, sqrt(2)],
                        adversario [aleatorio, minimax, mcts])
PARTIDAS = 200 ; SEMENTE_BASE = 12345

abrir results/resultados.csv
escrever cabeçalho: iteracoes,c,adversario,papel,partidas,vitorias,empates,derrotas,
                    tempo_medio_jogada_ms,simulacoes_por_jogada,semente

para cada cfg em configuracoes:
    para papel em [X, O]:
        vitorias = empates = derrotas = 0
        tempoTotal = 0 ; jogadas = 0 ; simulacoes = 0
        para i de 0 a PARTIDAS/2 - 1:
            semente = SEMENTE_BASE + hash(cfg, papel, i)       // reproduzível
            agente = new MCTSAgent(cfg.iteracoes, cfg.c, semente)
            b = new Board()
            enquanto não b.isGameOver():
                se b.getTurn() == papel:
                    t0 = nanoTime()
                    casa = agente.play(b)
                    tempoTotal += nanoTime() - t0 ; jogadas++
                senão:
                    casa = jogadaDoAdversario(cfg.adversario, b)
                b.move(casa)
            registar resultado do ponto de vista do MCTS
        escrever linha no CSV
fechar
```

Contar as simulações: o `MCTS` não expõe quantas fez. Opções, da menos para a mais intrusiva:
1. Calcular à parte: com «expandir todos», `n(raiz) = 1 + Σ n(filhos)` (usa o relatório e o exemplo 7.10).
2. Acrescentar ao `MCTS` um campo package-private `lastSimulations`, atribuído no fim de `chooseMove` a `root.n`.

Decide uma e regista-a em (iv).

**Saída para o relatório:**

```text
para cada tabela pedida (V/E/D por c × iterações; taxa de jogadas ótimas por c;
                         tempo por jogada vs iterações):
    ler o CSV e agregar
    escrever a tabela em Markdown/CSV e (se quiseres) um gráfico
```

A taxa de jogadas ótimas por `c`:

```text
para i de 0 a 150-1:
    b = posição aleatória não terminal (jogadas aleatórias com semente fixa)
    ótimas = { casa : valor(filho(casa)) é o melhor para quem joga }
    m = MCTSAgent(3000, c, semente_i).play(b)
    acertos += (m em ótimas)
taxa = acertos / 150
```

### P7. Javadoc, UML e zip (passos 6 e 7)

**Modelo de Javadoc** (inglês; primeira frase curta; só `@return` se não for `void`):

```text
/**
 * Chooses the move for the player to move in <code>state</code>.
 *
 * @param state the current game state (not modified)
 * @return the cell (0 to 8) to play
 * @throws IllegalStateException if the game is already over
 * @see Ilayout#children()
 */
```

Comando (pasta `T4_IA`, JDK 25): `javadoc -private -d docs src/*.java`

**UML** (PlantUML, para colar em plantuml.com e exportar a imagem):

```text
@startuml
interface Ilayout {
  +move(index): boolean
  +isGameOver(): boolean
  +getTurn(): ID
  +getWinner(): ID
  +getAvailableMoves(): HashSet<Integer>
  +children(): List<Ilayout>
}
enum ID { Blank  X  O }
class Board { -board: ID[][] ; -playersTurn: ID ; -winner: ID
              -movesAvailable: HashSet<Integer> ; -moveCount: int ; -gameOver: boolean
              +clone(): Object }
class MCTS { -iterations: int ; -c: double ; -seed: long ; -maxPlayer: ID
             +chooseMove(state: Ilayout): int }
class "MCTS.Node" as Node { layout: Ilayout ; parent: Node ; children: List<Node>
                            maxToPlay: boolean ; move: int ; n: int ; w: double }
class MCTSAgent { +play(board: Ilayout): int }
class XAgent { +play(board: Ilayout): int }
class Console
Ilayout <|.. Board
Ilayout +-- ID
MCTS +-- Node
Node "1" o-- "0..*" Node : children
MCTS ..> Ilayout
MCTSAgent ..> MCTS
Console ..> Board
Console ..> MCTSAgent
Console ..> XAgent
@enduml
```

**Zip limpo:**

```text
criar pasta entrega/
copiar: relatório, src/, test/, docs/, uml, lib/ (se os testes a precisarem), run_tests.sh
NÃO copiar: .classpath, .project, .vscode/, bin/, out/, bin-test/
numa cópia limpa: javac src/*.java e ./run_tests.sh têm de passar
zip -r grupoXX_assignment3.zip entrega/
```
