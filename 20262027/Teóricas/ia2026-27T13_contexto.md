# Contexto — T13 (ia2026-27 T13): MCTS (continuação) e MCTS de agente único

**Fonte:** `ia2026-27T13.pdf` · **Data nos slides:** 31 Out 2026 · Russell & Norvig cap. 6 · **Aula diretamente ligada ao enunciado do Problem 3**

## Resumo
Fecha o UCT com os dois termos da expressão e o tratamento de **nós MIN**, mostra a **retropropagação** e as melhorias, e apresenta o **MCTS para procura de agente único** com `g`, `sim`, `f`, `Fmax`, `reward`. As fórmulas coincidem com as do enunciado.

## Conteúdo

### UCT (expressão)
Para o filho `i` do nó: `UCT(i) = w_i / n_i + c·√( ln t / n_i )`

- `w_i`: vitórias no nó `i`; `n_i`: simulações em `i`; `t`: simulações no **pai** de `i`.
- **Termo de valor**: favorece ações que historicamente correram bem. **Termo de exploração**: bónus que cresce com `ln t`. **Não gasta muito tempo em braços sub-ótimos**, ao contrário do bandit uniforme.
- Exemplo nos slides: raiz com filhos `a1`, `a2` e valores 1, 1/2, 1/3…

### Nós MIN (turno do oponente)
Num nível que corresponde ao turno do adversário: **minimizar `w/n − c√(ln t / n)`** ou, equivalentemente, **maximizar `−w/n + c√(ln t / n)`**.

> Nos slides: *MAX nodes maximize UCT, whereas MIN nodes minimize its lower-bound counterpart `w/n − c√(ln t / n)`, so that exploration is favoured for both players (minimizing UCT itself would favour the less visited successors of MIN nodes instead of exploring them).*

É exatamente o texto do enunciado.

### Fases em recapitulação
Slides **Selection phase**, **Backpropagation phase** (propagar os resultados até à raiz) e **MCTS Recap**. Resultado: **exploração parcial** da árvore de jogo.

### Melhorias
- Conhecimento do domínio para melhorar a *rollout policy* (não escolher ações obviamente más) e para prever as jogadas do oponente.
- **Aprender uma função heurística** para avaliar e selecionar nós antes do fim da simulação.
- **AlphaGo**: evolução do computador no Go (do MCTS puro ao AlphaGo e AlphaZero).

### MCTS para procura em espaço de estados de agente único
Mesmas **4 fases**; só mudam os valores. Repetir até esgotar o recurso (tempo).

- **Tree policy**: **best-first**, em todos os níveis escolhe o filho que **maximiza** `value(i) = reward(i) + c·√( ln t / n_i )`. É a mesma expressão do UCT, com `reward(i)` no lugar de `wins/visits`.
- **Simulação e custo**: `g(i)` = soma dos custos da raiz até `i`; `sim(i)` = **custo mínimo** de `N` simulações aleatórias a partir de `i`, cada uma truncada em **`Cmax`**, `sim(i) ∈ [0, Cmax]`; `f(i) = g(i) + sim(i)`. `f(i)` é **estimativa**, porque `sim(i)` é o melhor de `N` amostras.
- **Recompensa**: `reward(i) = 1 − f(i)/Fmax`. O algoritmo maximiza mas queremos o custo mínimo. `Fmax` = **uma** cota superior de `f` válida para todos os nós (ex. profundidade máxima × custo máximo de um movimento + `Cmax`); **um denominador dependente do nó tornaria as recompensas incomparáveis**. `reward = 1` ⇔ `f = 0`; `reward = 0` ⇔ `f = Fmax`.
- **Retropropagação**: `sim(m) = min( sim(m), cost(m, i) + sim(i) )`, para cada nó `m` do caminho de `i` até à raiz; `cost(m, i)` = custo do caminho de `m` para baixo até `i`; todo o `sim(m)` **começa com `Cmax`**; as **contagens de visitas** são incrementadas no mesmo caminho. O melhor custo encontrado debaixo de um nó propaga-se aos antepassados.

### Pontos a levar (*take home points*)
MCTS/UCT é *state of the art* em **procura *anytime* com *look-ahead* probabilístico para decisões sequenciais** com incerteza. Dois ingredientes: **exploration vs exploitation** e **roll-out policy**.

## Relevância para o Problem 3
É o **resumo teórico do enunciado**: secção 8 do guia (`../T4_IA/IMPLEMENTATION_GUIDE.md`) detalha estes passos e o que fica por decidir (`Cmax`, `Fmax`, `N`, o que devolver).
