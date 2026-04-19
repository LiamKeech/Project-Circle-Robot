package Code;

import DataHandler.DataLoader;
import DataHandler.DataPoint;
import EvolutionaryNeuralNetwork.Chromosome;
import EvolutionaryNeuralNetwork.EvolutionaryAlgorithm;
import EvolutionaryNeuralNetwork.FFNN;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {
    private static final int POPULATION_SIZE = 200;
    private static final int GENERATIONS = 1000;
    private static final int ELITE_COUNT = 1;
    private static final int TOURNAMENT_SIZE = 3;
    private static final double MUTATION_RATE = 0.1;
    private static final double MUTATION_STRENGTH = 0.8;

    private static final List<String> errorProgress = new ArrayList<>();

    // Loads training data, creates a new NN with random weights, EA loop evaluates fitness of weights, optimises weights, then exports predictions of the best chromosome to CSV

    public static void main(String[] args) {
        List<DataPoint> dataPoints = DataLoader.loadData("data/circle_training_data.csv");

        FFNN ffnn = new FFNN();
        Chromosome[] population = new Chromosome[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = Chromosome.createRandom(ffnn.getChromosomeLength());
        }

        //EA loop
        for (int gen = 0; gen < GENERATIONS; gen++) {

            for (Chromosome chromosome : population) {
                double fitness = evaluateFitness(chromosome, ffnn, dataPoints);
                chromosome.setFitness(fitness);
            }

            EvolutionaryAlgorithm.sortByFitness(population);
            errorProgress.add(gen + "," + population[0].getFitness());

            if (gen % 10 == 0 || gen == GENERATIONS - 1) {
                System.out.println("Generation " + gen + " | Best Fitness: " + population[0].getFitness());
            }

            population = getNextGeneration(population);
        }
        // Re-evaluate after the EA loop to find the best
        for (Chromosome chromosome : population) {
            chromosome.setFitness(evaluateFitness(chromosome, ffnn, dataPoints));
        }
        EvolutionaryAlgorithm.sortByFitness(population);

        Chromosome best = population[0];
        ffnn.setChromosome(best.getGenes());

        try {
            exportPredictionsToCSV(best, dataPoints, ffnn, "data/predicted_outputs.csv");
        } catch (IOException e) {
            System.err.println("Export failed: " + e.getMessage());
        }

        System.out.println("Best Chromosome Fitness: " + population[0].getFitness());
    }

    private static Chromosome[] getNextGeneration(Chromosome[] currentPopulation) {
        Chromosome[] nextGen = new Chromosome[POPULATION_SIZE];

        // Elitism
        Chromosome[] elites = EvolutionaryAlgorithm.elitism(currentPopulation, ELITE_COUNT);
        System.arraycopy(elites, 0, nextGen, 0, ELITE_COUNT);

        // Crossover and Mutation
        for (int i = ELITE_COUNT; i < currentPopulation.length; i++) {
            Chromosome parent1 = EvolutionaryAlgorithm.tournamentSelect(currentPopulation, TOURNAMENT_SIZE);
            Chromosome parent2 = EvolutionaryAlgorithm.tournamentSelect(currentPopulation, TOURNAMENT_SIZE);

            Chromosome offspring = EvolutionaryAlgorithm.crossover(parent1, parent2);
            EvolutionaryAlgorithm.GaussianMutation(offspring, MUTATION_RATE, MUTATION_STRENGTH);

            nextGen[i] = offspring;
        }

        return nextGen;
    }

    public static double evaluateFitness(Chromosome chromosome, FFNN ffnn, List<DataPoint> dataPoints) {
        ffnn.setChromosome(chromosome.getGenes());
        double MSE = 0.0;

        for (DataPoint point : dataPoints) {
            double[] predictedOutputs = ffnn.fire(point.getInputs());
            MSE += ffnn.calculateError(predictedOutputs, point.getExpectedOutputs());
        }

        return MSE;
    }

    private static void exportPredictionsToCSV(Chromosome best, List<DataPoint> data,
                                               FFNN ffnn, String filename) throws IOException {
        ffnn.setChromosome(best.getGenes());
        double totalMSE = 0.0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("NormX,NormY,PredLeft,PredRight,PredDuration,ActLeft,ActRight,ActDuration");

            for (DataPoint point : data) {
                double[] predicted = ffnn.fire(point.getInputs());
                double[] actual = point.getExpectedOutputs();

                totalMSE += ffnn.calculateError(predicted, actual);

                writer.printf(Locale.US, "%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f%n", point.getInputs()[0], point.getInputs()[1], predicted[0], predicted[1], predicted[2], actual[0],    actual[1],    actual[2]);
            }
        }

        System.out.printf("Final MSE over dataset: %.6f%n", totalMSE / data.size());
    }

//    private static void exportErrorToCSV() throws IOException {
//        try (PrintWriter writer = new PrintWriter(new FileWriter("data/learning_curve.csv"))) {
//            writer.println("Generation,BestFitness");
//            for (String line : errorProgress) {
//                writer.println(line);
//            }
//        }
//    }
}
