Project Title & Abstract

**Khepera Circle — Evolutionary Neural Control for a Simulated Khepera Robot**

Abstract
-------
This repository implements a physics-lite simulator for a Khepera-style differential-drive robot together with a compact feedforward neural network (FFNN) controller and a simple evolutionary algorithm (EA) for evolving network parameters (weights and biases). The project is organized to support two complementary workflows:

- Evolve networks by encoding an entire FFNN as a single linear chromosome of real-valued genes, then using selection, crossover, and Gaussian mutation to minimize a supervised error or behavioral fitness.
- Run trained networks (from Encog `.eg` persistence files) to produce next-state predictions that can drive the simulator.

The system is primarily academic: it demonstrates genotype→phenotype mapping (linear arrays → network parameters), fitness-based selection, and closed-loop evaluation of control policies in a simulated environment with sensors, collision checks, and a visualizer.

**Key goals**: evolve controllers that produce wheel speed commands to trace circular trajectories; provide utilities to train, persist, load, and execute networks; and provide CSV-based data handling for training / prediction workflows.

**Primary languages and frameworks**: Java 8+, with optional Encog (encog-core-3.4.jar) for persistence and pretrained networks.

**Architecture & Design**

High-level architecture
-----------------------
The project separates concerns into three dominant layers:

1. Simulator & IO (physics, sensors, visualization)
   - Responsible for representing robot and world state, applying motor commands, and producing sensor observations.
2. Control / Model (FFNN, Encog networks)
   - Implements the phenotype: feedforward neural networks that map sensor/command inputs to outputs (either motor commands or delta-state predictions).
3. Evolutionary algorithm & data handling
   - Implements genotype operations (creation, crossover, mutation, selection) and dataset parsing for supervised fitness evaluation.

Interaction flow
----------------
- A run/evaluation begins with a `Chromosome` (genotype): a 1‑D array of doubles representing every weight and bias in a `FFNN`.
- The EA constructs candidate populations of `Chromosome` instances and maps each to an `FFNN` with `setChromosome(...)`.
- Each `FFNN` is executed (fired) against input vectors (from training data or simulated sensor readings) to compute outputs; a per-sample error is computed with `calculateError(...)` and aggregated into a fitness score stored on the `Chromosome`.
- Evolutionary operators (`tournamentSelect`, `crossover`, `GaussianMutation`, `elitism`) produce the next generation.
- Trained networks can be persisted via Encog or used directly to generate `predicted_outputs.csv`, which `RunNetwork` can load and feed to `KheperaSimulator` for visualization.

Design patterns and modularity
-----------------------------
- Genotype/Phenotype mapping: `Chromosome` ⇄ `FFNN` is explicit and single-responsibility: the chromosome stores genes; `FFNN` consumes a gene array and maps values into weight/bias matrices.
- Strategy-like separation of motion and sensing: `MotionSimulatorPatched` computes kinematic updates; `SensorReadingSimulator` computes distance readings and line sensors. `KheperaSimulator` orchestrates the two to produce `KheperaState` snapshots.
- File-based persistence / dataflow: CSV loaders and Encog persistence are used for interchange formats, enabling offline training, evaluation, and visualization.

Directory & File Breakdown

Repository tree (abridged)

```
KheperaCircleNN/
├─ data/
│  ├─ circle_training_data.csv
│  ├─ learning_curve.csv
│  └─ predicted_outputs.csv
├─ docs/
│  └─ project_overview.md
├─ src/
│  ├─ Code/
│  │  ├─ KheperaSimulator.java
│  │  ├─ KheperaState.java
│  │  ├─ MotionSimulatorPatched.java
│  │  ├─ SensorReadingSimulator.java
│  │  ├─ RunNetwork.java
│  │  ├─ NNSim.java
│  │  ├─ VisualFrame.java
│  │  └─ (other UI / helper classes)
│  ├─ DataHandler/
│  │  ├─ DataLoader.java
│  │  └─ DataPoint.java
│  └─ EvolutionaryRobotics/
│     ├─ Chromosome.java
│     ├─ EvolutionaryAlgorithm.java
│     └─ FFNN.java
└─ lib/
   └─ encog-core-3.4.jar (expected runtime dependency)
```

Key files and responsibilities
-------------------------------
- `src/Code/KheperaSimulator.java` ([src/Code/KheperaSimulator.java](src/Code/KheperaSimulator.java)) — Orchestrator for simulation episodes. Maintains world obstacles, a start `State`, and a list of `Command`s. Uses `MotionSimulatorPatched` to propagate kinematics and `SensorReadingSimulator` to produce sensor arrays. Produces `KheperaState` snapshots containing both position and sensor readings for each executed command.

- `src/EvolutionaryRobotics/FFNN.java` ([src/EvolutionaryRobotics/FFNN.java](src/EvolutionaryRobotics/FFNN.java)) — Compact feedforward neural network implementation with a fixed architecture: 2 inputs, 5 hidden neurons, and 3 outputs. Provides methods to compute the chromosome length (`getChromosomeLength()`), map a linear gene array into weight and bias matrices (`setChromosome(...)`), evaluate the network (`fire(...)`), and compute mean squared error (`calculateError(...)`). Activation function: logistic sigmoid $\\sigma(x)=1/(1+e^{-x})$.

- `src/EvolutionaryRobotics/Chromosome.java` ([src/EvolutionaryRobotics/Chromosome.java](src/EvolutionaryRobotics/Chromosome.java)) — Simple container for a `double[] genes` and `fitness` scalar. Provides factory `createRandom(int)` to initialize genes uniformly in $[-1,1]$, getter/setter for fitness, and `deepCopy()` utility used for elitism.

- `src/EvolutionaryRobotics/EvolutionaryAlgorithm.java` ([src/EvolutionaryRobotics/EvolutionaryAlgorithm.java](src/EvolutionaryRobotics/EvolutionaryAlgorithm.java)) — Implements core EA operators:
  - `sortByFitness(...)` — ascending fitness (lower is better) sort.
  - `elitism(...)` — copies top-N chromosomes into next generation via `deepCopy()`.
  - `tournamentSelect(...)` — selects the best of `tournamentSize` randomly sampled candidates.
  - `crossover(...)` — uniform crossover producing an offspring by selecting each gene from one of two parents at random.
  - `GaussianMutation(...)` — per-gene mutation with probability `mutationRate`, adding `N(0, mutationStrength^2)` and clamping genes to $[-1,1]$.

- `src/DataHandler/DataLoader.java` ([src/DataHandler/DataLoader.java](src/DataHandler/DataLoader.java)) — CSV reader for training/prediction data. Reads rows (skipping header), parses `(x, y, left, right, duration)` and normalizes them with constants `MAX_POS`, `MAX_SPEED`, `MAX_DURATION` into a `DataPoint` list used for supervised training/evaluation.

- `src/Code/RunNetwork.java` ([src/Code/RunNetwork.java](src/Code/RunNetwork.java)) — Example runner that reads `data/predicted_outputs.csv`, denormalizes wheel speeds and duration back to physical integers and dispatches the resulting `Command` sequence to a `KheperaSimulator` to produce a visual path using `VisualFrame`.

- `src/Code/NNSim.java` ([src/Code/NNSim.java](src/Code/NNSim.java)) — Utility that loads three Encog serialized networks (`NNxuse.eg`, `NNyuse.eg`, `NNtuse.eg`) and uses them to compute 3 scalar deltas that are scaled into physical displacements. Demonstrates an orthogonal workflow where Encog-trained networks produce deltas instead of the custom `FFNN` genotype.

Core Mechanics & Algorithms

1) Genotype → Phenotype mapping (FFNN and `Chromosome`)
------------------------------------------------------
The `FFNN` uses the following architecture constants: `numInputs = 2`, `numHidden = 5`, `numOutputs = 3`. The gene vector layout used by `FFNN.setChromosome(double[] genes)` is:

1. Input-to-hidden weights: `numInputs * numHidden` (matrix indexed `[input][hidden]`)
2. Hidden biases: `numHidden` (vector indexed by hidden neuron)
3. Hidden-to-output weights: `numHidden * numOutputs` (matrix indexed `[hidden][output]`)
4. Output biases: `numOutputs` (vector indexed by output neuron)

The total chromosome length is therefore:

$$L = n_{in} \\cdot n_{hid} + n_{hid} + n_{hid} \\cdot n_{out} + n_{out}$$

For the constants in this project: $L = 2\\cdot5 + 5 + 5\\cdot3 + 3 = 10 + 5 + 15 + 3 = 33$ genes.

When `setChromosome(...)` is called, the code sequentially consumes genes and populates the internal arrays `hiddenWeights`, `hiddenBiases`, `outputWeights`, and `outputBiases` in row-major order matching the network topology.

2) Forward pass / activation
----------------------------
Given an input vector $x\\in\\mathbb{R}^{n_{in}}$, the network computes hidden pre-activations:

$$h_j = b^{(h)}_j + \\sum_{i=1}^{n_{in}} x_i \\cdot w^{(ih)}_{i,j}$$

Hidden activations use the logistic sigmoid $\\sigma$: $z_j = \\sigma(h_j)$. Output pre-activations and activations are computed similarly:

$$o_k = b^{(o)}_k + \\sum_{j=1}^{n_{hid}} z_j \\cdot w^{(ho)}_{j,k}, \\quad y_k = \\sigma(o_k).$$

`FFNN.fire(...)` returns `y \\in \\mathbb{R}^{n_{out}}`.

3) Error metric and fitness
---------------------------
`FFNN.calculateError(predicted, expected)` computes per-sample mean squared error (MSE):

$$\\text{MSE} = \\frac{1}{m} \\sum_{i=1}^m (t_i - y_i)^2$$

The project uses this MSE as the fitness measure in a minimization sense (lower is better). During EA evaluation each `Chromosome` is mapped to an `FFNN` and run on one or more training examples; the sample-wise MSEs are aggregated (sum or mean depending on the evaluation harness) and assigned to `chromosome.setFitness(...)`.

4) Evolutionary loop
---------------------
Typical EA iteration uses the following steps (matchers in `EvolutionaryAlgorithm`):

- Evaluate current population: compute fitness for each `Chromosome`.
- Elitism: `elitism(population, eliteCount)` selects top `eliteCount` chromosomes (lowest fitness) and carries exact `deepCopy()` copies into the next population.
- Offspring generation: repeatedly use `tournamentSelect(population, tournamentSize)` to select parents, apply `crossover(parent1,parent2)` to create offspring, then apply `GaussianMutation(offspring, mutationRate, mutationStrength)`.
- Replacement: combine elites + offspring to form new population; sort by fitness for bookkeeping.

Operator specifics implemented here
----------------------------------
- Tournament selection: repeated random sampling and pick the best. Tournament size trades selection pressure (larger increases selection pressure).
- Uniform crossover: for each gene index the offspring takes value either from `parent1` or `parent2` with 50% chance.
- Gaussian mutation: for each gene, with probability `p = mutationRate`, add $\\mathcal{N}(0, \\sigma^2)$ where $\\sigma =$ `mutationStrength`, then clamp the gene to $[-1,1]`.

These operators implement a standard real-valued GA suitable for optimizing continuous parameters.

5) Simulator motion & state update
----------------------------------
Motion and sensing are split across dedicated collaborators:

- `MotionSimulatorPatched` — computes the deterministic update of the robot `State` given a previous `State` and a `Command` (left wheel speed, right wheel speed, duration). The `KheperaSimulator` uses `msp.getMovement(prevState, command)` to obtain a new `State`. The motion model is differential-drive: linear and angular increments are integrated (the code lives in `MotionSimulatorPatched.java`). The visualizer consumes these `State` objects for display.

- `SensorReadingSimulator` — given the robot's $(x,y,\\theta)$ and the environment obstacle list, it computes N simulated distance readings and simple line-under-sensor detection values. Distances are returned as integer-like doubles (the code uses sentinel ranges like >3500/3600 to indicate collisions). Sensor geometry is controlled by parameters such as `sensorAngle`, `obstacleDangerDistance`, and `robotRadius` in `KheperaSimulator`.

Collision detection
-------------------
The simulator performs collision checks both at discrete states and along interpolated trajectories to avoid tunneling. `KheperaSimulator.checkCollisionOnPath(...)` subdivides a `Command` duration into `numInterpolations` steps, queries `sim.getPath(...)` from `MotionSimulatorPatched` and checks each interpolated state for proximity to obstacles or sensor-indicated collisions.

Data Handling

CSV inputs and normalization
---------------------------
`DataLoader.loadData(...)` reads `circle_training_data.csv` (expected columns: x, y, left, right, duration), skips the header, and normalizes values using fixed maxima:

- Position normalization: divide by `MAX_POS = 35.0` → $x_{norm} = x / 35.0$.
- Wheel speed normalization: divide by `MAX_SPEED = 14000.0` → $v_{norm} = v / 14000.0$.
- Duration normalization: divide by `MAX_DURATION = 100.0` → $t_{norm} = t / 100.0$.

These normalized datapoints are stored as `DataPoint` instances and are the inputs/targets when evaluating an `FFNN` for supervised training or fitness calculation.

Predictions and playback
------------------------
The pipeline for playback is:

1. Use an evolved `FFNN` or an Encog-trained network to produce `predicted_outputs.csv` (columns match training format: x,y,left,right,duration but left/right/duration are predicted/normalized).
2. `RunNetwork` reads `predicted_outputs.csv`, denormalizes wheel speeds and duration back to integer physical units and creates `Command` objects.
3. `KheperaSimulator` executes these commands to produce states and a `VisualFrame` is used to animate the driven path.

Required third-party components
------------------------------
- Encog Core 3.4: `encog-core-3.4.jar` — used by `NNSim` and any other code that loads/saves Encog `BasicNetwork` objects.
