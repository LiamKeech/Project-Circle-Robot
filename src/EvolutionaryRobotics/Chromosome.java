package EvolutionaryRobotics;

import java.util.Arrays;
import java.util.Random;

public class Chromosome {
    private final double[] genes;
    private double fitness;

    // Represents weights and biases of the FFNN in a single array. Fitness is the error (lower is better).

    public Chromosome(double[] genes) {
        this.genes = genes;
        this.fitness = 0.0;
    }

    public static Chromosome createRandom(int length) {
        Random rand = new Random();
        double[] randomGenes = new double[length];
        for (int i = 0; i < length; i++) {
            randomGenes[i] = rand.nextDouble(-1,1);
        }
        return new Chromosome(randomGenes);
    }

    public Chromosome deepCopy() {
        return new Chromosome(Arrays.copyOf(this.genes, this.genes.length));
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