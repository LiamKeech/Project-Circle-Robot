package EvolutionaryNeuralNetwork;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class EvolutionaryAlgorithm {

    public static void sortByFitness(Chromosome[] population) {
        Arrays.sort(population, Comparator.comparingDouble(Chromosome::getFitness));
    }

    public static Chromosome[] elitism(Chromosome[] population, int eliteCount) {
        sortByFitness(population);
        Chromosome[] elites = new Chromosome[eliteCount];

        for (int i = 0; i < eliteCount; i++) {
            elites[i] = population[i].deepCopy();
        }
        return elites;
    }

    public static Chromosome tournamentSelect(Chromosome[] population, int tournamentSize) {
        Random rand = new Random();
        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {
            Chromosome candidate = population[rand.nextInt(population.length)];
            if (best == null || candidate.getFitness() < best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }

    public static Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        Random rand = new Random();
        double[] parent1Genes = parent1.getGenes();
        double[] parent2Genes = parent2.getGenes();
        Chromosome offspring = Chromosome.createRandom(parent1Genes.length);
        double[] offspringGenes = offspring.getGenes();

        for (int i = 0; i < offspringGenes.length; i++) {
            offspringGenes[i] = rand.nextBoolean() ? parent1Genes[i] : parent2Genes[i];
        }
        return offspring;
    }

    public static void GaussianMutation(Chromosome chromosome, double mutationRate, double mutationStrength) {
        Random rand = new Random();
        double[] genes = chromosome.getGenes();

        for (int i = 0; i < genes.length; i++) {
            if (rand.nextDouble() < mutationRate) {
                double delta = rand.nextGaussian() * mutationStrength;
                genes[i] = Math.max(-1.0, Math.min(1.0, genes[i] + delta));
            }
        }
    }
}