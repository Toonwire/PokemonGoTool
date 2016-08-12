package com.toonwire.pokemongotool;

/**
 * Created by Toonwire on 06-08-2016.
 */
public class InfoDataCP {
    private int candy;
    private double avgMultiplier;

    public InfoDataCP(int candy, double avgMultiplier) {
        this.candy = candy;
        this.avgMultiplier = avgMultiplier;
    }

    public double getAvgMultiplier() {
        return avgMultiplier;
    }

    public int getCandy() {
        return candy;
    }

}
