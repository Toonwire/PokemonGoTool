package com.toonwire.pokemongotool;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExperienceAdapter extends ArrayAdapter<PokemonDataXP> {

    private int resourceID;
    private ArrayList<PokemonDataXP> dataList;
    private LayoutInflater mInflater;

    public ExperienceAdapter(Activity context, int resource, ArrayList<PokemonDataXP> items) {
        super(context, resource, items);
        this.resourceID = resource;
        this.dataList = items;
        this.mInflater = context.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public PokemonDataXP getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(resourceID, parent, false);

            holder = new ViewHolder();
            holder.tvPokemon = (TextView) convertView.findViewById(R.id.tv_row_pokemon);
            holder.tvTransfer = (TextView) convertView.findViewById(R.id.tv_row_transfer);
            holder.tvEvolve = (TextView) convertView.findViewById(R.id.tv_row_evolve);
            holder.tvPostEvoTransfer = (TextView) convertView.findViewById(R.id.tv_row_post_evo_transfer);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PokemonDataXP data = dataList.get(position);
        holder.tvPokemon.setText(data.getName());
        holder.tvTransfer.setText(String.valueOf(data.getTransfers()));
        holder.tvEvolve.setText(String.valueOf(data.getEvolutions()));
        holder.tvPostEvoTransfer.setText(String.valueOf(data.getPostEvoTransfers()));

        return convertView;
    }

    // static reference object to minimize findViewById() and inflater calls
    private static class ViewHolder {
        private TextView tvPokemon;
        private TextView tvTransfer;
        private TextView tvEvolve;
        private TextView tvPostEvoTransfer;
    }
}
