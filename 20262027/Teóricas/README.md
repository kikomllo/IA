# Teóricas — índice dos contextos

Cada aula (PDF) tem ao lado um ficheiro `*_contexto.md` com o resumo do que foi dado, as fórmulas e a relevância para os trabalhos. Servem de **base para explicações de matéria e para os trabalhos práticos**. Foram escritos a partir do texto dos slides; o que for contexto adicional vem identificado como tal. Alguns slides são só imagens (exemplos passo a passo): para esses, vê o PDF.

## Ordem lógica (matéria)

| # | Contexto | Tema | Entra em |
|---|---|---|---|
| 1 | [ia2026-27T1](ia2026-27T1_contexto.md) | Apresentação da UC, avaliação, regras dos trabalhos | Todos |
| 2 | [ia2026-27T2](ia2026-27T2_contexto.md) | Sobre a inteligência (psicologia, Turing) | Exame |
| 3 | [ia2026-27T3](ia2026-27T3_contexto.md) | Limites da computabilidade, agentes, propriedades dos ambientes | Exame |
| 4 | [ia2026-27T4](ia2026-27T4_contexto.md) | Agentes reativos (Braitenberg, subsumption, Mars explorer) | Exame |
| 5 | [ia2025-26T5](ia2025-26T5_contexto.md) | Procura em espaço de estados: formulação, métricas, notação O | T1, T2, T3, T4 |
| 6 | [ia2025-26T6](ia2025-26T6_contexto.md) | Procura cega | T2 |
| 7 | [ia2026-27T6](ia2026-27T6_contexto.md) | Procura heurística I: melhor-primeiro, sôfrega, A* | T1, T3 |
| 8 | [ia2026-27T8](ia2026-27T8_contexto.md) | Heurísticas por relaxação, IDA* | T3 |
| 9 | [ia2026-27T9](ia2026-27T9_contexto.md) | Procura local I (hill-climbing, arrefecimento simulado) | Exame |
| 10 | [ia2026-27T10](ia2026-27T10_contexto.md) | Procura local II, beam, início da teoria dos jogos | Exame, T4 |
| 11 | [ia2025-26T11](ia2025-26T11_contexto.md) | Minimax, função de avaliação, α-β (intro) | **T4** |
| 12 | [ia2026-27T12](ia2026-27T12_contexto.md) | α-β, Go, **MCTS / UCT, bandits** | **T4** |
| 13 | [ia2026-27T13](ia2026-27T13_contexto.md) | **MCTS (2 agentes e agente único)** | **T4** |
| 14 | [ia2026-27T14](ia2026-27T14_contexto.md) | Expectiminimax, dilema do prisioneiro, Nash | Exame |
| 15 | [ia2026-27T15](ia2026-27T15_contexto.md) | PD iterado, Axelrod, intro ao Machine Learning | Exame |
| 16 | [ia2026-27T16](ia2026-27T16_contexto.md) | Neurocomputing, perceptrão de Rosenblatt | Exame |

## Notas sobre os ficheiros

- **A numeração dos PDFs não é uma sequência única.** Três decks têm o prefixo `ia2025-26` (T5, T6, T11) e são os que o docente reutilizou em 26/27; o `ia2026-27T6` é uma aula **diferente** (heurística) do `ia2025-26T6` (procura cega). Por isso os contextos usam o nome do ficheiro e não só o número.
- **Faltam** aulas na pasta: não há `T5`, `T7` e `T11` com o prefixo `ia2026-27`. O "T7" pode ser uma aula prática ou sem slides, e o `ia2025-26T5/T6/T11` aparentam fazer esse papel. Se mais slides forem adicionados, acrescenta o contexto correspondente.
- A **ordem** da tabela acima segue a matéria e as datas escritas nos slides, não os números dos ficheiros.

## Para o Problem 3 (MCTS), ler por esta ordem
1. `ia2025-26T11` (minimax, avaliação) → 2. `ia2026-27T12` (α-β, Go, UCT, bandits) → 3. `ia2026-27T13` (MCTS completo + agente único) → 4. `../T4_IA/IMPLEMENTATION_GUIDE.md`.
