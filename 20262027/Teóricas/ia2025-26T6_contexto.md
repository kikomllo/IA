# Contexto — ia2025-26T6: procura cega (não informada)

**Fonte:** `ia2025-26T6.pdf` (deck do ano anterior, reutilizado em 26/27) · Russell & Norvig cap. 3 · Costa e Simões cap. 3

## Resumo
Algoritmos de procura que **não usam conhecimento do problema** para guiar a escolha: largura, custo uniforme, profundidade, profundidade limitada, aprofundamento iterativo, aleatória. Mais **procura em grafos** (evitar estados repetidos).

## Conteúdo

### Estados vs nós
Estado = representação de uma configuração do ambiente. **Nó** = estrutura da árvore: estado + pai + ação + custo + profundidade.

### Notação: `r` = factor de ramificação, `L` ou `p` = profundidade da solução

| Algoritmo | Fila (*fringe*) | Completo | Discriminador (ótimo) | Tempo | Espaço |
|---|---|---|---|---|---|
| **Largura-primeiro** | FIFO | Sim (r finito) | Sim se o custo for constante | `O(r^(L+1))` | `O(r^(L+1))` (guarda todos os nós) |
| **Custo uniforme** | prioridade por `g` | Sim se custo ≥ ε > 0 | Sim | `O(r^⌈C*/ε⌉)` | idem |
| **Profundidade-primeiro** | LIFO (pilha) | Não | Não | `O(r^p)` | `O(r·p)` (só o caminho atual + irmãos) |
| **Profundidade limitada** | LIFO com limite | Não (se a solução estiver abaixo do limite) | Não | `O(r^p)` | `O(r·p)` |
| **Aprofundamento iterativo** | repete profundidade limitada com limite 0, 1, 2… | Sim | Sim (custo constante) | `O(r^p)` | `O(r·p)` |

- Largura-primeiro: "esgota primeiro a memória do que a nossa paciência". Exemplo: `r = 10`, 10 000 nós/s, 1 KB/nó: profundidade 8 = 31 horas e 1 TB; profundidade 12 = 35 anos.
- Custo uniforme equivale a largura-primeiro se os custos forem iguais; `C*` = custo da solução ótima.
- **Aprofundamento iterativo** tem pouco *overhead*: com `r = 10`, `p = 5`: 111 111 nós gerados (profundidade) vs 123 456 (com aprofundamento) = **11 %**.
- A profundidade-primeiro tem duas variantes: pára ao encontrar solução (ou quando o nó não pode ser expandido), ou pára quando a profundidade atinge um **limite** pré-definido (evita caminhos infinitos).

### Procura em grafos
Os algoritmos que não guardam história "estão condenados a repetir-se": mantém-se uma lista de **fechados** para não expandir o mesmo estado duas vezes (o `melhorG` no `BestFirst` do T3 faz isto).

### Conclusões
Guardar as opções alternativas para o caso de a primeira escolha falhar; a formulação exige uma **abstração do ambiente**; o aprofundamento iterativo usa espaço linear e assintoticamente o mesmo tempo que os outros.

## Relevância
Baseline do T2 (Ball Sort com custo uniforme) e termo de comparação nos relatórios.
