# Contexto — T4 (ia2026-27 T4): agentes reativos

**Fonte:** `ia2026-27T4.pdf` · **Data:** 29 Set 2026

## Resumo
Agentes **puramente reativos**: decidem só com a percepção atual, sem memória, sem representação de conhecimento nem raciocínio abstrato. Arquiteturas: veículos de Braitenberg, arquitetura de *subsumption* de Brooks e o *Mars explorer* de Steels.

## Conteúdo

### Reatividade e agentes puramente reativos
- Um sistema reativo mantém uma interação contínua com o ambiente e responde às mudanças **a tempo de a resposta ser útil**. Os ambientes interessantes são dinâmicos.
- **Puramente reativo**: decide sem referência ao histórico (ex. termóstato). Biologicamente plausível, simples, económico, tratável, robusto. A inteligência vê-se como **propriedade emergente** de sistemas complexos.
- O programa do agente é um ciclo `percept → regras condição-ação → ação`:

```java
Environment e; Cond-ActionRules r;
while (true) { state = perceive(e); a = selectAction(state, r); e.applyAction(a); }
```

### Veículos de Braitenberg (1984)
Veículos com sensores ligados diretamente a motores. Versões v1, v2a/v2b (covarde / agressivo), v3a/v3b (adorador / explorador), v3c com pares de sensores (luz, temperatura, oxigénio, comida). Parece ter **valores** (foge do calor, ataca luzes, procura sítios com oxigénio e comida), mas só tem sensores e atuadores: o comportamento surge da **interação agente-ambiente**; os valores estão na cabeça do observador. Conclusão: é muito mais difícil **analisar** o comportamento a partir da observação do que **sintetizá-lo**.

### Arquitetura de *subsumption* de Brooks
- Ideias: inteligência é emergente; surge da interação com o ambiente; está "nos olhos de quem vê"; a inteligência "real" está **situada** no mundo; comportamento inteligente sem representação explícita nem raciocínio abstrato.
- Decomposição por **comportamentos** (em vez de módulos funcionais): hierarquia de comportamentos tipo regra, que **competem** pelo controlo. As camadas **mais baixas** (mais primitivas, ex. evitar obstáculos) têm **precedência** sobre as mais altas.
- Contexto histórico: *Shakey* (Nilsson), robô clássico baseado em planeamento simbólico, contra o qual Brooks reagiu.

### *Mars explorer* (Steels, 1990)
Veículos exploram Marte recolhendo amostras de rocha (agrupadas, localização desconhecida), sem mapa e sem comunicação direta. O sinal de rádio da nave-mãe dá um **gradiente** para voltar à base. Hierarquia (0 ≺ 1 ≺ 2 ≺ 3 ≺ 4):
0. obstáculo → mudar de direção
1. a levar amostras e na base → largar
2. a levar amostras e fora da base → subir o gradiente
3. vê amostra → apanhá-la
4. senão → mover-se aleatoriamente

Com cooperação (inspirada em formigas): ao regressar com amostras, o agente **deixa 2 migalhas** de rocha; se sente migalhas, apanha 1 e **desce o gradiente** (segue o trilho até às amostras).

### Vantagens e limitações
- **Vantagens**: plausibilidade biológica, simplicidade, economia, tratabilidade, robustez, elegância.
- **Limitações**: sem informação não-local nem de longo prazo; perda de **transparência** (se funciona, não se sabe bem porquê; se falha, é difícil depurar); sem metodologia sistemática; difícil com muitos comportamentos; comunicação com humanos.

## Relevância
Contraste com a procura em espaço de estados: aqui **não há planeamento**. Os agentes aleatórios/reativos do Tic-Tac-Toe do Problem 3 (o "agente reativo que joga aleatoriamente") são deste tipo; o MCTS é um agente **deliberativo** que faz *look-ahead*.
