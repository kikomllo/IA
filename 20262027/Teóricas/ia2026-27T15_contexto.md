# Contexto — T15 (ia2026-27 T15): dilema do prisioneiro iterado e introdução ao Machine Learning

**Fonte:** `ia2026-27T15.pdf` · **Data:** 6 Nov 2026 · Russell & Norvig cap. 18 · Negnevitsky cap. 6 · Costa e Simões cap. 5

## Resumo
Termina a teoria dos jogos de soma não nula com o **dilema do prisioneiro iterado** e o torneio de Axelrod; **começa a parte de aprendizagem máquina**.

## Conteúdo

### Recapitulação e jogos 2×2 simétricos
Tópicos já dados: preferências e utilidades; forma normal (matrizes de pagamento) e forma extensiva (árvores de jogo); dominância; jogos de soma nula (maxmin); soma não nula: dilema do prisioneiro, **equilíbrio de Nash**; jogos 2×2 **simétricos** (slides com a classificação de Rapoport, Guyer e Gordon, 1976; alguns têm dois equilíbrios de Nash).

### Dilema do prisioneiro iterado
- Jogar **mais do que uma vez**. Se sabes que voltas a encontrar o adversário, o incentivo a trair parece desaparecer. A **cooperação pode tornar-se racional** no PD infinitamente repetido, porque as consequências futuras contam.
- **Nº finito e conhecido de rondas `n`**: **indução retroativa**: na ronda `n` convém trair; logo na `n−1` também; … **a deserção é a melhor estratégia**.

### Torneio de Axelrod (1984)
Programas a jogar o PD iterado contra uma gama de adversários. Estratégias:
- **ALLD**: "sempre trair" (falcão).
- **TIT-FOR-TAT**: na ronda 0 coopera; na ronda `u > 0` faz o que o adversário fez na ronda `u − 1`.
- **TESTER**: na 1ª ronda trai; se o adversário retaliar, joga TIT-FOR-TAT, senão alterna cooperação e traição.
- **JOSS**: como TIT-FOR-TAT mas trai de vez em quando.

**Porque ganha o TIT-FOR-TAT (Axelrod)**: não é invejoso (não joga como se fosse soma nula); é **simpático** (começa por cooperar e retribui cooperação); **retalia** de forma adequada (pune a traição de imediato com força "medida"); **não guarda rancor** (retribui a cooperação de imediato).

Há um slide "Try yourself" (para experimentar).

### Programa detalhado (slide de situação)
Mostra **onde vamos**: o resto da UC é interações de soma não nula (feito) → **aprendizagem máquina** → **abordagem conexionista** (perceptrão, ADALINE, retropropagação, Hebb, Kohonen, deep learning) → **abordagem biológica** (algoritmos genéticos, programação genética, estratégia evolutiva, enxame).

### Introdução ao Machine Learning
- IA ⊃ ML ⊃ Deep Learning.
- **Definição (Mitchell, 1997)**: um programa aprende com a experiência `E` relativamente a uma classe de tarefas `T` e uma medida de desempenho `P` se o seu desempenho em `T`, medido por `P`, melhora com `E`.
- Exemplos `T / P / E`: jogar damas (% de jogos ganhos / jogar contra si próprio); reconhecer palavras manuscritas; classificar emails como spam; carro autónomo (distância até um erro avaliado por humanos / imagens e comandos de volante de um condutor).
- **Porque importa**: essencial em ambientes desconhecidos ou variáveis no tempo; útil como método de construção (há tarefas mais fáceis de especificar por exemplos); estão sempre a ser recolhidos dados novos.
- **Variantes**: entradas (pares atributo/valor, reais), saídas (numéricas, simbólicas, gráficas), tipo de aprendizagem (indutiva, dedutiva), tipo de feedback (**supervisionada**, por reforço, **não supervisionada**), tarefa (classificação, regressão, *clustering*), representação da função (numérica: regressão linear, redes neuronais, SVM; simbólica: regras; baseada em instâncias: vizinho mais próximo; modelos gráficos probabilísticos).
- **Regressão**: saída contínua (ex. preço em função de cm³); **classificação**: saída discreta (ex. tumor maligno sim/não).
- Disciplinas ligadas: psicologia, neurociência, estatística, ciência da computação, controlo adaptativo, evolução, economia.

## Relevância
Fecha a parte de jogos. O MCTS do Problem 3 é *aprendizagem por simulação* em tempo de execução (não há fase de treino), mas o AlphaZero (referido no enunciado) junta MCTS e redes neuronais, a ponte para esta parte da UC.
