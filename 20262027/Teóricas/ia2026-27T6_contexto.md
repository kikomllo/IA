# Contexto — T6 (ia2026-27 T6): procura heurística I

**Fonte:** `ia2026-27T6.pdf` · **Data nos slides:** 09/10/2026 · Russell & Norvig cap. 4 · Costa e Simões cap. 3

## Resumo
Introduz as **heurísticas** e a **procura ordenada (melhor-primeiro)**: sôfrega, uniforme e **A***. Define heurística admissível e consistente. O sumário do slide 2 promete também IDA* e local search, mas só chegam à T8–T10.

## Conteúdo

### Heurística
Do grego "descobrir". Regra prática, simplificação ou conjetura que reduz a procura de soluções em domínios difíceis; **não garante soluções ótimas**. Orienta a procura para caminhos promissores; decidir que nó expandir é **meta-raciocínio**; uma heurística perfeita resolveria o problema sem procura; o **tempo gasto a calculá-la** deve ser recuperado por uma procura melhor.

### Função de avaliação heurística `f̂(n)`
Índice de promessa (mérito) do nó `n`; estimativa do custo do caminho mais curto do estado inicial ao objetivo **passando por `n`**.

### Algoritmo melhor-primeiro (procura ordenada)
Duas coleções: `A` (abertos = fila por avaliar, ordenada por `f̂`) e `F` (fechados = já avaliados).

```
A = {}; F = {}
A <- no(inicial)
repetir
    se A vazia: fracasso
    atual = primeiro(A)
    se atual é objetivo: devolve caminho(atual)
    remove atual de A; adiciona atual a F
    Sucs = expande(atual); avalia f̂(Sucs)
    para cada S em Sucs:
        se S não está em A nem em F:            insere S em A
        se S está em A e f̂(S) < f̂ do nó em A:   atualiza em A
        se S está em F e f̂(S) < f̂ do nó em F:   atualiza em F
```

Casos particulares: `f̂ = ĝ` é **custo uniforme**; `f̂ = ĥ` é **procura sôfrega** (*greedy*).

### Exemplo de heurística: 8-puzzle
`h1(n)` = nº de peças fora do sítio. `h1(Start) = 8`, `h1(Goal) = 0`.

### Procura sôfrega
Completa? **Não**. Discriminador? **Não**. Tempo e espaço exponenciais (guarda tudo). Exemplo da Roménia: o caminho sôfrego custa 450 vs o ótimo 418 (`140+99+211 = 450`; `140+80+97+101 = 418`).

### A*
Motivação: **evitar expandir nós que já são dispendiosos**. `f̂(n) = ĝ(n) + ĥ(n)`.

- **`h` admissível**: `h(n) ≤ h*(n)` (o verdadeiro custo restante) e `h(G) = 0` para qualquer objetivo `G`. **Nunca sobrestima.**
- **`h` consistente**: `h(n) ≤ c(n, n') + h(n')` para todo o `n` e sucessor `n'`.
- **Admissibilidade do A***: expande nós por valores **crescentes de `f`**; o contorno `i` tem todos os nós com `f = f_i`, onde `f_i < f_{i+1}`.
- Propriedades: **completo, discriminador (ótimo), tempo exponencial, espaço exponencial** (mantém todos os nós em memória).

## Relevância para o Problem 3
- Implementação de referência: `../T3_IA/src/classes/BestFirst.java` (A* com `State`, `abertos` e `melhorG`) e a explicação em `../T3_IA/IMPLEMENTATION_GUIDE.md`.
- O MCTS no Ball Sort usa o mesmo vocabulário (`g`, `f = g + sim`), mas **sem admissibilidade**: `sim` é uma amostra aleatória, não uma garantia.
