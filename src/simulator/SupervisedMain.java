package Code;

import EvolutionaryRobotics.Chromosome;
import EvolutionaryRobotics.EvolutionaryAlgorithm;
import EvolutionaryRobotics.FFNN;

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


    public static void main(String[] args) {

        FFNN ffnn = new FFNN();
        Chromosome[] population = new Chromosome[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = Chromosome.createRandom(ffnn.getChromosomeLength());
        }

        //EA loop
        for (int gen = 0; gen < GENERATIONS; gen++) {

            for (Chromosome chromosome : population) {
                double fitness = 0;
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
            //chromosome.setFitness();
        }
        EvolutionaryAlgorithm.sortByFitness(population);

        Chromosome best = population[0];
        ffnn.setChromosome(best.getGenes());

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
}
