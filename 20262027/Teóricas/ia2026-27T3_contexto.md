# Contexto — T3 (ia2026-27 T3): limites da computabilidade e agentes

**Fonte:** `ia2026-27T3.pdf` · A aula junta o fim da introdução (computabilidade, fundamentos) com o início de **Agentes e ambientes**.

## Resumo
Primeiro, o que um computador **não pode** fazer e os fundamentos da IA. Depois a noção de **agente**, de **agente racional** e as **propriedades dos ambientes**, que servem para classificar os problemas de procura (o Tic-Tac-Toe e o Ball Sort têm propriedades diferentes).

## Conteúdo

### Limites da computabilidade
- **Hipótese de Church-Turing**: o que é computável é computável por uma máquina de Turing, e só isso.
- **Problema da paragem**: não existe uma máquina universal que, dado um programa `M` e um input `I`, decida se `M` pára em `I`.
- **Teoremas de Gödel (1931)**: um sistema formal "suficientemente poderoso" não pode ser ao mesmo tempo completo e consistente; num sistema consistente há proposições indemonstráveis; não há procedimento construtivo para provar a consistência dos axiomas.
- **Limites de recursos**: problemas **NP-completos / NP-difíceis** (tempo explode com o tamanho do input), **PSPACE-completos** (espaço). Exemplo: TSP com 13 509 cidades dos EUA (`USA13509`).
- Pergunta em aberto: *AGI em breve?* Dados, energia, investimento.

### Fundamentos da IA
Filosofia (lógica, mente), Matemática (provas, computação, (in)tratabilidade, probabilidade), Psicologia, Economia (decisão racional, **teoria dos jogos**), Linguística, Neurociência, Teoria de controlo.

### Agentes
- **Agente** (Russell & Norvig): qualquer coisa que perceba o ambiente por **sensores** e atue sobre ele por **atuadores**. Termos: *percept* (entrada perceptiva num instante), *percept sequence*, ambiente.
- Exemplos: humano (olhos / mãos), robô (câmaras / rodas), computador (teclado / ecrã). Agentes triviais: termóstato, *daemon* de UNIX.
- **Inteligência de enxame**: formigas; **PSO** (*Particle Swarm Optimization*): cada partícula tem posição e velocidade e compara o seu melhor estado com o dos vizinhos (exemplo: aprender xadrez, Duro e jvo, 2008).
- **Definição forte** (Wooldridge e Jennings, 1995): software com **autonomia, capacidade social, reatividade e pró-atividade**. Outras: mobilidade, veracidade, benevolência, aprendizagem, racionalidade.
- **Agentes vs objetos**: um objeto não controla o seu comportamento (quem chama decide); um agente decide se faz o que lhe pedem; sistemas multi-agente são inerentemente multi-thread; agentes podem ter objetivos divergentes.

### Agente racional
Para cada sequência de percepções, escolhe a ação que se espera maximizar a sua **medida de desempenho**, dada a evidência e o conhecimento embutido. Especifica-se com **PEAS**: Performance, Environment, Actuators, Sensors (exemplo: agente de anestesia, Fazendeiro, jvo e Pedrycz, 2008).

### Propriedades dos ambientes
| Propriedade | Distinção | Nota |
|---|---|---|
| Discreto vs contínuo | nº finito de estados ou não | xadrez: ~10^154 estados, mas discreto |
| Completamente vs parcialmente observável | o agente vê todo o estado? | o mundo real é só parcialmente observável |
| Determinístico vs estocástico | cada ação tem um efeito garantido? | **estratégico** = determinístico exceto as ações de outros agentes |
| Estático vs dinâmico | muda só pelas ações do agente? | o mundo real é dinâmico |
| Episódico vs sequencial | a decisão atual afeta as futuras? | |

## Relevância para o Problem 3
- **Tic-Tac-Toe**: completamente observável, **estratégico**, estático, discreto, sequencial, **multi-agente**.
- **Ball Sort**: observável, determinístico, estático, discreto, sequencial, **agente único**.
