package EvolutionaryNeuralNetwork;

import Code.Command;

import java.util.ArrayList;
import java.util.Random;

public class Chromosome {

    public static final int numberOfCommands = 10;
    private final int[] leftSpeeds  = new int[numberOfCommands];
    private final int[] rightSpeeds = new int[numberOfCommands];
    private final int[] durations   = new int[numberOfCommands];

    public static final int MIN_SPEED = 8000;
    public static final int MAX_SPEED = 17500;
    public static final int MIN_TIME = 300;
    public static final int MAX_TIME = 3100;

    private double fitness = Double.NEGATIVE_INFINITY;

    public static Chromosome createRandChromosome() {
        Chromosome chromosome = new Chromosome();
        Random rand = new Random();
        for (int i = 0; i < numberOfCommands; i++) {
            chromosome.leftSpeeds[i]  = rand.nextInt(MIN_SPEED, MAX_SPEED);
            chromosome.rightSpeeds[i] = rand.nextInt(MIN_SPEED, MAX_SPEED);
            chromosome.durations[i]   = rand.nextInt(MIN_TIME, MAX_TIME);
        }
        return chromosome;
    }

    public ArrayList<Command> createCommands() {
        ArrayList<Command> commands = new ArrayList<>(numberOfCommands);
        for (int i = 0; i < numberOfCommands; i++) {
            commands.add(new Command(leftSpeeds[i], rightSpeeds[i], durations[i]));
        }
        return commands;
    }

    public int[] getLeftSpeeds() {
        return leftSpeeds;
    }

    public int[] getRightSpeeds() {
        return rightSpeeds;
    }

    public int[] getDurations() {
        return durations;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}