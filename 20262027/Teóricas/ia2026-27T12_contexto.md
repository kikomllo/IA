# Contexto — T12 (ia2026-27 T12): procura adversária III (α-β, Go, início do MCTS)

**Fonte:** `ia2026-27T12.pdf` · **Data:** 27 Out 2026 · Russell & Norvig cap. 6 · **Aula-chave para o Problem 3**

## Resumo
Termina os **cortes α-β**, mostra a história dos programas de jogos e porque o **Go** quebrou o minimax, e **introduz o MCTS/UCT** com o problema dos *multi-armed bandits*.

## Conteúdo

### Cortes α-β
- `α` = valor auxiliar provisório de um nó **maximizante**; `β` = valor auxiliar provisório de um nó **minimizante**.
- O `α` de um nó MAX é o **máximo atual dos `β`** dos seus sucessores; o `β` de um nó MIN é o **mínimo atual dos `α`** dos seus sucessores. **Os `α` nunca diminuem; os `β` nunca aumentam.**
- **Regra 1 (corte α)**: pára-se a procura abaixo de qualquer nó **minimizante** cujo `β ≤ α` de qualquer antecessor maximizante (em qualquer nível). O `β` passa a definitivo (pode não ser o valor verdadeiro, mas serve para os cálculos seguintes).
- **Regra 2 (corte β)**: pára-se abaixo de qualquer nó **maximizante** cujo `α ≥ β` de qualquer antecessor minimizante.
- Há um exercício com uma árvore grande de ~45 folhas para resolver.
- **Ganho**: no pior caso o α-β visita `O(b^m)` nós como o minimax; **melhor caso** (filhos de MAX ordenados por valor decrescente, filhos de MIN por crescente): `O(b^(m/2))` (Knuth e Moore, 1975), duplica a profundidade; **ordem aleatória**: `O(b^(3m/4))`. Se soubéssemos ordenar perfeitamente não precisávamos de procura.
- Conclusões: **não altera o resultado** do minimax; boa ordenação melhora o corte; é um exemplo de **meta-raciocínio** (raciocinar sobre que computações são relevantes).

### História dos programas
Logistello (Buro) venceu Murakami por 6-0 em Othello (1997); Deep Blue II em 1997 (nível de grande mestre); Stockfish 19 com ~3650 Elo (open source, 19 vezes campeão TCEC, nos slides a 27 Out 2026).

### Go
- 19×19, jogar uma pedra ou passar, ganha quem ficar com mais território; fim quando ambos passam. **Duas regras**: captura (pedras sem liberdades são removidas) e *ko* (não se pode repetir a posição anterior).
- **Porque é difícil**: espaço de ~**10^170** estados, factor de ramificação ~200, ambiente sequencial (uma jogada tem efeitos dezenas de jogadas depois), **sem boa função de avaliação**, características locais e globais.
- Cronologia: 2005 inútil; 2008 MoGo (MCTS) venceu um 5º dan em 9×9; 2013 Zen venceu um 9º dan em 19×19; 2018 AlphaZero aprendeu do zero. "Os humanos já não estão à frente."

### Monte Carlo Tree Search (Kocsis e Szepesvári, 2006)
Quatro fases (cíclicas): **Selection** (*tree policy*), **Expansion**, **Simulation** (*default / rollout policy*), **Backpropagation**.

**Exemplo passo a passo (UCT)** com recompensas em [0, 1]:
1. A árvore começa numa folha. Faz-se um *rollout* aleatório até um terminal (ex. recompensa 1) e propaga-se (nos slides os nós vão ficando com 1, 1/2, 1/3…).
2. **Cada ação de um nó tem de ser selecionada pelo menos uma vez** antes de se usar a tree policy.
3. Quando todas as ações do nó já foram tentadas, escolhe-se segundo a **tree policy**.
4. Pergunta dos slides: *qual é uma tree policy apropriada?*

### Multi-armed bandit
`k` máquinas com recompensas aleatórias desconhecidas: qual jogar a seguir? Procura-se uma estratégia de puxar braços tal que a recompensa total esperada no instante `n` fique perto do melhor possível (puxar sempre o melhor braço).
- **Bandit uniforme** (Even-Dar et al., 2002): puxar cada braço `w` vezes e devolver o de melhor média. **Péssima escolha**: desperdiça tempo em braços maus.
- **Exploration vs exploitation**: quando explorar uma máquina nova? Quando parar de explorar e aproveitar a que parece melhor?

### UCT
`π(s) = argmax_a [ Q(s,a) + c·√( ln n(s) / n(s,a) ) ]` onde `Q(s,a) = wins(s,a)/n(s,a)`, `n(s)` = nº de vezes que `s` foi encontrado nas simulações e `n(s,a)` = nº de vezes que `a` foi tomada em `s`; `c` = parâmetro de exploração.
Referências nos slides: Kocsis e Szepesvári (ECML 2006, "Bandit based Monte-Carlo planning") e Browne et al. (2012, *A Survey of Monte Carlo Tree Search Methods*, IEEE TCIAIG).

## Relevância para o Problem 3
É **a** base teórica do trabalho: as 4 fases, a regra "visitar cada ação uma vez" (o enunciado exige selecionar o nó por visitar antes de avaliar a expressão) e a expressão (1). A continuação (MIN, agente único) está na T13. Ver `../T4_IA/IMPLEMENTATION_GUIDE.md`.
