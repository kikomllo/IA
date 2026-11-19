# Contexto — T1 (ia2026-27 T1): apresentação da UC

**Fonte:** `ia2026-27 T1.pdf` · **Data nos slides:** 15 Set 2026 · **Docente:** José Valente de Oliveira (jvo@ualg.pt) · **Páginas da UC:** tutoria.ualg.pt e <http://tiny.cc/ia2026-27>

## Resumo
Aula de apresentação: o que é a IA, o programa, a bibliografia e, sobretudo, **as regras de avaliação**. Não tem matéria técnica.

## Conteúdo

### O que é IA (definições citadas)
- Nilsson, 1971: construir máquinas que executam tarefas que, se feitas por humanos, seriam reconhecidas como requerendo inteligência.
- Rich, 1983: estudar como fazer os computadores fazerem coisas que hoje são mais bem feitas pelo Homem.
- P. Martins, 1990: conjunto de técnicas para tornar os computadores mais amigáveis e úteis.
- Simões e Costa, 2004: estudo e construção de entidades artificiais com capacidades cognitivas semelhantes às humanas.
- AAAI: "compreensão científica dos mecanismos subjacentes ao pensamento e ao comportamento inteligente e da sua incorporação em máquinas".

### Tipos de IA
- **Narrow AI (NAI, fraca/simulada)**: muito competente em objetivos complexos de ambientes concretos (jogos, diagnóstico médico, carros autónomos).
- **AGI (Artificial General Intelligence)**: atingir objetivos complexos em ambientes complexos com recursos computacionais limitados, com autonomia, compreensão prática de si e dos outros, e compreender "qual é o problema" em vez de resolver só os que o programador formula.
- Relação **IA ⊃ Machine Learning ⊃ Deep Learning**.

### Abordagens ou metáforas
Computacional (símbolos), conexionista (redes neuronais), biológica (evolutiva), bayesiana (probabilidades), analogista (semelhança).

### Programa (numa linha)
1. Introdução. 2. Agentes (reativos). 3. **Procura em espaço de estados**: agente único (cega, informada, A*, IDA*, estratégias alpinistas, funções heurísticas) e **agentes adversários** (minimax, cortes alfa-beta, **Monte-Carlo Tree Search**). 4. Aprendizagem e abordagem conexionista (perceptrão, ADALINE, retropropagação, Hebb, Kohonen, deep learning). 5. Adaptação e simulação da evolução natural (algoritmos genéticos, programação genética, estratégia evolutiva, enxame).

### Bibliografia
- Principal: Russell & Norvig, *Artificial Intelligence: A Modern Approach*, 4ª ed., Pearson, 2021.
- Complementar: Negnevitsky, *Artificial Intelligence: A Guide to Intelligent Systems* (3ª ed.); Costa e Simões, *Inteligência Artificial: Fundamentos e Aplicações* (2ª ed., FCA, 2008).

### Avaliação
- **Nota final = 0,6 × nota de exame + 0,4 × nota de avaliação prática.** Arredondamentos só na nota final.
- **Admissão a exame (qualquer época):** nota de avaliação prática **≥ 7,5 valores**.
- Exame: prova escrita **sem consulta**.
- Prática = média ponderada das notas dos trabalhos. Trabalhos individuais ou em grupo; **inscrição num grupo obrigatória** (só se aceitam trabalhos de estudantes inscritos), máximo **3 estudantes**; a nota de grupo é convertida em nota individual na validação.
- Um *assignment* pode ser um **Lab tutorial** ou um **Problem**. Para ser **validado**: cumprir todos os requisitos e restrições, ser aceite pelo **Mooshak** e ser **defendido com sucesso numa aula de laboratório**.
- Alguns trabalhos só pedem código no Mooshak; outros pedem também **relatório em PDF**. Há prazos no Mooshak; depois do prazo não se pode submeter. Não validados podem ser re-submetidos e defendidos de novo **dentro do prazo**.
- **Defesa**: podem perguntar qualquer coisa sobre a implementação, decisões e alternativas; falhar as respostas **invalida** o trabalho.

## Relevância para o trabalho
- Explica porque o Problem 3 tem de ser **defendido** e porque o relatório (zip) importa.
- O "MCTS" está no programa como último ponto de "problemas com agentes adversários".
