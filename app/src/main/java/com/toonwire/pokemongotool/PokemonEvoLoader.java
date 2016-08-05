package com.toonwire.pokemongotool;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Created by Toonwire on 25-07-2016.
 */
public class PokemonEvoLoader extends AsyncTask<String, String, ArrayList<Pokemon>> {
    // Initialize the Set size to avoid resizing
    public static ArrayList<Pokemon> POKEMON_LIST = new ArrayList<>(150);
    public static String[] POKEMON_NAMES = new String[150];

    private Context mContext;

    public PokemonEvoLoader(Context context) {
        this.mContext = context;
    }

    // Runs in UI before background thread is called
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // This is run in a background thread
    @Override
    protected ArrayList<Pokemon> doInBackground(String... params) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mContext.getResources().getAssets().open("PokemonGo_evolution_data.csv")));
            LinkedList<Pokemon> pokeFamily = new LinkedList<>();
            int index = 0;
            String line;
            while ((line = br.readLine()) != null) {
                // pokemon name, candy, minMult, maxMult, avgMult
                String[] data = line.split(",");
                boolean fullyEvolved = data.length == 1;

                // ignore the index '# xxx - ' = 8 length
                String name = data[0].substring(8);
                POKEMON_NAMES[index] = name;
                index++;

                Pokemon pokemon;
                if (!fullyEvolved) {
                    int candyNeeded = Integer.parseInt(data[1]);
                    double minMult = Double.parseDouble(data[2].substring(0, data[2].length()-1));
                    double maxMult = Double.parseDouble(data[3].substring(0, data[3].length()-1));
                    double avgMult = Double.parseDouble(data[4].substring(0, data[4].length()-1));
                    pokemon = new Pokemon(name, candyNeeded, minMult, maxMult, avgMult);
                    pokeFamily.add(pokemon);

                } else {
                    pokemon = new Pokemon(name);
                    pokeFamily.add(pokemon);

                    for (Pokemon p : pokeFamily) {
                        p.addFamily(pokeFamily, pokeFamily.indexOf(p));
                    }
                    pokeFamily = new LinkedList<>();
                }
                POKEMON_LIST.add(pokemon);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return POKEMON_LIST;
    }

    // This runs in UI when background thread finishes
    @Override
    protected void onPostExecute(ArrayList<Pokemon> result) {
        super.onPostExecute(result);

    }

}
