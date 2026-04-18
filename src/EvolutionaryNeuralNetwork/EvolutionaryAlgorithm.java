package EvolutionaryNeuralNetwork;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class EvolutionaryAlgorithm {

    public static void sortByFitness(Chromosome[] population) {
        Arrays.sort(population, Comparator.comparingDouble(Chromosome::getFitness).reversed());
    }

    public static Chromosome[] elitism(Chromosome[] population, int eliteCount) {
        sortByFitness(population);
        Chromosome[] elites = new Chromosome[eliteCount];

        for (int i = 0; i < eliteCount; i++) {
            elites[i] = population[i];
        }
        return elites;
    }

    public static Chromosome tournamentSelect(Chromosome[] population, int tournamentSize) {
        Random rand = new Random();
        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {
            Chromosome candidate = population[rand.nextInt(population.length)];
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }

    public static Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        Random rand = new Random();
        Chromosome offspring = new Chromosome();

        for (int i = 0; i < Chromosome.numberOfCommands; i++) {
            offspring.getLeftSpeeds()[i]  = rand.nextBoolean() ? parent1.getLeftSpeeds()[i]  : parent2.getLeftSpeeds()[i];
            offspring.getRightSpeeds()[i] = rand.nextBoolean() ? parent1.getRightSpeeds()[i] : parent2.getRightSpeeds()[i];
            offspring.getDurations()[i]   = rand.nextBoolean() ? parent1.getDurations()[i]   : parent2.getDurations()[i];
        }
        return offspring;
    }

    public static void GaussianMutation(Chromosome chromosome, double mutationRate, double mutationStrength) {
        Random rand = new Random();

        for (int i = 0; i < Chromosome.numberOfCommands; i++) {
            if (rand.nextDouble() < mutationRate) {
                int delta = (int)(rand.nextGaussian() * mutationStrength * (Chromosome.MAX_SPEED - Chromosome.MIN_SPEED));
                chromosome.getLeftSpeeds()[i] = Math.clamp(chromosome.getLeftSpeeds()[i] + delta, Chromosome.MIN_SPEED, Chromosome.MAX_SPEED);
            }
            if (rand.nextDouble() < mutationRate) {
                int delta = (int)(rand.nextGaussian() * mutationStrength * (Chromosome.MAX_SPEED - Chromosome.MIN_SPEED));
                chromosome.getRightSpeeds()[i] = Math.clamp(chromosome.getRightSpeeds()[i] + delta, Chromosome.MIN_SPEED, Chromosome.MAX_SPEED);
            }
            if (rand.nextDouble() < mutationRate) {
                // Same idea scaled to the time range
                int delta = (int)(rand.nextGaussian() * mutationStrength * (Chromosome.MAX_TIME - Chromosome.MIN_TIME));
                chromosome.getDurations()[i] = Math.clamp(chromosome.getDurations()[i] + delta, Chromosome.MIN_TIME, Chromosome.MAX_TIME);
            }
        }
    }
}