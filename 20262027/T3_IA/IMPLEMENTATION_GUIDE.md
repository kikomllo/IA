# Problem 2 — Ball Sort with A* and a heuristic: implementation guide

Everything you need to implement this yourself: the theory, the heuristic with proofs, the data structures, the pitfalls in your T2 code, and how to test.

> **Status: implemented.** The code in `src/` follows this guide. Section 0 lists what was actually done and where; the other sections explain the why. This guide is kept in sync with the code: when the code changes, the affected sections change too.

**Contents**

0. [What was implemented (change index)](#0-what-was-implemented-change-index)
1. [What is being asked](#1-what-is-being-asked)
2. [The problem as a state space](#2-the-problem-as-a-state-space)
3. [Where your T2 code stands](#3-where-your-t2-code-stands)
4. [A* in depth](#4-a-in-depth)
5. [The heuristic](#5-the-heuristic)
6. [Making it fast (the 1 second limit)](#6-making-it-fast-the-1-second-limit)
7. [Code changes, file by file](#7-code-changes-file-by-file)
8. [Testing and validation](#8-testing-and-validation)
9. [Pitfalls checklist](#9-pitfalls-checklist)
10. [Suggested order of work](#10-suggested-order-of-work)

---

## 0. What was implemented (change index)

The source has no "change" markers any more: the new and changed code is documented the same way as in T2 (Javadoc on methods, short comments inside). The `#n` numbers below are only labels used in this guide, and the table says where each change is. Compare with `../T2_IA/src` to see what changed.

| # | File | What | Guide section |
|---|---|---|---|
| 1 | `interfaces/Ilayout.java` | New method `getH(Ilayout goal)` | 7 |
| 2 | `classes/BallSortLayout.java` | Lazily cached fields `key` (String) and `hCache` (int). Lazy because `children()` edits the clone after creating it. | 6.3 |
| 3 | `classes/BallSortLayout.java` | `key()`: each tube written as text bottom→top, tubes sorted, joined with `,`. `equals()` compares keys and `hashCode()` is `key().hashCode()`, so tube order is ignored. | 6.1, 6.3 |
| 4 | `classes/BallSortLayout.java` | `children()` tries only the first empty tube as destination | 6.2 |
| 5 | `classes/BallSortLayout.java` | `clone()` copies rows and `sizeTube` with `System.arraycopy` | 7 |
| 6 | `classes/BallSortLayout.java` | `getH()`: `h = balls − Σ largest base per color`, cached. The per-color array is indexed with the `Color` enum (#13). | 5.4, 5.6 |
| 7 | `classes/BestFirst.java` | `Map<Ilayout, Double> melhorG` (best g per layout) replaces `fechados` and `abertos.contains` | 4.3, 4.4 |
| 8 | `classes/BestFirst.java` | `State` caches `h` and `f = g + h` | 4.1 |
| 9 | `classes/BestFirst.java` | `State.hashCode()` delegates to the layout | 6.3 |
| 10 | `classes/BestFirst.java` | `sucessores` no longer filters the father (the `melhorG` check already rejects it) | 7 |
| 11 | `classes/BestFirst.java` | Queue ordered by `f`, ties to the larger `g`, using `Double.compare` | 4.1, 4.5 |
| 12 | `classes/BestFirst.java` | Lazy deletion: a polled entry with `g > melhorG` is skipped | 4.3, 4.4 |
| 13 | `classes/Color.java` (new), `BallSortLayout.getH()` | `enum Color { R, B, P, Y, G }`. `getH` finds a color's slot with `Color.valueOf(String.valueOf(color)).ordinal()`. An unknown letter now throws a clear `IllegalArgumentException` instead of indexing outside an array. | 5.6 |

`Main.java` is unchanged.

**Tests** (run from `T3_IA` with `./run_tests.sh`, or `./run_tests.sh fast` to skip the timing tests):

- `test/PuzzleUnitTests.java`: the T2 tests. One changed: `testEqualsDifferent` used a board that is the same puzzle with two tubes swapped, which now counts as equal.
- `test/AStarHeuristicTests.java`: heuristic values, admissibility, consistency, A\* vs the reference solver, tube-order equality, edge cases, time limit.
- `test/EndToEndTests.java`: text in through `System.in`, answer from `System.out`, like Mooshak: the expected output of every input file, input format (no final newline, Windows line endings), edge shapes (one tube, H = 1, H = 25, no empty tube, many empty tubes), and a real run in a new JVM.
- `test/StressTests.java`: ~2000 random boards with fixed seeds. Small ones are compared with the reference solver (including boards with no solution and shuffled tube order); the solution path is checked step by step; h is checked along the path of big boards; and many boards are timed against the 1 s limit.
- `test/TestSupport.java`: the independent BFS reference solver, the random board generators (`scrambled`, `shuffled`) and the one-move legality check (section 8).
- `test/inputs/`: `sample1.txt`, the T2 inputs T01–T10 and `hard1.txt` (the heaviest board found).

**Measured** (this machine, 75 tests pass; the timing tests are tagged `perf`):

| Case | Result |
|---|---|
| Sample 1, whole program in a new JVM | 37, about 40–130 ms |
| 150 shuffled boards, H 6–10, 4–5 colors, 2 empty tubes | slowest about 45 ms (inside the test JVM) |
| 60 boards with 3–4 empty tubes | slowest 100–500 ms (varies from run to run) |
| `test/inputs/hard1.txt` (5 colors, H = 8, 4 empty tubes, 25 000 states expanded) | 39, about 500 ms as a whole program; it has taken 1.7–1.9 s on some runs when the machine was busy |
| Tall tubes, H up to 22 | slowest about 350 ms |
| H = 30 | one board took about 1.5 s, so only correctness is tested |

So the code is comfortably fast on boards shaped like the sample (2 empty tubes). Boards with many empty tubes are the slow ones, and for those the margin to the 1 s limit is thin. If you want more margin, section 5.7 (a stronger heuristic) is the next step.

---

## 1. What is being asked

- Same puzzle, same input, same output as Problem 1: print the **minimum number of moves**.
- You **must** use an informed search (A*) with a heuristic. Any other approach is graded 0, even if it is correct and fast.
- It must run in **under 1 second on Mooshak**. The PDF says Sample 1 takes tens of seconds with uniform cost and tens of milliseconds with a good heuristic.
- "Minimum" means the heuristic must be **admissible** (never overestimates). A non-admissible heuristic can be faster but may print a number that is too big, and then you fail the test case.
- Deadline: Mooshak submission by **26 Oct 2026, 17h**. Validation by the instructor in the lab class is due by **16 Nov 2026**.

Input recap: first line `H T` (tube capacity, number of tubes), then `H` lines of `T` letters separated by spaces. The first line of the board is the **top** of the tubes. Colors are R, B, P, Y, G, and E means empty. Each color that is used appears **exactly H times**, so each color fills exactly one tube in the goal.

---

## 2. The problem as a state space

| Search concept | Ball sort |
|---|---|
| State | The full board: the contents of every tube |
| Initial state | The parsed input |
| Operator | Move the top ball of tube `i` to tube `j` |
| Legal if | `i` is non-empty, `i != j`, `j` is not full, and `j` is empty **or** the top of `j` has the same color as the ball |
| Cost | 1 per move (`getK() = 1`) |
| Goal | Every tube is either empty or full with a single color |

Things worth noticing, because they drive the design:

- **Branching factor**: at most `T·(T-1)` moves per state (30 for T=6). In practice it is much less.
- **The graph has cycles and transpositions.** Different move orders reach the same board, and a ball can move back and forth. So you need a closed set, as T2 already has.
- **Every move is reversible-ish.** A move from `i` to `j` can often be undone by `j` to `i`. This means the state space is rich in loops, and uniform cost explores a huge number of states.
- **Tubes are interchangeable.** Swapping two tubes gives a board that is different as an array but identical as a puzzle: it has exactly the same optimal cost. This symmetry (up to `T!` copies of every state) is the single biggest waste in your T2 code. See section 6.
- **Solvability.** The PDF guarantees valid inputs. Your T2 code returns `null` when `abertos` empties; keep that as a fallback, but it should never trigger.
- **Already solved input** → the answer is 0. The search handles this naturally: the first state polled is a goal.

---

## 3. Where your T2 code stands

What you can keep as-is:

- `Ilayout` (extended, see below), `BallSortLayout.children()`, `isGoal(null)`, `getK()`.
- `Main` (reading input, printing `(int) g` of the last state).
- The `State` class idea: layout + father + `g`.

What is slow or wrong for T3:

| Problem in T2 | Why it hurts | Fix (section) |
|---|---|---|
| `abertos.contains(suc)` | `PriorityQueue.contains` is a **linear scan**. Called once per successor, so the cost is O(open list size) each time. This is the dominant cost. | Use a `HashMap` for open states (7) |
| `hashCode()` = `toString().hashCode()` | Builds a new `String` (H×T chars plus a `StringBuilder`) on **every** hash lookup. `State.hashCode()` does it again via `toString()`. | Cache a hash computed once (6.3) |
| Order by `g` only | That's uniform cost — no knowledge of the goal. | `f = g + h` (4) |
| Comparator `(int) Math.signum(a - b)` | Works, but clumsy. With `f` and tie-breaking you want something explicit. | `Double.compare` / ints (7) |
| `clone()` recomputes `sizeTube` by scanning the whole board | Wasted work for each child. | Copy `sizeTube` with `clone()`/`arraycopy`, then adjust by one (7) |
| No symmetry reduction | Equivalent boards are expanded separately, up to `T!` times | Canonical form (6.1) |
| The `!e.equals(n.father.layout)` check | Redundant once there is a closed set (the father is always closed). It is not wrong, just extra work. | Can be dropped |

---

## 4. A* in depth

### 4.1 The idea

- `g(n)`: the exact cost from the start to `n` (number of moves made so far).
- `h(n)`: an **estimate** of the cheapest cost from `n` to a goal.
- `f(n) = g(n) + h(n)`: the estimated cost of the best solution **through** `n`.

A* always expands the open node with the smallest `f`. Uniform cost is the special case `h = 0`.

### 4.2 Admissible and consistent

- **Admissible**: `h(n) ≤ h*(n)` for every `n`, where `h*(n)` is the true optimal cost to a goal. With an admissible `h`, A* returns an optimal solution **if you stop when the goal is *polled* from the open list** (not when it is generated). Your T2 code already tests `isGoal` on the polled state, which is correct.
- **Consistent** (monotone): `h(n) ≤ k(n, n') + h(n')` for every move `n → n'`. Here every `k = 1`, so it reads
  `h(n) − h(n') ≤ 1` — **one move may lower h by at most 1**.
  Consistency implies admissibility (given `h(goal) = 0`), and with a consistent `h`:
  - `f` never decreases along a path.
  - The first time A* polls a state, it has already found the cheapest path to it, so a closed state never needs to be reopened.

The heuristic in section 5 is both admissible and consistent. I checked this by hand (below) and by brute force on small random boards. Even so, implement open and closed handling so that it stays **correct without** relying on consistency (section 7). It costs almost nothing and protects you if you later try a stronger, riskier heuristic.

### 4.3 Data structures

- **Open list**: a priority queue ordered by `f`. Java's `PriorityQueue` has no decrease-key, so you use **lazy deletion**: if you find a cheaper path to a state already in open, just push another entry. When you poll an entry that is stale (its state is already closed with `g ≤` the entry's `g`), skip it.
- **Best known `g` per state**: a `HashMap<Layout, Integer>` (or `Double`). This replaces `abertos.contains(...)`: look up the state, and only push the new entry if it is new or has a strictly smaller `g`.
- **Closed set**: a `HashSet` (or the same map, with a flag).

### 4.4 The loop

```
open  = PriorityQueue ordered by (f, then larger g first)
best  = HashMap<Layout, g>
push(start); best[start] = 0

while open not empty:
    cur = open.poll()
    if best[cur.layout] < cur.g: continue        // stale entry, skip
    if cur.layout.isGoal(null): return cur.g     // done, minimal
    for child in cur.layout.children():
        g2 = cur.g + 1
        if g2 < best.getOrDefault(child, INF):   // new state, or a cheaper path
            best[child] = g2
            open.add(State(child, cur, g2, h(child)))
return null
```

`Main` only prints the number, so you do not strictly need `father` and the path reconstruction. Keeping it is harmless, and useful for debugging (you can print the solution).

### 4.5 Tie-breaking

Many states share the same `f`. If you break ties toward the **larger `g`** (equivalently, smaller `h`), A* tends to dive toward the goal instead of expanding all states of equal `f` in breadth. It does not change optimality, and can change running time noticeably. Test it both ways.

### 4.6 Use ints

All costs and the heuristic are whole numbers. Using `int` for `g`, `h`, `f` avoids floating-point comparisons and is slightly faster. You can keep `getK()` returning `double` if the interface requires it, and cast.

---

## 5. The heuristic

Goal: a cheap-to-compute lower bound on the number of moves still needed.

### 5.1 Key definitions

- The **base** of a tube is the longest run of balls of the *same color* starting from the **bottom**. Example, bottom→top `P P Y B P`: the base is `P P`, size 2. A tube that is entirely one color is entirely base.
- Because moves can only take the **top** ball, a ball can never leave a tube before the balls above it have left.

### 5.2 Lemma: balls that never move form a bottom prefix

If a ball sits above a ball that moves, then it must have moved first. So in any solution, the balls of a tube that never move are a **prefix from the bottom**.

### 5.3 h1 — "not in the base"

> `h1(s) = number of balls that are not part of their tube's base`

**Why it is admissible.** Take a tube with base size `b` and a ball above the base at position `p > b`:

- The first ball above the base has a different color from the base (that's how the base ends). In the goal, every tube is single-colored, so either this ball moves or the base balls below it do. But the base balls cannot move before this ball does (the lemma). So this ball must move.
- Any ball above that one is above a ball that must move, so it must move too.

So every non-base ball moves **at least once**, and one move moves exactly one ball. Hence at least `h1` moves are needed. ∎

### 5.4 h2 — also account for duplicated bases (the one to implement)

In the goal, each color occupies **exactly one** tube, because each color has exactly `H` balls and a tube holds `H`. So for a color `c`, all the balls of color `c` that never move must sit in the same tube — the one that ends up as the `c` tube.

By the lemma, the unmoved balls of a tube are inside its base. So for color `c`:

> unmoved balls of color `c` ≤ `maxBase(c)` = the largest base, among all tubes, whose base color is `c` (0 if there is none).

Summing over colors: at most `Σ_c maxBase(c)` balls are never moved. Everything else moves at least once:

> **`h2(s) = N − Σ_c maxBase(c)`**, where `N` is the total number of balls in the board.

It dominates h1 (`h2 ≥ h1`), because h1 = `N − Σ_tubes base`, and `Σ_c maxBase(c) ≤ Σ_tubes base`. ∎

> `N` = number of non-`E` cells. You can also compute it as `(number of colors) × H` since each color appears exactly `H` times.

**Worked example, Sample 1.** Reading the tubes bottom→top (4 colors, 6 tubes, tubes 4 and 5 empty):

```
tube 1:  P P Y B P P B Y B     base = PP  (size 2, color P)
tube 2:  B P Y P Y R Y P R     base = B   (size 1, color B)
tube 3:  Y R B B R R P P Y     base = Y   (size 1, color Y)
tube 4:  (empty)
tube 5:  (empty)
tube 6:  B R Y R R Y B B R     base = B   (size 1, color B)
```

- `N = 36`.
- `h1 = 36 − (2+1+1+1) = 31`.
- `maxBase`: P → 2, B → 1 (tubes 2 and 6 both have a B base, only one can be kept), Y → 1, R → 0.
- `h2 = 36 − (2+1+1+0) = 32`.
- Optimal answer = 37. So the heuristic is off by only 5. That is the reason A* will be fast: it is very well informed.

### 5.5 Properties

- **Admissible**: proven above.
- **`h2(s) = 0` exactly when `s` is a goal.** If `h2 = 0` then every ball is unmovable-base and each color has all `H` balls in one base, which means a full single-color tube. You can use this as a unit test. For speed, keep `isGoal` as is.
- **Consistent**: `h(s) − h(s') ≤ 1` for every legal move. Since `h2 = N − Σ_c maxBase(c)` and `N` never changes (a move does not create or destroy balls), this is the same as saying **`Σ_c maxBase(c)` grows by at most 1 per move**. A move takes the top ball `x` (color `c`) from tube `i` to tube `j`, and only those two tubes change:
  - Tube `i` loses its top ball, so its base can only stay the same or shrink by 1. That can never raise `Σ maxBase`.
  - Tube `j` gains a ball on top. Its base grows by 1 only if `j` was empty or entirely color `c`; otherwise the base is unchanged (the new ball sits on a non-base ball). So at most one base grows, by exactly 1, and only for color `c`. `maxBase(c)` therefore grows by at most 1, and no other color's `maxBase` grows.

  Hence `Σ maxBase` grows by at most 1, and `h2` drops by at most 1. ∎

  I also verified admissibility and consistency by brute force on 60 random small boards (all states in the reachable space, every edge), with zero violations. Do the same in your own tests (section 8).

### 5.6 Computing it fast

- With the `sizeTube[]` you already have, the top of tube `j` is at row `H − sizeTube[j]`, the bottom at row `H − 1`. Walk from the bottom upward while the color matches the bottom ball: that's the base.
- Keep an array `maxBase` with one slot per color, sized `Color.values().length` (5) and indexed by `Color.valueOf(String.valueOf(color)).ordinal()` (CHANGE #13; `Color` is the enum `R, B, P, Y, G` in `classes/Color.java`). At the end `h = balls − (sum of the array)`. An earlier version used `new int[26]` indexed by `color − 'A'`; the enum is easier to read and fails with a clear message on an unknown letter.
- Cost is O(T·H) per state. That is fine; it's the same order as `children()`.
- Compute `h` **once** when the state is created, not at every comparison.
- If you want to go further later: the base of the tubes can be updated incrementally from the parent, since a move changes only two tubes.

### 5.7 Optional stronger heuristics (only if profiling says you need them)

If `h2` is not enough for some larger test case, you can try tightening. **Any new idea must be proven or validated against the oracle (section 8) before you trust it**, because an overestimate silently gives wrong answers.

- **Think about what forces a second move.** Admissible bounds count the moves that are *forced*. Ask: "in what situation must a ball move twice?" For example, a ball that has to leave its tube to uncover a base ball, and cannot go straight to its final tube because that tube is not ready yet (its color has no base anywhere, so it first needs an empty tube).
- **Count free capacity.** The sample has two entirely empty tubes. Boards with no empty tubes at all (T equals the number of colors) are the hardest to reason about: the first move must go on top of a ball with the same color.
- A bound that is not obviously additive with `h2` (they could count the same move) is **not** safe to add. Only add parts that count *disjoint* sets of forced moves.

The PDF only needs a "suitable heuristic". For Sample 1, a gap of 5 is already excellent.

---

## 6. Making it fast (the 1 second limit)

Do these in order and **measure after each one** (just `time` the run on Sample 1 and on your largest test).

### 6.1 Symmetry: canonical form (potentially the biggest win after A* itself)

Two boards that differ only by the order of their tubes are the same state for the puzzle. Collapse them:

- Represent each tube as a small value. **Implemented:** a `String` of its balls bottom→top, without the `E`s (CHANGE #3). A packed `long` with 3 bits per ball also works, but it is harder to read and limits H to 21, so it was not used.
- Build the state's key from the **sorted** list of tube values, joined with `,` (e.g. tubes `RB`, `B`, empty → `,B,RB`).
- Two states are `equal` iff their keys are equal, and the hash is the hash of the key.

Is it safe? Yes: the cost of the best solution does not depend on the order of tubes, because relabeling tubes maps legal moves to legal moves one-to-one. The heuristic depends only on per-tube contents, so it is also unaffected.

Two ways to implement it:

1. **Keep the actual layout as is, canonicalize only inside `equals`/`hashCode`.** Simplest and leaves `children()` and your existing tests alone, but each hash/equal call sorts the tubes (so the key is cached). **This is the option implemented.**
2. **Sort the columns when a layout is built** (in `children()` after the move, and in the constructor). Then `equals`/`hashCode` stay simple. The catch: the child layout's column order no longer matches the "tube i, tube j" you moved, and your T2 unit tests that check `children()` output may need updating.

Note: color relabeling (e.g., all R↔B) is also a symmetry, but it is harder to exploit. Skip it.

### 6.2 Pruning useless moves

- A move from a tube of size 1 to an empty tube yields the same board up to tube order. With canonical form it is rejected immediately by the `melhorG` check, so you get this for free.
- **Implemented (CHANGE #4):** in `children()`, a ball is moved to only the **first** empty tube, because moving it to a second empty tube gives the same board with two tubes swapped. The child is not even created. This is safe, and the answer does not change.
- Moving from an all-one-color tube of size > 1 to an *empty* tube is probably never useful, but I have **not** proven it. If you add it, check against the oracle on many boards.
- Avoid ad-hoc "clever" pruning without a proof; being wrong here means a wrong answer, not just a slow one.

### 6.3 Hashing

- The key (section 6.1) is built **once**, the first time it is needed, and stored in a field. It is lazy and not built in the constructor because `children()` creates a clone and then edits it; building the key in the constructor would describe the board before the move. `clone()` does not copy the cached key, so every child starts with none.
- `hashCode()` is `key().hashCode()`. A `String` remembers its own hash, so nothing is recomputed. `equals()` is `key().equals(other.key())`, which agrees with `hashCode()` by construction.
- `State.hashCode()` delegates to the layout (CHANGE #9) instead of calling `toString()`.
- Building a `String` per layout is not free, but it happens once per layout and measured fast enough (section 0), so there was no reason to use something harder to read.

### 6.4 Compact representation (only if still too slow)

- **Not needed and not done.** The `char[H][T]` board and the `String` key were fast enough.
- If you ever needed it: store `H` and `T` once and a flat `char[]`/`byte[]`, or each tube as one `long` (3 bits per ball, so H up to 21). That makes the key a sort of `T` longs, but it is a bigger rewrite, harder to read, and it limits H. Only go there if profiling shows the simple version was not enough.

### 6.5 Java specifics

- Use `ArrayDeque`/arrays over boxed collections where you can.
- Pre-size the `HashMap` (e.g. 1 << 16) to avoid repeated rehashing.
- Read the input with the `Scanner` as you do now; the input is tiny, the search is the cost.
- JVM startup is roughly 50–100 ms on its own on the judge — leave headroom.
- Watch memory. A* keeps every generated state. If a test blows the heap, the canonical form and a stronger heuristic are the cures, not a bigger limit.

---

## 7. Code changes, file by file

(Use a copy of T2 inside `T3_IA/src`, so T2 stays intact as your oracle.)

### `interfaces/Ilayout.java`

Add the heuristic to the interface, next to `isGoal`:

```java
double getH(Ilayout goal);   // estimated cost from this layout to the goal
```

It mirrors `isGoal(Ilayout l)`, which you also call with `null` for this problem. A stub returning 0 should reproduce the T2 behavior (A* with `h = 0` is uniform cost). That's a handy sanity check right after you refactor `BestFirst`.

### `classes/BallSortLayout.java`

- `getH(Ilayout goal)` implements h2, cached in `hCache` (CHANGE #6). It uses the `Color` enum to index the per-color array (CHANGE #13).
- `clone()` copies the rows and `sizeTube` with `System.arraycopy` instead of recounting every cell (CHANGE #5). `children()` already adjusts the two tubes that change (`-- / ++`). The cached `key` and `hCache` are not copied.
- `key()` (CHANGE #3) builds the sorted, comma-joined tube text once. `equals()` and `hashCode()` use it, so tube order is ignored.
- `children()` skips empty destination tubes after the first one (CHANGE #4).
- `toString()` stays for debugging and for tests; it is no longer used as the hash.

### `classes/BestFirst.java`

- `State` keeps `layout`, `father`, `g`, and now `h` and `f = g + h`, all computed in the constructor (CHANGE #8). The constructor takes the goal as a third argument because `getH` needs it.
- The queue compares `f`, then the larger `g` (CHANGE #11).
- `fechados` and `abertos.contains` are replaced by `melhorG` (CHANGE #7). A successor is added only if its layout is new or this path is strictly cheaper.
- On poll, an entry whose `g` is above `melhorG` for its layout is stale and skipped (CHANGE #12). Then `isGoal` is tested, then the state is expanded.
- `sucessores` no longer filters the father (CHANGE #10).
- The path reconstruction is kept; `Main` uses only the final `g`.

### `Main.java`

- No logic change needed. It already prints `(int) g` of the last state.
- Keep the `"no solution found"` fallback.

---

## 8. Testing and validation

### 8.1 Your oracle

For small boards you need a solver you can trust to give the true optimum; your A* must return the **same number** on every board. **Implemented:** `TestSupport.optimal`, a plain breadth-first search written from scratch in the test folder (all moves cost 1, so BFS finds the minimum). It deliberately shares no code with `BallSortLayout` or `BestFirst`, so a bug in one cannot hide a bug in the other. The T2 uniform-cost search would also work, but it uses the same `children()` and `equals()` that are being tested.

### 8.2 Test list

1. **Sample 1** → `37`, and a time check under 1 s (e.g. in a test or with `time java Main < sample1.txt`). Put the sample in a file; the T2 `reports/T01input … T10input` can also be reused as regression inputs.
2. **A* equals uniform cost** on a few hundred small random boards (e.g. H ∈ {2,3,4}, 2–3 colors, 1–2 spare tubes).
3. **Admissibility**: for every board in (2), `h(initial) ≤ optimal`. Stronger: check it for *every* state reachable in a small board.
4. **Consistency**: for every edge `s → s'` in a small state space, `h(s) − h(s') ≤ 1`.
5. **`h = 0` iff goal.**
6. **Edge cases**: already solved board (answer 0), `H = 1`, `T` equal to the number of colors (no empty tube), a single color.
7. **Canonical form**: two boards that differ only in tube order are `equals` and have the same `hashCode`; boards that differ in content are not equal.

**Where these are in the code:** items 1–7 above map to `AStarHeuristicTests` (`sample1IsFast`, `astarMatchesReferenceOnRandomBoards`, `hNeverAboveOptimalOnRandomBoards`, `hConsistentAndZeroOnlyAtGoalsOnWholeStateSpace`, the edge-case tests, `equalsIgnoresTubeOrder`). The reference solver is `TestSupport.optimal` (a plain BFS that shares no code with the classes under test).

### 8.3 Generating solvable random boards

A random shuffle of balls can be unsolvable. Instead, **scramble backwards from the solved board**:

- A forward move takes the top ball `x` of tube `i` and puts it on tube `j`, legal when `j` is empty or its top equals `x`.
- The reverse of that move takes the top ball `x` of tube `j` and puts it on any non-full tube `i`, and is valid only if the ball *below* `x` in `j` is also `x` (or `x` was alone in `j`), so that the forward move would have been legal.
- Start from the solved board, apply many random valid reverse moves, and you get a solvable board by construction. The number of reverse moves also gives you an upper bound on the optimum.

(If the board has no empty space at all, no reverse move exists; handle that case, it is already solved.)

**Caveat found while testing:** boards made by random reverse moves are *easy*, even after hundreds of steps (cost around 11–24, while Sample 1 has cost 37), because the random walk wanders near the solved board. For realistic timing, also use fully shuffled boards (shuffle all balls into the tubes and leave the extra tubes empty). With 2 empty tubes these are almost always solvable; with 1 empty tube most are not.

**Used by `StressTests`:** `TestSupport.scrambled` (always solvable) for the comparison with the reference solver, and `TestSupport.shuffled` for the timing tests and for the boards with no solution. A solution step is checked by `TestSupport.isLegalMove`, which has its own rules (one tube loses its top ball, another gains that ball, and it was empty or had the same color on top and was not full).

### 8.4 Quick timing harness

Time a few of the largest boards you can generate (H up to 9 or more, 4–5 colors, 1–2 extra tubes) and watch both time and number of expanded states. Print the count of expanded and generated states while developing. It's the best way to see whether a change (tie-break, canonical form, stronger `h`) helps.

---

## 9. Pitfalls checklist

- [ ] The goal test is done when a state is **polled**, not when generated.
- [ ] `h` is admissible. Never add a term you have not argued, and never use `h1 + h2`-like sums (they double count).
- [ ] Stale open entries are skipped on poll.
- [ ] A cheaper path to a state seen before is not ignored.
- [ ] `equals` and `hashCode` agree. If `equals` is canonical (order-independent), `hashCode` must be too.
- [ ] The cached key is computed after the layout is fully built (lazily), not before the move is applied in a clone, and `clone()` does not copy it.
- [ ] The key is built once per layout, not on every `equals`/`hashCode` call.
- [ ] The heuristic only knows the colors in `Color` (R, B, P, Y, G). Any other letter in a tube (apart from `E` for empty) makes `getH` throw `IllegalArgumentException`. If a new color is ever needed, add it to the enum.
- [ ] Heuristic uses `int` arithmetic and `H − sizeTube[j]` to find the top, matching your existing indexing: row 0 is the **top** of the board and row `H−1` is the **bottom**.
- [ ] `Main` still handles `H`/`T` and the E-padding exactly as before.
- [ ] Output is just the number followed by a newline, nothing else.
- [ ] Package layout compiles the way Mooshak expects (same as T2: `Main` at the root, `classes/` and `interfaces/`).

---

## 10. Order of work (and what was done)

1. Copy T2 into T3; get it compiling and the T2 tests passing. *(done)*
2. **Write the tests first** (reference solver, heuristic values, consistency, symmetry, edge cases, timing) and run them against the T2 search with a stub `getH` returning 0: 7 failed, as expected. *(done)*
3. Add `getH` to `Ilayout`. Rewrite `BestFirst` as A\* with the best-`g` map. *(done)*
4. Implement h2 and check Sample 1 → 37. *(done)*
5. Cache the key/hash; remove `toString()` from hashing; fix `clone()`; add the tube-order-independent key. *(done)*
6. Run everything: 75 tests pass (unit, heuristic, edge to edge and stress). *(done)*
7. Time the biggest boards. Only if needed: tie-breaking experiments, a compact representation, a stronger heuristic. *(not needed so far)*
8. Submit to Mooshak, then get it validated in the lab class before 16 Nov. *(yours to do)*
