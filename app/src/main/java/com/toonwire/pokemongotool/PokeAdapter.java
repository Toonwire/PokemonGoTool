package com.toonwire.pokemongotool;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Toonwire on 03-08-2016.
 */
public class PokeAdapter extends ArrayAdapter<PokemonData> {

    private Activity context;
    private int resourceID;

    private ArrayList<PokemonData> dataList;
    private LayoutInflater mInflater = null;

    public PokeAdapter(Activity context, int resource, ArrayList<PokemonData> items) {
        super(context, resource, items);
        this.context = context;
        this.resourceID = resource;
        this.dataList = items;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public PokemonData getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(resourceID, parent, false);
        }

        TextView tvPokemon = (TextView) convertView.findViewById(R.id.tv_row_pokemon);
        TextView tvTransfer = (TextView) convertView.findViewById(R.id.tv_row_transfer);
        TextView tvEvolve = (TextView) convertView.findViewById(R.id.tv_row_evolve);
        TextView tvPostEvoTransfer = (TextView) convertView.findViewById(R.id.tv_row_post_evo_transfer);

        PokemonData data = dataList.get(position);
        tvPokemon.setText(data.getName());
        tvTransfer.setText(String.valueOf(data.getTransfers()));
        tvEvolve.setText(String.valueOf(data.getEvolutions()));
        tvPostEvoTransfer.setText(String.valueOf(data.getPostEvoTransfers()));

        return convertView;
    }
}
