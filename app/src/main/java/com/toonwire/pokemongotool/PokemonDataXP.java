package com.toonwire.pokemongotool;

/**
 * Created by Toonwire on 03-08-2016.
 */
public class PokemonDataXP {
    private String name;
    private int transfers, evolutions, postEvoTransfers;

    public PokemonDataXP(String name, int transfers, int evolutions, int postEvoTransfers) {
        this.name = name;
        this.transfers = transfers;
        this.evolutions = evolutions;
        this.postEvoTransfers = postEvoTransfers;
    }

    public int getTransfers() {
        return transfers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEvolutions() {
        return evolutions;
    }

    public int getPostEvoTransfers() {
        return postEvoTransfers;
    }

}
