# Contexto — T9 (ia2026-27 T9): procura local I

**Fonte:** `ia2026-27T9.pdf` · **Data:** 18 Out 2026 · Russell & Norvig cap. 4

## Resumo
Quando o **caminho é irrelevante** e só interessa o estado final (projeto de circuitos, planeamento de fábrica, *scheduling*, otimização de redes), usam-se algoritmos de **procura local**: um único estado atual (ou poucos), memória constante, mesmo em espaços enormes.

## Conteúdo

### Procura local e otimização
Vantagens: memória quase constante; encontra frequentemente soluções razoáveis em espaços de estados enormes. Não guarda caminho.

### Exemplo de trabalho: n-rainhas
Colocar `n` rainhas num tabuleiro `n × n` sem que se ataquem (mesma linha, coluna ou diagonal). Nº de soluções para `n = 4, 8, 12, 16`: 2, 92, 14 200, 14 772 512.
- **Representações** possíveis: matriz `n×n`; conjunto de pares ordenados; **permutação** (a rainha da coluna `i` está na linha `x_i`; garante à partida uma rainha por linha e por coluna).
- **Função a minimizar**: `ĥ(board) = Σ_i Σ_{j>i} a(x_i, x_j)` com `a(x, y) = 1` se `x` ataca `y`, 0 se não (nº de pares que se atacam). Ou maximizar `score = −ĥ`.
- Exemplo dos slides: tabuleiro com `ĥ = 17` e o `ĥ` de cada sucessor por coluna; **mínimo local** com `ĥ = 1`.

### Hill-climbing (alpinismo, procura sôfrega local)
"Como escalar o Evereste com nevoeiro cerrado e amnésia": move-se sempre para o melhor vizinho; pára quando nenhum vizinho é melhor. Pode **ficar preso** em mínimos/máximos locais e planaltos (exemplo 4-rainhas com sucesso e com falha).

Variantes:
- **Random-restart**: reinicia `n` vezes de estados aleatórios e devolve o melhor.
- **First-choice**: gera sucessores aleatórios até achar um melhor.
- **Estocástico**: escolhe aleatoriamente, com probabilidade dependente da qualidade do sucessor.

### Cristalização / arrefecimento simulado (*simulated annealing*)
Analogia mecânica: recozimento de metais. Aceita **movimentos para pior** com probabilidade que diminui com a "temperatura" `T`:

```
current <- estado inicial
para t = 1 até ∞:
    T <- schedule[t]
    se T = 0: devolve current
    next <- sucessor aleatório de current
    ΔE <- VALUE[next] − VALUE[current]
    se ΔE > 0: current <- next
    senão:     current <- next só com probabilidade e^(ΔE/T)
```

Com `T` alto aceita quase tudo (explora); com `T → 0` torna-se hill-climbing. Exemplos: maximizar uma função com vários máximos; 4-rainhas.

## Relevância
Outro exemplo do tema **exploração vs aproveitamento** (aceitar piores soluções para explorar), que reaparece no `c` do UCB do MCTS (aula T12).
