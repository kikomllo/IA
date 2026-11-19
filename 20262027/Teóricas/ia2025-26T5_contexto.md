# Contexto — ia2025-26T5: procura em espaço de estados (introdução)

**Fonte:** `ia2025-26T5.pdf` (deck do ano anterior, reutilizado em 26/27) · Russell & Norvig cap. 3 / Costa e Simões cap. 3

## Resumo
Como **formular um problema** como procura num espaço de estados, as **métricas** para comparar algoritmos e a **notação O**. É a base de todos os trabalhos práticos (T1 8-puzzle, T2 Ball Sort, T3 A*, T4 MCTS).

## Conteúdo

### Pressupostos da abordagem computacional
- **Physical Symbol System Hypothesis** (Newell e Simon, 1976): qualquer ação inteligente pode ser reproduzida por manipulação de símbolos.
- Racionalidade com restrições implica escolher **a tempo**. Um perito conhece (7 ± 2) × 10^4 factos do seu domínio. **A procura compensa a falta de conhecimento; o conhecimento alivia a necessidade de procura.**

### Agente resolvente de problemas e algoritmo de procura em árvore
Agente simples: formula o objetivo e o problema, procura uma sequência de ações e executa-a. O **algoritmo geral de procura em árvore** mantém uma fila de nós por expandir (*fringe*): retira um, testa se é objetivo, expande-o e junta os sucessores. O que distingue um algoritmo de outro é **a ordem** em que a fila é servida.

### Exemplo: 8-puzzle
Estado = posição das peças; estado inicial e objetivo dados; ações = mover o espaço para a esquerda/direita/cima/baixo; custo por ação = 1. A solução é a **sequência de movimentos** do estado inicial ao final.

### Métricas de avaliação de um algoritmo
- `L` comprimento da solução; `G` nós gerados; `E` nós expandidos; `P = L/G` (penetrância); `R` **factor de ramificação efetivo** (R > 1, quanto menor melhor), definido por `G = R + R² + … + R^L = R(R^L − 1)/(R − 1)`.
- Melhor caso (segue o caminho da solução sem procura): `L = G = E = 3`, `P = 1`, `R = 1`.
- **Completude**: garante encontrar solução, se existir, em tempo finito.
- **Discriminador** (optimalidade da solução): encontra a de menor custo.
- **Optimalidade** de um algoritmo sobre uma classe: domina todos os membros. **Dominância**: A1 domina A2 se todos os nós expandidos por A1 são também expandidos por A2 (estritamente se A2 não domina A1).
- **Complexidade temporal e espacial**.

### Notação O
`f(N) = O(g(N))` se existem `c, k > 0` tais que `f(N) ≤ c·g(N)` para `N > k`. Ordens: `O(1)`, `O(log N)`, `O(N)`, `O(N log N)`, `O(N²)`, `O(N³)`, exponencial `O(b^N)`, fatorial `O(N!)`. Tabelas de tempos de execução: um algoritmo exponencial ou fatorial é **inviável** mesmo com 1 Pflops para N moderado (ex. `2^N` com N = 83 demora séculos; `N!` "nunca").

## Relevância para o Problem 3
- `Ilayout`, `State`/`Node`, custo `g`, factor de ramificação: o vocabulário é este.
- O MCTS **não** é completo nem discriminador (ótimo) na variante de agente único; compara-se com A* usando estas métricas.
