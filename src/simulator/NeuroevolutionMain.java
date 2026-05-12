package simulator;

import fitness.TimeTrialFitnessFunction;
import ga.Chromosome;
import ga.EvolutionaryAlgorithm;
import nn.FFNN;

import java.util.ArrayList;

public class NeuroevolutionMain {
    private static final int POPULATION_SIZE = 150;
    private static final int GENERATIONS = 200;
    private static final int ELITE_COUNT = 2;
    private static final int TOURNAMENT_SIZE = 5;
    private static final double MUTATION_RATE = 0.15;
    private static final double MUTATION_STRENGTH = 0.2;
    private static final int STEPS_PER_INDIVIDUAL = 15;
    private static final double TARGET_FITNESS = 480.0;

    private static final double MAX_POS = 35.0;
    private static final int MIN_SPEED = 8000;
    private static final int MAX_SPEED = 17500;
    private static final int MIN_TIME = 300;
    private static final int MAX_TIME = 3100;

    private static final State START_STATE = new State(-20, 20, 270);

    public static void main(String[] args) {
        FFNN ffnn = new FFNN();
        Chromosome[] population = new Chromosome[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = Chromosome.createRandom(ffnn.getChromosomeLength());
        }

        for (int gen = 0; gen < GENERATIONS; gen++) {
            // 1. Evaluate individuals
            for (Chromosome chromosome : population) {
                double fitness = evaluateChromosome(chromosome, ffnn);
                chromosome.setFitness(fitness);
            }

            // 2. Sort by fitness
            EvolutionaryAlgorithm.sortByFitness(population);
            Chromosome best = population[0];

            System.out.println("Generation " + gen + " | Best Fitness: " + best.getFitness());

            if (best.getFitness() >= TARGET_FITNESS) {
                break;
            }

            //3. Create the next generation
            population = nextGeneration(population);
        }

        EvolutionaryAlgorithm.sortByFitness(population);
        Chromosome best = population[0];
        ArrayList<KheperaState> bestStates = runTrajectory(best, ffnn);
        showTrajectory(bestStates, "Best Fitness: " + best.getFitness());
    }

    private static Chromosome[] nextGeneration(Chromosome[] currentPopulation) {
        Chromosome[] nextGen = new Chromosome[POPULATION_SIZE];
        Chromosome[] elites = EvolutionaryAlgorithm.elitism(currentPopulation, ELITE_COUNT);
        System.arraycopy(elites, 0, nextGen, 0, ELITE_COUNT);

        for (int i = ELITE_COUNT; i < currentPopulation.length; i++) {
            Chromosome parent1 = EvolutionaryAlgorithm.tournamentSelect(currentPopulation, TOURNAMENT_SIZE);
            Chromosome parent2 = EvolutionaryAlgorithm.tournamentSelect(currentPopulation, TOURNAMENT_SIZE);

            Chromosome offspring = EvolutionaryAlgorithm.crossover(parent1, parent2);
            EvolutionaryAlgorithm.GaussianMutation(offspring, MUTATION_RATE, MUTATION_STRENGTH);
            nextGen[i] = offspring;
        }

        return nextGen;
    }

    private static double evaluateChromosome(Chromosome chromosome, FFNN ffnn) {
        /*
            Simulate the trajectory for a chromosome and evaluate its fitness.
         */
        ArrayList<KheperaState> states = runTrajectory(chromosome, ffnn);
        return TimeTrialFitnessFunction.evaluate(states);
    }

    private static ArrayList<KheperaState> runTrajectory(Chromosome chromosome, FFNN ffnn) {
        /*
            Runs the simulation for a specific chromosome
         */

        // Load genes from the chromosome into the neural network & initialise simulator
        ffnn.setChromosome(chromosome.getGenes());
        KheperaSimulator simulator = new KheperaSimulator(START_STATE);
        ArrayList<Command> commands = new ArrayList<>();
        ArrayList<KheperaState> states = simulator.getKheperaState(commands);

        double currentX = START_STATE.sx;
        double currentY = START_STATE.sy;

        for (int step = 0; step < STEPS_PER_INDIVIDUAL; step++) {
            // Ensure X,Y in the range of -1 to 1 for neural network input
            double normX = clamp(currentX / MAX_POS, -1.0, 1.0);
            double normY = clamp(currentY / MAX_POS, -1.0, 1.0);

            // Normalised X and Y are fed into the neural network
            double[] outputs = ffnn.fire(new double[]{normX, normY});

            // Three outputs are scaled up to the robot's limits
            int left = (int) Math.round(clamp(lerp(MIN_SPEED, MAX_SPEED, outputs[0]), MIN_SPEED, MAX_SPEED));
            int right = (int) Math.round(clamp(lerp(MIN_SPEED, MAX_SPEED, outputs[1]), MIN_SPEED, MAX_SPEED));
            int time = (int) Math.round(clamp(lerp(MIN_TIME, MAX_TIME, outputs[2]), MIN_TIME, MAX_TIME));

            // Create a new command with the parameters
            commands.add(new Command(left, right, time));

            // Call simulator, compute the new state
            states = simulator.getKheperaState(commands);
            KheperaState last = states.get(states.size() - 1);

            // Update position
            currentX = last.position.sx;
            currentY = last.position.sy;
        }

        return states; // list of states after 15 commands (per chromosome)
    }

    private static void showTrajectory(ArrayList<KheperaState> states, String message) {
        ArrayList<State> path = new ArrayList<>();
        for (KheperaState state : states) {
            path.add(state.position);
        }

        KheperaSimulator simulator = new KheperaSimulator(START_STATE);
        VisualFrame visual = new VisualFrame(50, 50, 1000, 1000, new ArrayList<>(), simulator.obstacleRadius, new Point(0, 0), new Point((int) START_STATE.sx, (int) START_STATE.sy), simulator.targetRadius, simulator.robotRadius);
        visual.setPath(path, message + "\nStates: " + path.size());

        Thread thread = new Thread(visual);
        thread.start();
    }

    private static double clamp(double value, double min, double max) {
        /*
            Clamp a value between a minimum and maximum range
            1. Scale number down (normalise) or up (denormalise)
            2. Ensure the numbers never fall outside a specific boundary
         */
        return Math.max(min, Math.min(max, value));
    }

    private static double lerp(double min, double max, double rate) {
        /*
            Linear interpolation between min and max based on a rate (0 to 1)
         */
        return min + rate * (max - min);
    }
}

