import DataHandler.DataLoader;
import DataHandler.DataPoint;
import EvolutionaryNeuralNetwork.Chromosome;
import EvolutionaryNeuralNetwork.EvolutionaryAlgorithm;
import EvolutionaryNeuralNetwork.FFNN;

private static final int POPULATION_SIZE = 100;
private static final int GENERATIONS = 1000;
private static final int ELITE_COUNT = 1;
private static final int TOURNAMENT_SIZE = 12;

private static List<String> errorProgress = new ArrayList<>();

void main() {
    List<DataPoint> dataPoints = DataLoader.loadData("data/circle_training_data.csv");

    FFNN ffnn = new FFNN();
    Chromosome[] population = new Chromosome[POPULATION_SIZE];
    for (int i = 0; i < POPULATION_SIZE; i++) {
        //population[i] = Chromosome.getRandomChromosome(ffnn.getChromosomeLength());
    }

    //EA loop
    for (int gen = 0; gen < GENERATIONS; gen++) {
        for (Chromosome chromosome : population) {
            ffnn.setChromosome(chromosome.getGenes());
            double totalError = 0.0;

            for (int i = 0; i < dataPoints.size() - 1; i++) {
                //double[] input = dataPoints.get(i).toArray();
                //double[] actual = dataPoints.get(i + 1).toArray();

                //double[] predicted = ffnn.fire(input);
                //totalError += ffnn.calculateError(predicted, actual);
            }
            //chromosome.setFitness(-totalError / (dataPoints.size() - 1));
        }

        EvolutionaryAlgorithm.sortByFitness(population);
        errorProgress.add(gen + ", " + population[0].getFitness());

        if (gen % 1 == 0 || gen == GENERATIONS - 1) {
            System.out.println("Generation " + gen + " | Best Fitness: " + population[0].getFitness());
        }

        population = getNextGeneration(population);
    }

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
        //EvolutionaryAlgorithm.GaussianMutation(offspring);

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

    return -MSE;
}
