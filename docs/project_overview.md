# Project Overview: Khepera Circle NN

## Objective
Develop a feedforward neural network (FFNN) in pure Java that controls a Khepera robot simulator to drive in a circular path. The network will be trained using an evolutionary algorithm to map the robot's current position to motor commands.

## Data Source
* The network will be trained on synthetically generated circular path data.
* Format: `[x, y, left_speed, right_speed, time]`

## Neural Network Architecture
* **Input Layer**: 2 neurons (Current X position, Current Y position)
* **Hidden Layer**: Configurable number of neurons
* **Output Layer**: 3 neurons (Left wheel speed, Right wheel speed, Time duration)
* **Activation Function**: Sigmoid for hidden layer
* **Weights & Biases**: Initialized randomly and evolved over generations. Must be extractable into a 1D `double[]` array for the weights of each layer so the evolutionary algorithm can mutate them.

## Evolutionary Algorithm
* **Population**: A set of neural networks with different weight configurations (initially random).
* **Fitness Function**: Pass the circular path data through each FFNN. Calculate the Mean Squared Error (MSE) between the network's predictions and the target outputs. Lower error = Higher fitness.
* **Selection**: Keep the top performing networks (elitism).
* **Crossover**: Combine the weights of two parent networks.
* **Mutation**: Randomly alter some weights in the offspring to introduce variation.
* **Generations**: Repeat the process for a set number of generations.
* **Validation**: Test the best evolved network in the Khepera simulator to verify circular motion.

## Implementation Details
* The FFNN and evolutionary algorithm will be implemented in Java without using any external ML libraries.
* The code will be modular, with separate classes for the FFNN, evolutionary algorithm, data generation, and simulator integration.
* Normalization utilities will scale inputs to `[-1, 1]` and outputs to appropriate motor/time ranges.