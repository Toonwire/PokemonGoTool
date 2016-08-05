package com.toonwire.pokemongotool;

import java.util.LinkedList;

public class Pokemon {

    private String name;
    private int candyNeeded;
    private double minMult, maxMult, avgMult;
    private boolean fullyEvolved;

    private LinkedList<Pokemon> family;
    private int familyIndex;

    public Pokemon(String name, int candyNeeded, double minMult, double maxMult, double avgMult) {
        this.name = name;
        this.candyNeeded = candyNeeded;
        this.minMult = minMult;
        this.maxMult = maxMult;
        this.avgMult = avgMult;
        fullyEvolved = false;
    }

    public Pokemon(String name) {
        this.name = name;
        fullyEvolved = true;
    }

    public String getName() {
        return name;
    }

    public void addFamily(LinkedList<Pokemon> family, int familyIndex) {
        this.family = family;
        this.familyIndex = familyIndex;
    }

    public int getCandyNeeded() {
        return candyNeeded;
    }

    public boolean isFullyEvolved() {
        return fullyEvolved;
    }

    public int getFamilyIndex() {
        return familyIndex;
    }

    public LinkedList<Pokemon> getFamily() {
        return family;
    }

    public double getMinMult() {
        return minMult;
    }

    public double getMaxMult() {
        return maxMult;
    }

    public double getAvgMult() {
        return avgMult;
    }
}
