package com.toonwire.pokemongotool;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ExperienceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context mContext;

    private AutoCompleteTextView editAutoPokemon;
    private EditText editCandy, editPokemonAmount;
    private TextView tvXP;

    private int totalXP;
    private double totalMinutes;

    private ListView listView;
    private ArrayList<PokemonData> dataList;
    private PokeAdapter pokeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xp);

        mContext = getApplicationContext();

        tvXP = (TextView) findViewById(R.id.tv_xp);
        editAutoPokemon = (AutoCompleteTextView) findViewById(R.id.auto_edit_pokemon);
        editAutoPokemon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                hideSoftKeyboard(view);
            }
        });
        editCandy = (EditText) findViewById(R.id.edit_candy);
        editPokemonAmount = (EditText) findViewById(R.id.edit_pokemon_amount);

        TextView.OnEditorActionListener editorListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideSoftKeyboard(v);
                    return true;
                }
                return false;
            }
        };

        editPokemonAmount.setOnEditorActionListener(editorListener);
        editAutoPokemon.setOnEditorActionListener(editorListener);
        editCandy.setOnEditorActionListener(editorListener);


        dataList = new ArrayList<>();
        pokeAdapter = new PokeAdapter(this, R.layout.row, dataList);


        listView = (ListView) findViewById(R.id.list_view);
        ViewGroup headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.list_header, listView, false);
        listView.addHeaderView(headerView);
        listView.setAdapter(pokeAdapter);




        new PokemonEvoLoader(this).execute("");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setSubtitle("Experience optimizer");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRequiredFilled()) {
                    // check to see if the pokemon is fully evolved
                    Pokemon pokemon = getPokemonFromName(editAutoPokemon.getText().toString());
                    if (pokemon.isFullyEvolved()) {
                        String msg = pokemon.getName() + " is already fully evolved.\nNo experience can be gained through evolution";
                        showSnackbar(view, msg, R.color.accent);
                    } else {
                        // add item to list
                        hideSoftKeyboard(view);
                        addPokemonDataRow();
                    }
                } else {
                    showSnackbar(view, "Fill required fields", R.color.accent);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public void showSnackbar(View view, String msg, int colorID) {
        hideSoftKeyboard(view);
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null);
        View snackBarView = snackbar.getView();
        TextView tv = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        snackBarView.setBackgroundColor(ContextCompat.getColor(mContext, colorID));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_xp) {
            Toast.makeText(mContext, "XP clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_cp) {
            Toast.makeText(mContext, "CP clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ExperienceActivity.this, CombatPowerActivity.class));

        } else if (id == R.id.nav_share) {
            Toast.makeText(mContext, "Share clicked", Toast.LENGTH_SHORT).show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addPokemonDataRow() {
        // adds a new row element to the listView

        Pokemon pokemon = getPokemonFromName(editAutoPokemon.getText().toString());
        int candy = Integer.parseInt(editCandy.getText().toString());
        int numberOfPokemon = Integer.parseInt(editPokemonAmount.getText().toString());

        int candyNeeded = pokemon.getCandyNeeded();
        int candyMissing = candyNeeded - candy;
        int transfers = 0, evolutions = 0;
        int maxTransfers = numberOfPokemon - 1;

        while (transfers + candyMissing <= maxTransfers
                && evolutions < numberOfPokemon - transfers) {
            if (candyMissing > 0) {
                transfers += candyMissing;
                candyMissing = candyNeeded;
            } else {
                candy -= candyNeeded;
                candyMissing = candyNeeded - candy;
            }
            evolutions++;
        }
        int finalEvolutions = evolutions;


        int pokemonLeft = numberOfPokemon - transfers;
        int postEvoTransfers = 0;

        while (pokemonLeft > 0 && numberOfPokemon > evolutions && pokemonLeft + evolutions > candyMissing) {
            if (evolutions >= candyMissing) {
                evolutions -= candyMissing;
                postEvoTransfers += candyMissing;
            } else if (evolutions + pokemonLeft > candyMissing) {
                postEvoTransfers += evolutions;
                transfers += candyMissing - evolutions;
                pokemonLeft -= candyMissing + evolutions; // subtract what has just been transferred
                evolutions = 0;
            }
            finalEvolutions++;
            pokemonLeft--;
        }


        double minutes = ((double)finalEvolutions)/2;    // if one evolution takes 30 sec
        int xp = finalEvolutions*1000;         // 1000xp with Lucky Egg per evolution

        totalXP += xp;
        totalMinutes += minutes;


        PokemonData row = new PokemonData(pokemon.getName(), transfers, finalEvolutions, postEvoTransfers);
        dataList.add(0, row);
        pokeAdapter.notifyDataSetChanged();

        // update main xp view
        tvXP.setText(String.valueOf(totalXP));

        // make snackbar to display time spent evolving
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getTimeSpentEvolving(), Snackbar.LENGTH_LONG).setAction("Action", null);
        View snackBarView = snackbar.getView();
        TextView tv = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_dark));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();

        // reset edit texts
        editAutoPokemon.setText("");
        editCandy.setText("");
        editPokemonAmount.setText("");
        editAutoPokemon.requestFocus();

    }

    public String getTimeSpentEvolving() {
        return "Evolving for "  + totalMinutes + " min";
    }

    public Pokemon getPokemonFromName(String name) {
        Pokemon pokemon = null;

        for (Pokemon p : PokemonEvoLoader.POKEMON_LIST) {
            if (p.getName().equals(name)) {
                pokemon = p;
                break;
            }
        }
        return pokemon;
    }

    public AutoCompleteTextView getSelectView() {
        return editAutoPokemon;
    }

    private boolean isRequiredFilled() {
        return PokemonEvoLoader.POKEMON_LIST.contains(getPokemonFromName(editAutoPokemon.getText().toString()))
                && !editPokemonAmount.getText().toString().isEmpty()
                && !editCandy.getText().toString().isEmpty();
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }
}