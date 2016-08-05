package com.toonwire.pokemongotool;

import android.graphics.drawable.Drawable;

/**
 * Created by Toonwire on 05-08-2016.
 */
public class PokemonDataCP {
    private Drawable icon;
    private String name;
    private int cp;

    public PokemonDataCP(String name, int cp, Drawable icon) {
        this.name = name;
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
}
