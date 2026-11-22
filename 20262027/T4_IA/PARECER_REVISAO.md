# Parecer sobre o relatório de verificação (RELATORIO_VERIFICACAO.md)

**Data:** 2026-11-22
**Âmbito:** só código, testes e comentários de `T4_IA`. O relatório académico e o UML ficam com os autores.

Este parecer compara o relatório de verificação com o código atual (`Board`, `Console`, `MCTS`, `MCTSAgent`, `XAgent`), com o enunciado (`Problem 3 - MCTSv3.2c.pdf`) e com o `IMPLEMENTATION_GUIDE.md`. Os 90 testes e o Javadoc **não foram voltados a correr**; os números do relatório são aceites como estão.

## 1. Opinião em resumo

O relatório é bom e quase tudo o que afirma sobre o código confere. A recomendação de **não mexer no MCTS** está certa. O que discordo é da calibração das prioridades: a correção com mais valor é a consola, e o pedido de `@inv`/`@version`/`@see` como "etiquetas obrigatórias" não tem base no enunciado.

## 2. O que está certo

- **Expansão de todos os sucessores.** Manter. O enunciado diz "attaching a given number of successors" e "for each successor, apply a random sequence", por isso não é um desvio.
- **Política MIN.** `-w/n + c·√(ln t / n)` maximizado é equivalente a minimizar `w/n - c·√(ln t / n)`, como o relatório diz.
- **Falhas reais confirmadas no código:**
  - `Board.move(int)`, `move(int,int)` e `isBlank(int)` não validam limites (`move(-1)` dá `ArrayIndexOutOfBoundsException`).
  - O construtor de `MCTS` aceita `c = +∞` (`!(c >= 0)` não o rejeita).
  - `chooseMove(null)` dá `NullPointerException` indireta.
  - `Console.getHumanMove()` imprime o erro mas devolve a posição na mesma.
- **Imports sem uso:** `Timer` e `TimerTask` em `Console`, `List` em `XAgent`.

## 3. Onde discordo ou matizo

### 3.1 `@author`, `@version`, `@inv`, `@see` não são "pedidos"

O enunciado não os exige e o `IMPLEMENTATION_GUIDE.md` (linha 875) diz que são **opcionais**.

- `@inv` é uma etiqueta personalizada. Com `javadoc` simples falha com "unknown tag" e pode partir a geração para quem não use `-tag "inv:a:Invariant:"`.
- Proposta: pôr `@author` nas classes (barato e inofensivo), escrever a invariante em texto na descrição da classe, **sem** `@inv`.
- Corrigir os avisos reais do Javadoc (`@return` em falta, construtores e campos sem comentário) faz sentido.

### 3.2 O caso "casa ocupada" na consola está exagerado

`board.move()` devolve `false` sem trocar o turno, por isso o jogador é só perguntado outra vez. O que rebenta de facto:

- índice fora de `0..8` (`ArrayIndexOutOfBoundsException` dentro do `Board`);
- texto não numérico (`InputMismatchException` do `Scanner`).

Esta é a correção com mais valor, porque a consola é a forma como se joga o trabalho.

### 3.3 Validação no `Board` tem valor baixo

O MCTS nunca passa índices inválidos e, se a consola validar, o utilizador também não. Vale a pena (poucas linhas, `IllegalArgumentException`), mas não é prioridade 1. O `Board` é o esqueleto do professor; a alteração é permitida, mas não necessária.

### 3.4 Correções do MCTS são triviais

- Construtor: exigir `Double.isFinite(c) && c >= 0`.
- `chooseMove`: `Objects.requireNonNull(state, ...)`.
- Risco concreto de `c = ∞`: com `parent.n = 1`, `∞ · 0 = NaN`, nenhum filho passa em `confBound > bestValue`, `best` fica `null` e rebenta com `NullPointerException`.

### 3.5 A limpeza da prioridade 3 pode ficar quase toda de fora

Quase tudo vem do esqueleto (`new String(sb)`, `catch (Exception)` em `clone()`, `import java.util.*`). Risco e ganho são pequenos. Só faria:

- remover os imports sem uso;
- traduzir os poucos comentários em português de `MCTS.java` (o guia pede um só idioma).

## 4. O que o relatório não viu

1. **`Board.move(int x, int y)`** usa `movesAvailable.remove(y * N + x)`. O índice é `y * M + x` (ver `move(int index)`). Só funciona porque `N = M = 3`. Não é bug hoje, mas contradiz o elogio à generalização com `K`, `N` e `M`. Corrigir é trocar um carácter.
2. **Prazo.** O enunciado diz entrega até **18/11/2026**; hoje é 22/11. Confirmar se há extensão antes de gastar tempo em polimento.
3. **Ficheiros de IDE no repositório.** Foram para o GitHub `.classpath`, `.project` (em `T4_IA`) e `.vscode/settings.json` (com o caminho do JDK de uma máquina). Não servem para a entrega. Remover do git e acrescentar ao `.gitignore`.
4. **`docs/` está no git** (Javadoc gerado) e vai ficar desatualizado depois das alterações. Regenerar no fim, ou decidir se se versiona.

## 5. Ordem de trabalho proposta

1. **Consola:** ciclo de leitura que só termina com um índice em `0..8` e casa livre; tratar texto não numérico (consumir a linha e repetir). O método só devolve o índice; `playMove()` continua a chamar `board.move()` uma vez. Para ser testável, um construtor `Console(Scanner)` de visibilidade de pacote chega.
2. **MCTS:** `Double.isFinite(c)` e verificação de `null`.
3. **Board:** validação de limites em `move(int)`, `move(int,int)` e `isBlank(int)` com `IllegalArgumentException`; manter `IllegalStateException` após o fim e `false` para casa ocupada; corrigir `y * N + x` para `y * M + x`.
4. **Testes novos** (sem remover nenhum existente):
   - `move(-1)`, `move(N*M)`, `move(-1,0)`, `move(0,-1)`, `move(M,0)`, `move(0,N)` e `isBlank(-1)`, `isBlank(N*M)` lançam `IllegalArgumentException`;
   - o `Board` fica igual depois de cada tentativa inválida (células, turno, `moveCount`, movimentos livres, vencedor, `gameOver`);
   - `new MCTS(..., +∞, ...)` e `new MCTS(..., -∞, ...)` rejeitados (e via `MCTSAgent`);
   - `chooseMove(null)` rejeitado com exceção explícita;
   - consola: texto não numérico, índice fora do limite e casa ocupada são repetidos.
5. **Javadoc:** `@author` nas classes, `@return` e comentários em falta, sem `{@code}` se preferirem texto simples, sem `@inv`. Gerar com `javadoc -private -d docs src/*.java`.
6. **Limpeza mínima:** imports sem uso e comentários em português.
7. **Repositório:** remover `.classpath`, `.project` e `.vscode/` do git; atualizar `.gitignore`.

## 6. O que não deve mudar

- MCTS como algoritmo, expansão de todos os sucessores, MAX/MIN com as fórmulas atuais, filhos não visitados primeiro.
- Recompensas `1`, `0.5`, `0` do ponto de vista de MAX; Backpropagation soma o mesmo resultado em todos os níveis.
- Decisão final por visitas (desempate por `w`, depois primeiro filho).
- `Board.children()` ordenado e independente; `MCTSAgent` e `XAgent` como estão; `Minimax` só em `test/`.
- Os nomes privados usados por reflexão nos testes (`Node`, `n`, `w`, `selectMove`, `expandLeaf`, `simulation`, `backPropagation`).
- Os resultados experimentais só se repetem se alguma correção alterar a decisão do agente (nenhuma das propostas altera).

## 7. Validação final

1. Compilar `src/` e `test/` com JDK 25.
2. Correr os testes rápidos e os `slow`; confirmar os 90 anteriores mais os novos, com zero falhas, abortos e ignorados.
3. Gerar o Javadoc e confirmar que não restam avisos evitáveis.
4. Jogar uma partida na consola com entrada inválida (texto, `-1`, `9`, casa ocupada).
5. Não deixar ficheiros compilados ou temporários na pasta de entrega.
