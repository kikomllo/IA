# Contexto — T14 (ia2026-27 T14): expectiminimax e interações de soma não nula

**Fonte:** `ia2026-27T14.pdf` · **Data:** 5 Nov 2026

## Resumo
Estende o minimax a jogos com **acaso** (expectiminimax) e passa para **interações de soma não nula**: forma normal, **dilema do prisioneiro**, dominância e **equilíbrio de Nash**.

## Conteúdo

### Classificação dos jogos
Slide de recapitulação (determinísticos vs com acaso, informação perfeita vs imperfeita; ex. xadrez, gamão, Kriegspiel).

### Expectiminimax (jogos com acaso: gamão)
O gamão tem dados. Introduzem-se **nós de acaso** entre os níveis MAX e MIN:

```
EXPECTED-MINIMAX-VALUE(n) = UTILITY(n)                              se n é terminal
                            max_{s} EXPECTIMINIMAX(s)               se n é MAX
                            min_{s} EXPECTIMINIMAX(s)               se n é MIN
                            Σ_{s} P(s) · EXPECTIMINIMAX(s)          se n é nó de acaso
```

(Os slides escrevem `MINIMAX-VALUE` nos ramos max/min por gralha.)
- No **minimax**, os valores exatos de `Eval` são irrelevantes (basta a ordem): qualquer transformação monótona de `Eval` preserva o comportamento. No **expectiminimax já não**: as médias ponderadas dependem dos valores (exemplo com 2 resultados de probabilidades 0.9 e 0.1: o melhor movimento muda).
- Na prática: o lançamento de 2 dados dá **21 resultados**, o que aumenta o factor de ramificação e torna o α-β **muito menos eficaz**. **TD-Gammon** usa pesquisa de profundidade 2 + uma avaliação muito boa (rede neuronal treinada por aprendizagem por reforço) e ficou entre os 3 melhores do mundo.

### Interações de soma não nula (estáticas)
- Modelo: dois agentes escolhem **em simultâneo** uma ação (C = cooperar, D = trair/defect); o resultado em `W` depende da **combinação** de ações; informação completa. A função de transformação de estado pode ser sensível aos dois agentes, a nenhum ou só a um.
- **Forma normal**: **matriz de pagamentos** (payoff matrix) com os pagamentos de `i` e `j` em cada célula.

### Dilema do prisioneiro (PD)
Dois detidos, em celas separadas e sem comunicação: se um confessa e o outro não, o que confessa fica livre e o outro apanha 20 anos; se ambos confessam, 5 anos cada; se nenhum confessa, 1 ano cada. Matriz de pagamentos (i, j):

| | j: C (calar) | j: D (confessar) |
|---|---|---|
| **i: C** | (−1, −1) | (−20, 0) |
| **i: D** | (0, −20) | (−5, −5) |

- **Teorema** (1 slide): se o PD é jogado por agentes racionais, ambos confessam (D). Prova: C é **estritamente dominada** por D (`ui(C,C) < ui(D,C)` e `ui(C,D) < ui(D,D)`; idem para `j`), logo um agente racional nunca joga C.
- **Paradoxo**: o equilíbrio de **estratégia dominante** (D, D) dá −5 a cada um, mas o resultado **ótimo de Pareto** é (C, C) com −1 cada. Parece implicar que a cooperação não aparece em sociedades de agentes racionais e egoístas. O PD é omnipresente (ex. crise do armamento nuclear).

### Dominância e equilíbrio de Nash
- Por vezes a melhor resposta de um agente **depende** do que os outros jogam: não há estratégia dominante. O **jogo da coordenação** tem dois equilíbrios e nenhuma estratégia dominante.
- **Equilíbrio de Nash**: duas estratégias `s1` e `s2` estão em equilíbrio de Nash se (1) assumindo que `i` joga `s1`, `j` não pode fazer melhor que jogar `s2`; e (2) assumindo que `j` joga `s2`, `i` não pode fazer melhor que `s1`. **Nenhum tem incentivo a desviar.** Formalmente `(s, t)` é Nash se `∀ s' ∈ S1, t' ∈ S2: U1(s', t) ≤ U1(s, t)` e `U2(s, t') ≤ U2(s, t)`.
- Exemplos nos slides: **matching pennies** (sem Nash em estratégias puras), **jogo da coordenação**, **dilema do prisioneiro**.
- Explica acordos que se auto-impõem, convenções sociais estáveis, e consequência da inferência racional. **Mas** pode não existir (em forma pura), pode não ser único e as pessoas nem sempre se comportam como o equilíbrio prevê.

## Relevância
Contexto de "soma não nula" vs o Tic-Tac-Toe do Problem 3, que é de **soma nula** (por isso minimax/MCTS com MAX e MIN fazem sentido).
