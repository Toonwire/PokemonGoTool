package com.toonwire.pokemongotool;

import android.graphics.drawable.Drawable;

public class PokemonDataCP {
    private Pokemon pokemon;
    private Drawable icon;
    private String name;
    private int cp;

    public PokemonDataCP(Pokemon pokemon, int cp, Drawable icon) {
        this.pokemon = pokemon;
        this.name = pokemon.getName();
        this.cp = cp;
        this.icon = icon;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public int getCP() {
        return cp;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }
}
