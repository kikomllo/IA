# Contexto — T16 (ia2026-27 T16): fundamentos e pressupostos do neurocomputing

**Fonte:** `ia2026-27T16.pdf` · **Data:** 10 Nov 2026 · Russell & Norvig cap. 18 · Negnevitsky cap. 6 · Costa e Simões cap. 5

## Resumo
Início da **abordagem conexionista**: pressupostos, neurónio biológico vs artificial e o **perceptrão de Rosenblatt** com a regra de aprendizagem supervisionada.

## Conteúdo

### Motivação
- **Ilusão de Adelson** (checker shadow): a perceção engana, o cérebro faz inferências.
- **Pombos como peritos de arte** (Watanabe et al., 1995 e 2001): discriminam Monet/Picasso e Van Gogh/Chagall com ~95 % de acerto nos quadros do treino e ~85 % em quadros **não vistos**. Não decoram: **extraem padrões** (o "estilo") e **generalizam**. É isso que as redes neuronais fazem bem.

### Pressupostos da abordagem conexionista
- Os animais reagem de forma adaptativa às mudanças do ambiente usando o sistema nervoso; um modelo adequado do sistema nervoso deve produzir respostas semelhantes em sistemas artificiais; o sistema nervoso é construído por **unidades simples, os neurónios**.
- Outros pressupostos correntes: a cognição **emerge** das interações sinápticas entre muitos neurónios simples; a informação neuronal relevante é completamente baseada na membrana (neurónio de **Hodgkin-Huxley**); a força das **sinapses** medeia a memória e a aprendizagem (Hebb, potenciação de longo prazo).
- Gráfico das "ondas históricas" (Goodfellow, 2016): cibernética (anos 40-60), conexionismo (80-90), deep learning.

### Neurónio biológico vs artificial
Neurónio biológico (micrografia, sinapses, comunicação por *spikes*); neurónio artificial: entradas `x1…xn`, pesos `w1…wn`, soma ponderada e saída `o`. Cérebro humano (slides ilustrados).

### Perceptrão de Rosenblatt (1957)
Unidade de limiar (**TLU**, *Threshold Linear Unit*).
- Dado um conjunto de treino finito com duas classes **linearmente separáveis** `T = {(x_i, d_i) | i = 1…M}` com `d_i ∈ {0, 1}` e uma taxa de aprendizagem `η` com `0 < η ≤ 1`, estimar os pesos `W` que classificam corretamente `T`.
- **Algoritmo**: (1) `k = 0`; (2) inicializar `W[0]` com valores pequenos aleatórios; (3) repetir até convergir: escolher aleatoriamente `(x_j, d_j)`; calcular `o_j = perceptron(x_j)`; **atualizar `W[k+1] = W[k] + η (d_j − o_j) x_j`**; `k = k + 1`.
- **Teorema da convergência de Rosenblatt**: o algoritmo converge para a classificação correta se os dados forem **linearmente separáveis**, `η` suficientemente pequeno, o conjunto for finito e os padrões forem apresentados repetidamente e de forma aleatória. Depois de `N` iterações `W[N] = W[N+1] = …` (o hiperplano que separa as classes).
- Exemplo: função **AND** com a fronteira `w1·x1 + w2·x2 − θ = 0`.

## Relevância
Fora do âmbito do Problem 3. Entra no exame e nos trabalhos seguintes.
