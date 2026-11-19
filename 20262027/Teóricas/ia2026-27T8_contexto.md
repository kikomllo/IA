# Contexto — T8 (ia2026-27 T8): procura heurística II

**Fonte:** `ia2026-27T8.pdf` · **Data:** 13 Out 2026

## Resumo
Como **gerar** funções heurísticas (relaxação sistemática do problema) e o algoritmo **IDA***, a versão de A* com espaço linear.

## Conteúdo

### Como desenhar heurísticas
Três fontes: conhecimento sobre o problema, **geração sistemática** por relaxação, aprendizagem (mais tarde na UC).

### Relaxação sistemática (8-puzzle)
Uma peça em `p` pode mover-se para `q` sse **(1)** `vazia(q)` e **(2)** `adjacente(p, q)`.
- Relaxar as **duas** condições dá `h1` = nº de peças fora do sítio (`h1(Start) = 8`).
- Relaxar a **condição 1** (ignora que `q` tem de estar vazia) dá `h2` = soma das **distâncias de Manhattan** (`h2(Start) = 3+1+2+2+2+3+3+2 = 18`).
- O custo ótimo do problema relaxado é **admissível** para o original (contexto adicional, não dito nos slides: como `h2 ≥ h1`, `h2` domina `h1` e expande menos nós).

### Comparação de custos de procura e tamanhos de espaços
Há um slide só com imagem a comparar custos de procura (ver o PDF). Tempos da procura cega a 10 M nós/s: 8-puzzle `10^5` nós (0.01 s); cubo de Rubik 2×2 (`10^6`, 0.2 s); 15-puzzle `10^13` (6 dias); cubo 3×3 `10^19` (**68 000 anos**).

### IDA* (Korf, 1985)
- Versão de A* com **espaço linear** que usa a mesma função `f̂ = ĝ + ĥ`.
- Cada iteração faz **profundidade-primeiro** com um **limite de corte** em `f`; se não encontra solução, o novo limite é o menor `f` que excedeu o anterior (puzzle: limite inicial, 1ª iteração, 2ª iteração…).
- Mais simples e muitas vezes **mais rápido** que o A* por menor *overhead* por nó; o desempenho depende da qualidade da heurística.
- Resultados de Korf: 15-puzzle com Manhattan: soluções ótimas de ~53 movimentos, 400 M nós, ~50 s; cubo de Rubik com **pattern databases** (1997): soluções ótimas de mediana 18 movimentos, ~1 dia cada.

## Relevância
Dá o contexto da heurística do T3 (`h = bolas − Σ maior base por cor`, uma relaxação). Para o MCTS, a lição é "o desempenho depende da qualidade da avaliação"; a avaliação do MCTS é feita por **simulação**.
