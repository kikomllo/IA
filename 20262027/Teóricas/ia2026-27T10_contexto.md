# Contexto — T10 (ia2026-27 T10): procura local II e introdução aos jogos

**Fonte:** `ia2026-27T10.pdf` · **Data:** 20 Out 2026 · Russell & Norvig cap. 4 e 6 · Costa e Simões cap. 9

## Resumo
Fecha a procura local (arrefecimento simulado, *beam search*) e **abre a segunda parte**: teoria dos jogos, interações de soma nula e **procura com agentes adversários**.

## Conteúdo

### Procura local (continuação)
- **Arrefecimento simulado**: ver T9.
- **Local beam search**: mantém `k` estados em memória; gera os sucessores de todos; se algum é objetivo, devolve; senão fica com os `k` melhores sucessores e repete. Risco: os `k` estados iniciais podem não ser diversos (agrupam-se num máximo local). Melhoria: **stochastic beam search**, escolhe `k` ao acaso com probabilidade crescente com o valor.
- **Algoritmos genéticos** (mais tarde na UC): variante de stochastic beam search. População de `k` indivíduos (cadeias num alfabeto finito, ex. {0, 1}) avaliados por uma **função de mérito (fitness)**; pais escolhidos pela fitness; sucessores gerados por **combinação de dois pais**. Vistos como procura estocástica paralela.
- Resumo da procura heurística de agente único: heurística e função de avaliação, procura ordenada, geração e propriedades de heurísticas, aprendizagem de heurísticas, A*, IDA*, alpinismo, arrefecimento simulado, local beam.

### Teoria dos jogos (fundamentos)
- Estudo matemático da interação entre agentes autónomos, **racionais e com interesses próprios**. Dá um quadro para pensar interações estratégicas; saber teoria dos jogos não garante ganhar.
- Um **jogo** é a representação formal de uma situação de interdependência estratégica: conjunto de `n` agentes (jogadores) `Ag`, cada um com ações (movimentos); a **combinação das ações** de todos determina os **resultados (outcomes)** e estes definem as **recompensas (payoffs)**.
- **Preferências e utilidades**: com dois agentes `{Ai, Aj}` e `W = {w1, w2, …}` o conjunto de resultados, cada agente tem uma **função de utilidade** `ui : W → ℝ`; `w ≽_i w'` significa `ui(w) ≥ ui(w')`. Assumem-se maximizadores de utilidade esperada.
- **Estratégia** = plano de ação.
- **Interações de soma nula** (estritamente competitivas): `ui(w) + uj(w) = 0` para todo o `w`.

### Porquê os jogos competitivos
São difíceis de resolver (xadrez: `b = 35`, ~`35^100 ≈ 10^154` nós), forçam a **decidir mesmo quando a decisão ótima é inviável** (racionalidade limitada), a formalização é fácil e é fácil medir sucesso ou fracasso. Exemplo trabalhado: **jogo do galo** (no xadrez: `r = 35`, `p = 50 × 2`).

### Procura de agente único vs agentes adversários
| | Agente único | Adversários |
|---|---|---|
| Solução | método (heurístico) para achar um **caminho** para o objetivo | uma **estratégia** |
| Qualidade | pode ser ótima | limitações temporais forçam solução **aproximada** |
| Função de avaliação | estima o custo do caminho **através** do estado | avalia a **bondade** do estado atual |
| Exemplos | puzzles, planeamento | xadrez, damas, Othello, galo |

### Formulação do jogo de dois agentes
Ambiente completamente observável, determinístico-estratégico, estático, discreto, multi-agente. **MAX e MIN** jogam à vez; soma nula; informação completa. Componentes: **estado inicial** (configuração e primeiro a jogar), **função sucessor** (movimentos legais), **teste de fim de jogo**, **função de utilidade** nos estados terminais.

## Relevância para o Problem 3
Esta formulação **é** a do Tic-Tac-Toe do Problem 3 (MAX/MIN, soma nula, utilidade nos terminais). As aulas T11 e T12 usam-na para minimax e MCTS.
