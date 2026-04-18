package EvolutionaryNeuralNetwork;

import java.util.Random;

public class Chromosome {
    private double[] genes; // FFNN weights flattened to 1D

    private double fitness = Double.NEGATIVE_INFINITY;

    public static Chromosome createRandom(int chromosomeLength) {
        Chromosome chromosome = new Chromosome();
        chromosome.genes = new double[chromosomeLength];
        Random rand = new Random();
        for (int i = 0; i < chromosomeLength; i++) {
            chromosome.genes[i] = rand.nextDouble(-1.0, 1.0);
        }
        return chromosome;
    }

    public double[] getGenes() {
        return genes;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}