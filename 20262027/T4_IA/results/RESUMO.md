# Resultados das experiências (item viii)

Gerado por `test/Experiments.java`.

- Semente base: 12345 (+1000·iterações); MCTS com MAX = quem joga na raiz.
- Partidas por combinação: 100 por papel com 100 iterações, 50 com 1 000, 15 com 10 000 (X e O em partes iguais); cada célula das tabelas junta os dois papéis.
- Células: **V/E/D** do MCTS (vitórias/empates/derrotas).
- `mcts` = outro MCTS com a mesma configuração e semente diferente. `minimax` escolhe ao acaso entre as jogadas ótimas.
- «Simulações» = `n(raiz)` por jogada (contadas com um decorador de `Ilayout`, sem alterar o `MCTS`).

## V/E/D contra aleatorio

| iterações \ c | 0.10 | 0.20 | 0.50 | 1.41 |
|---|---|---|---|---|
| 100 | 188/10/2 | 191/8/1 | 192/7/1 | 191/9/0 |
| 1000 | 94/5/1 | 94/6/0 | 99/1/0 | 97/3/0 |
| 10000 | 28/2/0 | 28/2/0 | 29/1/0 | 30/0/0 |

## V/E/D contra minimax

| iterações \ c | 0.10 | 0.20 | 0.50 | 1.41 |
|---|---|---|---|---|
| 100 | 0/148/52 | 0/167/33 | 0/183/17 | 0/185/15 |
| 1000 | 0/92/8 | 0/92/8 | 0/100/0 | 0/100/0 |
| 10000 | 0/28/2 | 0/30/0 | 0/30/0 | 0/30/0 |

## V/E/D contra mcts

| iterações \ c | 0.10 | 0.20 | 0.50 | 1.41 |
|---|---|---|---|---|
| 100 | 37/127/36 | 16/170/14 | 2/195/3 | 10/184/6 |
| 1000 | 5/91/4 | 3/96/1 | 0/100/0 | 0/100/0 |
| 10000 | 0/30/0 | 0/30/0 | 0/30/0 | 0/30/0 |

## Tempo médio por jogada (ms) e simulações por jogada (contra aleatório)

| iterações | c | ms/jogada | simulações/jogada |
|---|---|---|---|
| 100 | 0.10 | 0.43 | 194 |
| 100 | 0.20 | 0.54 | 239 |
| 100 | 0.50 | 0.89 | 319 |
| 100 | 1.41 | 1.05 | 353 |
| 1000 | 0.10 | 0.84 | 1200 |
| 1000 | 0.20 | 1.28 | 1307 |
| 1000 | 0.50 | 2.50 | 1729 |
| 1000 | 1.41 | 4.49 | 2337 |
| 10000 | 0.10 | 2.84 | 10136 |
| 10000 | 0.20 | 4.04 | 10532 |
| 10000 | 0.50 | 6.94 | 11330 |
| 10000 | 1.41 | 10.98 | 12920 |

## Jogadas ótimas em 150 posições aleatórias (minimax como oráculo)

| iterações \ c | 0.10 | 0.20 | 0.50 | 1.41 |
|---|---|---|---|---|
| 100 | 127/150 | 135/150 | 137/150 | 135/150 |
| 1000 | 136/150 | 141/150 | 149/150 | 150/150 |
| 10000 | 141/150 | 147/150 | 150/150 | 150/150 |
