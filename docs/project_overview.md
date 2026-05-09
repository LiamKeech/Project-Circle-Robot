# Project Overview: Neuroevolution for Khepera Circle Behaviour

## Abstract

This project implements a **neuroevolution** system that evolves the weights of a feedforward neural network (FFNN) to control a simulated Khepera differential-drive robot. The goal is for the robot to drive in a circle, rewarded through a behavioural fitness function rather than supervised error. The genetic algorithm (GA) treats every set of FFNN weights as a chromosome. Each individual is evaluated by running the robot inside `KheperaSimulator` for a fixed number of steps (N), where the FFNN fires once per step to produce a motor command. The resulting trajectory is then scored by the fitness function, and the best individuals are carried forward into the next generation.

---

## System Architecture

The neuroevolution system joins two pre-existing subsystems that were previously separate:

| Subsystem | Origin | Role in Neuroevolution |
|---|---|---|
| `FFNN` | `src/EvolutionaryRobotics/FFNN.java` | The controller — maps (x, y) → (leftSpeed, rightSpeed, time) |
| `Chromosome` | `src/EvolutionaryRobotics/Chromosome.java` | Genome — a flat `double[]` of all NN weights and biases |
| `EvolutionaryAlgorithm` | `src/EvolutionaryRobotics/EvolutionaryAlgorithm.java` | GA operators — selection, crossover, mutation, elitism |
| `KheperaSimulator` | `src/Code/KheperaSimulator.java` | Physics — executes commands and returns robot states |
| Fitness Functions | `Code/DistanceScaledFitnessFunction.java` etc. | Scoring — rewards circular clockwise traversal |

The key design decision is that fitness is **behavioural, not supervised**. The FFNN is never trained against a target dataset; instead, its weights are shaped purely by how well the robot traverses a circle in simulation.

---

## Component Descriptions

### `FFNN.java` — The Neural Network Controller

The FFNN is a compact three-layer feedforward network with fixed architecture:

- **Input layer:** 2 nodes — normalised `x` and `y` position of the robot
- **Hidden layer:** 5 nodes with sigmoid activation
- **Output layer:** 3 nodes with sigmoid activation — normalised `leftSpeed`, `rightSpeed`, and `time` (command duration)

**Forward pass:**

Given input vector **x** ∈ ℝ²:

1. Hidden pre-activations: `h_j = bias_j + Σ x_i · w_ih[i][j]`
2. Hidden activations: `z_j = sigmoid(h_j)`
3. Output pre-activations: `o_k = bias_k + Σ z_j · w_ho[j][k]`
4. Output activations: `y_k = sigmoid(o_k)`

All outputs are in (0, 1) due to sigmoid. They are denormalised before being passed to the simulator:
- `leftSpeed = y[0] × MAX_SPEED` (where MAX_SPEED = 14000)
- `rightSpeed = y[1] × MAX_SPEED`
- `time = y[2] × MAX_DURATION` (where MAX_DURATION = 100ms, clamped to simulator limits)

**Genome layout (`setChromosome`):**

The chromosome is consumed sequentially to populate the network:

```
Index range        Contents
0 .. (2×5)-1       Input-to-hidden weights [input][hidden]
10 .. 14           Hidden biases [hidden]
15 .. 29           Hidden-to-output weights [hidden][output]
30 .. 32           Output biases [output]
```

Total chromosome length: `(2×5) + 5 + (5×3) + 3 = 33 genes`

---

### `Chromosome.java` — The Genome

A `Chromosome` wraps a `double[] genes` array of length 33 (the full weight/bias set of the FFNN) and a scalar `fitness` value.

Key methods:

- `createRandom(int length)` — initialises all genes uniformly in `[-1, 1]` using `Random.nextDouble(-1, 1)`
- `deepCopy()` — produces a fully independent copy via `Arrays.copyOf`, used by elitism so the original is not modified
- `setFitness(double)` / `getFitness()` — fitness is stored in the chromosome so the GA can sort by it

In the neuroevolution context, genes are continuous real values and no clamping to speed/time units is needed here (that happens when the FFNN output is denormalised at evaluation time).

---

### `EvolutionaryAlgorithm.java` — The GA Operators

All evolutionary operators work on `Chromosome[]` arrays. Fitness is **maximised** (higher is better), consistent with the reward-based fitness functions.

**`sortByFitness`**
Sorts the population in descending order of fitness using `Comparator.comparingDouble(...).reversed()`. This places the best individuals at index 0.

**`elitism(population, eliteCount)`**
Selects the top `eliteCount` chromosomes (already sorted) and returns `deepCopy()` instances. This guarantees the best solution found so far cannot be lost to crossover or mutation.

**`tournamentSelect(population, tournamentSize)`**
Randomly samples `tournamentSize` candidates from the population and returns the one with the highest fitness. A larger tournament size increases selection pressure (fewer weak individuals survive).

**`crossover(parent1, parent2)`**
Performs uniform crossover: for each of the 33 gene positions, the offspring independently takes the gene from `parent1` or `parent2` with 50% probability. This produces a single offspring per call.

**`GaussianMutation(chromosome, mutationRate, mutationStrength)`**
Iterates over each gene. With probability `mutationRate`, a perturbation drawn from `N(0, mutationStrength²)` is added to the gene. The result is clamped to `[-1, 1]` to keep weights in a stable range.

---

### Fitness Functions — Behavioural Scoring

Three fitness functions are available, all operating on an `ArrayList<KheperaState>` trajectory produced by the simulator. All use the same **clockwise 3×3 grid traversal** concept and are **maximisation** objectives (higher = better).

The arena is divided into a 3×3 grid of cells (each cell ≈ 20×20 units), identified by IDs 0–8, with cell 4 being the centre. The target clockwise order is: `{0, 1, 2, 5, 8, 7, 6, 3, 0}`.

---

#### `DistanceScaledFitnessFunction` *(recommended for neuroevolution)*

Rewards the robot for visiting grid cells in the correct clockwise order, penalises it for entering the centre cell (ID 4) or leaving the grid entirely.

```
For each state in trajectory:
  if outside grid:       fitness -= outsidePenalty × (1 + distanceOutside / gridWidth)
  if in centre cell:     fitness -= centrePenalty  × (1 - distanceToCentre / cellWidth)
  if correct next cell:  fitness += cellReward
                         nextCellIdx++
  if full traversal:     fitness += traversalReward; return early
```

Constants: `cellReward = 20`, `traversalReward = 300`, `centrePenalty = 25`, `outsidePenalty = 50`.
Maximum possible score: `(9 × 20) + 300 = 480`.

---

#### `CheckpointFitnessFunction`

Extends `DistanceScaledFitnessFunction` with an additional **proximity bonus** at every step: the robot is rewarded proportionally to how close it currently is to the next target cell's centre. This provides a dense gradient signal that helps the GA explore more effectively in early generations.

---

#### `FlatFitnessFunction`

The simplest variant: fixed rewards/penalties per state with no distance scaling. Useful as a baseline but provides a coarser gradient for the GA.

---

#### `TimeTrialFitnessFunction`

Like `CheckpointFitnessFunction` but adds a **speed bonus** when a checkpoint is reached: arriving at a cell earlier in the N-step sequence yields a higher reward. This encourages compact, efficient circular paths.

---

### `KheperaSimulator.java` — The Physics Engine

`KheperaSimulator` is the bridge between the FFNN controller and the physical world. It is responsible for:

1. Maintaining robot state (`State`: x, y, θ) across a sequence of commands
2. Computing new states via `MotionSimulatorPatched`, which uses three trained Encog networks (`NNxuse.eg`, `NNyuse.eg`, `NNtuse.eg`) to predict differential-drive kinematics
3. Optionally computing sensor readings via `SensorReadingSimulator`
4. Returning `ArrayList<KheperaState>` — one entry per executed command, containing position and sensor data

**Important:** `KheperaSimulator` is **stateful**. A fresh instance must be created for each individual evaluation to ensure the robot always starts from the same position (`State(-20, 20, 270)`).

---

## The N-Step Evaluation Loop

This is the core of the neuroevolution system. For each individual (set of NN weights), the following procedure is carried out:

```
given: chromosome (33 genes), N (number of steps)

1.  Load genes into FFNN via ffnn.setChromosome(genes)
2.  Create a fresh KheperaSimulator at START_STATE (-20, 20, 270°)
3.  currentX = START_STATE.sx
    currentY = START_STATE.sy

4.  For step = 1 to N:
      a. Normalise position:
             normX = currentX / MAX_POS    (MAX_POS = 35.0)
             normY = currentY / MAX_POS

      b. Fire FFNN:
             double[] output = ffnn.fire(new double[]{normX, normY})
             // output[0] = normalised leftSpeed  (0–1)
             // output[1] = normalised rightSpeed (0–1)
             // output[2] = normalised duration   (0–1)

      c. Denormalise outputs:
             leftSpeed  = clamp(output[0] × MAX_SPEED,  MIN_SPEED, MAX_SPEED)
             rightSpeed = clamp(output[1] × MAX_SPEED,  MIN_SPEED, MAX_SPEED)
             duration   = clamp(output[2] × MAX_DURATION, MIN_TIME, MAX_TIME)

      d. Build and execute command:
             Command cmd = new Command(leftSpeed, rightSpeed, duration)
             ArrayList<KheperaState> states = sim.getKheperaState(commands_so_far)

      e. Read new robot position from latest KheperaState:
             currentX = states.getLast().position.sx
             currentY = states.getLast().position.sy

5.  Pass full states list to fitness function:
        double fitness = DistanceScaledFitnessFunction.evaluate(states)

6.  chromosome.setFitness(fitness)
```

Speed clamping constants (from `Code/Chromosome.java`):
- `MIN_SPEED = 8000`, `MAX_SPEED = 17500`
- `MIN_TIME = 300`, `MAX_TIME = 3100`

**Why N steps?** A fixed step count per individual makes evaluation time predictable and comparable across the population. The value of N should be large enough that the robot can complete at least one full clockwise traversal of the grid (empirically, N = 10–20 is sufficient given the command durations involved).

---

## The Evolutionary Loop

```
Initialise population of P chromosomes with random genes in [-1, 1]

For generation = 1 to G:

    // Evaluate
    For each chromosome in population:
        run N-step simulation → score with fitness function
        chromosome.setFitness(score)

    // Sort (descending — best first)
    EvolutionaryAlgorithm.sortByFitness(population)

    // Log best fitness
    Print generation, population[0].getFitness()

    // Check termination
    If population[0].getFitness() >= 480.0:  break (full traversal achieved)

    // Build next generation
    nextGen[0..ELITE_COUNT-1] = elitism(population, ELITE_COUNT)  // deepCopy
    For i = ELITE_COUNT to P-1:
        parent1 = tournamentSelect(population, TOURNAMENT_SIZE)
        parent2 = tournamentSelect(population, TOURNAMENT_SIZE)
        offspring = crossover(parent1, parent2)
        GaussianMutation(offspring, MUTATION_RATE, MUTATION_STRENGTH)
        nextGen[i] = offspring

    population = nextGen

// Visualise best result
Collect trajectory states from best chromosome
Display via VisualFrame
```

### Recommended Hyperparameters

| Parameter | Value | Rationale |
|---|---|---|
| Population size | 150 | Balances diversity with evaluation speed |
| Generations | 200 | Sufficient for convergence on circle |
| N steps per individual | 10–20 | Enough to complete one full traversal |
| Elite count | 2 | Preserves top solutions without stagnation |
| Tournament size | 5 | Moderate selection pressure |
| Mutation rate | 0.15 | Per-gene probability |
| Mutation strength | 0.2 | Gaussian σ for weight perturbation |
| Fitness function | `DistanceScaledFitnessFunction` | Best gradient signal for circular behaviour |

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     EVOLUTIONARY LOOP                       │
│                                                             │
│  Population of Chromosomes (each = 33 NN weights)          │
│         │                                                   │
│         ▼                                                   │
│  ┌─────────────────────────┐                                │
│  │   N-Step Evaluation     │  ← runs once per individual   │
│  │                         │                                │
│  │  (x,y) ──► FFNN.fire()  │                                │
│  │         ──► Command     │                                │
│  │         ──► Simulator   │                                │
│  │         ──► new (x,y)   │                                │
│  │  repeat N times         │                                │
│  └─────────────────────────┘                                │
│         │                                                   │
│         ▼                                                   │
│  FitnessFunction.evaluate(states)  →  score                 │
│         │                                                   │
│         ▼                                                   │
│  Sort → Elitism → Tournament → Crossover → Mutation         │
│         │                                                   │
│         └──────────────────────────► Next Generation        │
└─────────────────────────────────────────────────────────────┘
```

---

## What to Reuse vs. What to Build

| Component | Action | Notes |
|---|---|---|
| `FFNN.java` | **Reuse as-is** | Already correct architecture (2 in, 5 hidden, 3 out) |
| `Chromosome.java` (EvolutionaryRobotics) | **Reuse as-is** | Genome = 33 real-valued genes |
| `EvolutionaryAlgorithm.java` (EvolutionaryRobotics) | **Reuse as-is** | All operators already implemented |
| `DistanceScaledFitnessFunction.java` | **Reuse as-is** | Core scoring logic, no changes needed |
| `KheperaSimulator.java` | **Reuse as-is** | Instantiate fresh per individual |
| `Code/Main.java` | **Adapt** | Replace `Chromosome.createCommands()` with FFNN N-step loop |
| `Code/Chromosome.java` | **Do not use** | This encodes raw commands, not NN weights |
| `DataLoader` / `predicted_outputs.csv` | **Not needed** | Neuroevolution does not use supervised training data |
| `VisualFrame.java` | **Reuse as-is** | Visualise best individual's trajectory after evolution |

---

## Key Differences from the Previous Systems

| Aspect | Supervised NN (`src/Code/Main.java`) | Direct GA (`Code/Main.java`) | Neuroevolution (this system) |
|---|---|---|---|
| What evolves | NN weights (to minimise MSE) | Raw motor commands | NN weights (to maximise fitness) |
| Fitness signal | Mean squared error vs. CSV data | Behavioural (grid traversal) | Behavioural (grid traversal) |
| Simulator used? | No (offline, dataset only) | Yes | Yes |
| Genome | `double[]` of 33 weights | `int[]` of speeds/durations | `double[]` of 33 weights |
| Generalisation | Limited to seen positions | None | Generalises — the NN can react to any (x, y) |

The neuroevolution approach is more powerful than the direct GA because the FFNN is a **closed-loop controller**: it reacts to the robot's actual position at every step, meaning if the robot drifts slightly off the ideal circle, the NN can correct its commands in the next step. The direct GA, by contrast, fires a fixed command sequence blindly regardless of where the robot actually ends up.