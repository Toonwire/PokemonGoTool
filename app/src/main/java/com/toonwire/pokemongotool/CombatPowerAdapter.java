package com.toonwire.pokemongotool;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Toonwire on 05-08-2016.
 */
public class CombatPowerAdapter extends ArrayAdapter<Object>{

    private static final int POKEMON_ROW = 0;
    private static final int INFO_ROW = 1;
    private static final int[] ROW_TYPES = {POKEMON_ROW, INFO_ROW};



    private Activity context;
    private int resourceID;

    private ArrayList<Object> dataList;
    private LayoutInflater mInflater;


    public CombatPowerAdapter(Activity context, int resource, ArrayList<Object> items) {
        super(context, resource, items);
        this.context = context;
        this.resourceID = resource;
        this.dataList = items;
        this.mInflater = context.getLayoutInflater();
    }

    @Override
    public int getItemViewType(int position) {
        if (dataList.get(position) instanceof PokemonDataCP) {
            return POKEMON_ROW;
        } else {
            return INFO_ROW;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == POKEMON_ROW;
    }

    @Override
    public int getViewTypeCount() {
        return ROW_TYPES.length;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        PokemonViewHolder pokemonViewHolder;
        InfoViewHolder infoViewHolder;

        switch (getItemViewType(position)) {

            case POKEMON_ROW:
                // null our other view holders
                infoViewHolder = null;

                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_row_cp, parent, false);
                    pokemonViewHolder = new PokemonViewHolder(convertView);
                    convertView.setTag(pokemonViewHolder);
                } else {
                    pokemonViewHolder = (PokemonViewHolder) convertView.getTag();
                }
                PokemonDataCP pokemonData = (PokemonDataCP) dataList.get(position);
//                String cp = position == 0 ? pokemonData.getCP() + " CP" : (pokemonData.getCP() + " CP (avg)");
                String cp = pokemonData.getCP() + " CP";

                pokemonViewHolder.ivPokemon.setImageDrawable(pokemonData.getIcon());
                pokemonViewHolder.tvName.setText(pokemonData.getName());
                pokemonViewHolder.tvIndividualCP.setText(cp);
                break;


            case INFO_ROW:
                // null our other view holders
                pokemonViewHolder = null;

                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_row_arrow_data, parent, false);
                    infoViewHolder = new InfoViewHolder(convertView);
                    convertView.setTag(infoViewHolder);
                } else {
                    infoViewHolder = (InfoViewHolder) convertView.getTag();
                }
                InfoDataCP infoData = (InfoDataCP) dataList.get(position);
                String candy = infoData.getCandy() + " candy";
                String avgMult = "x" + infoData.getAvgMultiplier();

                infoViewHolder.tvCandy.setText(candy);
                infoViewHolder.tvMultiplier.setText(avgMult);
                break;

        }
        return convertView;
    }

    // static reference object to minimize findViewById() and inflater calls
    private static class PokemonViewHolder {
        private ImageView ivPokemon;
        private TextView tvName;
        private TextView tvIndividualCP;

        public PokemonViewHolder(View v) {
            this.ivPokemon = (ImageView) v.findViewById(R.id.iv_ic_pokemon);
            this.tvName = (TextView) v.findViewById(R.id.tv_row_pokemon_name);
            this.tvIndividualCP = (TextView) v.findViewById(R.id.tv_row_cp);
        }
    }


    private static class InfoViewHolder {
        private TextView tvCandy;
        private TextView tvMultiplier;

        public InfoViewHolder(View v) {
            this.tvCandy = (TextView) v.findViewById(R.id.tv_row_pokemon_candy);
            this.tvMultiplier = (TextView) v.findViewById(R.id.tv_row_cp_multiplier);
        }
    }
}
