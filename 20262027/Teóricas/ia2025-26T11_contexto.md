# Contexto — ia2025-26T11: jogos de soma nula e procura adversária II (minimax)

**Fonte:** `ia2025-26T11.pdf` (deck do ano anterior; a data nos slides é 23/10/26) · Russell & Norvig cap. 6 · Costa e Simões cap. 9

## Resumo
Jogos em **forma extensiva** (árvores de jogo), **estratégia vencedora** e o algoritmo **minimax**; como lidar com espaços enormes (**corte + função de avaliação**); introdução aos **cortes α-β**.

## Conteúdo

### Jogos em forma extensiva
Um jogo de dois jogadores em forma extensiva é uma **árvore** em que os níveis alternam entre o jogador `i` e o jogador `j` e as **folhas** têm as **recompensas** (payoffs) dos jogadores (exemplo com ações C/D e pagamentos `(1,1)`, `(4,4)`, `(1,4)`, `(4,1)`).

### Estratégia vencedora
Uma **estratégia** é um plano de ação (função que, dado um nó, dá o movimento). Uma **estratégia vencedora** leva o jogador à vitória sempre que ela é possível.

### Minimax
Para jogos por turnos, de soma nula e informação perfeita; MAX joga primeiro; assume que **ambos são racionais** (se MIN não joga racionalmente, é ainda melhor para MAX).

```
MINIMAX-VALUE(n) = UTILITY(n)                          se n é terminal
                   max_{s ∈ suc(n)} MINIMAX-VALUE(s)   se n é MAX
                   min_{s ∈ suc(n)} MINIMAX-VALUE(s)   se n é MIN
```

```
MINIMAX-DECISION(state): v <- MAX-VALUE(state); devolve a ação de SUCCESSORS(state) com valor v
MAX-VALUE(state): se TERMINAL-TEST, devolve UTILITY; v <- -∞; para cada (a,s): v <- max(v, MIN-VALUE(s))
MIN-VALUE(state): se TERMINAL-TEST, devolve UTILITY; v <- +∞; para cada (a,s): v <- min(v, MAX-VALUE(s))
```

Propriedades: dá a estratégia vencedora/ótima; tempo `O(b^m)`; espaço `O(b^m)` (os slides escrevem assim; implementado em profundidade-primeiro costuma ser `O(b·m)`).

### Contas que mostram o problema
Se o espaço tem `10^40` nós e se gera um nó em 1/3 ns, são ~**10^20 séculos** (um século = 31 536 × 10^6 s). Daí que **a terminação seja impossível** em jogos reais.

### Interromper a procura
- **MinimaxCutoff** = minimax com `TERMINAL-TEST → CUTOFF-TEST` e `UTILITY → EVAL`.
- Fatores a considerar no corte: alguém ganhou? Quantas jogadas já foram exploradas? Quanto tempo resta? Quão promissor é o caminho? Quão estável é a configuração?
- **`Eval`**: estimativa de `Utility`. Requisitos: ordenar os nós terminais como `Utility`; ser **muito rápida**; nos estados não terminais estar **fortemente correlacionada** com as hipóteses reais de ganhar.
- **Jogo do galo**: `Eval(p) = +∞` se MAX ganha, `−∞` se MIN ganha, senão `(nº de linhas, colunas e diagonais abertas para MAX) − (nº das abertas para MIN)`. Slides com a árvore de 2 níveis calculada (`6−4 = 2`, `5−4 = 1`, …) e os valores propagados por minimax.
- **Xadrez**: `Eval(s) = w1·f1(s) + w2·f2(s) + … + wn·fn(s)`; ex. `w1 = 9`, `f1 = nº de rainhas brancas − nº de rainhas pretas`; outras: estrutura de peões, segurança do rei, controlo do centro.
- **Profundidade na prática**: com 100 s e 10^4 nós/s dá 10^6 nós por jogada; `b^m = 10^6` com `b = 35` dá `m = 4`. Um jogador de 4-ply é fraco ("nabo"); 8-ply ≈ PC comum / nível de mestre; 12-ply ≈ Deep Blue II / Kasparov.

### Cortes α-β (introdução)
Os slides mostram um exemplo passo a passo; as regras formais estão na T12.

## Relevância para o Problem 3
- O Tic-Tac-Toe permite minimax completo (espaço pequeno): é um **bom adversário de teste** para o agente MCTS.
- O MCTS substitui `Eval` por **simulações**.
