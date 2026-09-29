from heapq import heappush, heappop
from collections import defaultdict

# --- Custos de swap ---
COST_EE = 2   # ambos pares
COST_OO = 20  # ambos ímpares
COST_EO = 11  # par/ímpar (diferentes paridades)

def parity_cost(a, b, costs=(COST_EE, COST_OO, COST_EO)):
    ee, oo, eo = costs
    a_even = (a % 2 == 0)
    b_even = (b % 2 == 0)
    if a_even and b_even:
        return ee
    if (not a_even) and (not b_even):
        return oo
    return eo

def build_goal_positions(goal):
    """Mapeia valor -> lista de índices alvo (seguro para duplicados)."""
    pos = defaultdict(list)
    for j, v in enumerate(goal):
        pos[v].append(j)
    return pos

def distance_sum(state, goal_positions):
    """
    Soma de |idx_atual - idx_objetivo| para cada elemento.
    Para duplicados: atribui a 1ª ocorrência à 1ª posição alvo, etc.
    """
    taken = defaultdict(int)
    total = 0
    for i, v in enumerate(state):
        g_list = goal_positions[v]
        j = g_list[taken[v]]  # i-ésima ocorrência -> i-ésima no objetivo
        taken[v] += 1
        total += abs(i - j)
    return total

def heuristic(state, goal_positions, costs=(COST_EE, COST_OO, COST_EO), mode='avg'):
    """
    mode='avg' -> (ee+oo+eo)/3 * distance_sum   (não necessariamente admissível)
    mode='min' -> min(ee,oo,eo)  * distance_sum (admissível)
    """
    ee, oo, eo = costs
    if mode == 'avg':
        avg = (ee + oo + eo) / 3.0
        return avg * distance_sum(state, goal_positions)
    elif mode == 'min':
        mn = min(ee, oo, eo)
        return mn * distance_sum(state, goal_positions)
    else:
        raise ValueError("Unknown heuristic mode: use 'avg' or 'min'")

def a_star_adjacent_swaps(start, goal, costs=(COST_EE, COST_OO, COST_EO), hmode='avg'):
    start_t = tuple(start)
    goal_t = tuple(goal)
    goal_positions = build_goal_positions(goal_t)

    # Priority queue: (f, h, tie_id, state)
    open_heap = []
    tie = 0

    # Melhores g e pais
    g_best = {start_t: 0.0}
    parent = {start_t: (None, None, 0.0)}  # state -> (prev_state, move_info, move_cost)

    h0 = heuristic(start_t, goal_positions, costs, hmode)
    heappush(open_heap, (h0, h0, tie, start_t))
    tie += 1

    closed = set()
    expansions = 0

    while open_heap:
        f, h, _, state = heappop(open_heap)
        if state in closed:
            continue
        closed.add(state)
        expansions += 1

        if state == goal_t:
            # Reconstruir caminho
            path = []
            cur = state
            while cur is not None:
                prev, move_info, move_cost = parent[cur]
                g = g_best[cur]
                h_cur = heuristic(cur, goal_positions, costs, hmode) if cur != goal_t else 0.0
                f_cur = g + h_cur
                path.append((cur, g, h_cur, f_cur, move_info, move_cost))
                cur = prev
            path.reverse()
            return {
                'path': path,        # lista de (state, g, h, f, move_info, move_cost)
                'expansions': expansions,
                'closed_size': len(closed)
            }

        n = len(state)
        g_state = g_best[state]

        # Vizinhos: swap adjacente i <-> i+1
        for i in range(n - 1):
            a, b = state[i], state[i + 1]
            c = parity_cost(a, b, costs)
            new_list = list(state)
            new_list[i], new_list[i + 1] = new_list[i + 1], new_list[i]
            new_state = tuple(new_list)
            tentative_g = g_state + c
            if tentative_g < g_best.get(new_state, float('inf')):
                g_best[new_state] = tentative_g
                parent[new_state] = (state, {'swap_idx': i, 'a': a, 'b': b}, c)
                h_new = heuristic(new_state, goal_positions, costs, hmode)
                heappush(open_heap, (tentative_g + h_new, h_new, tie, new_state))
                tie += 1

    return None  # não encontrou (não deve acontecer com swaps adjacentes)
    
if __name__ == "__main__":
    start = [3, 5, 4, 2, 1]
    goal  = [1, 2, 3, 4, 5]
    res_avg = a_star_adjacent_swaps(start, goal, costs=(COST_EE, COST_OO, COST_EO), hmode='avg')
    res_min = a_star_adjacent_swaps(start, goal, costs=(COST_EE, COST_OO, COST_EO), hmode='min')

    def print_path(res, title):
        print(title)
        if not res:
            print("Sem solução.")
            return
        for step, (state, g, h, f, move, move_cost) in enumerate(res['path']):
            if step == 0:
                print(f"Step {step}: state={list(state)} | g={g:.2f} h={h:.2f} f={f:.2f} (start)")
            else:
                print(
                    f"Step {step}: state={list(state)} | g={g:.2f} h={h:.2f} f={f:.2f} "
                    f"| move=swap[{move['swap_idx']}] ({move['a']},{move['b']}) cost={move_cost}"
                )
        print(f"Expansions: {res['expansions']}, Closed size: {res['closed_size']}")
        print()

    print_path(res_avg, "Heuristic: average cost x distance")
    print_path(res_min, "Heuristic: MIN cost x distance (admissible)")