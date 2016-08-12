package com.toonwire.pokemongotool;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ExperienceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PokemonEvoLoader.AsyncTaskCompleteListener<String[]> {

    private Context mContext;

    private AutoCompleteTextView editAutoPokemon;
    private EditText editCandy, editPokemonAmount;
    private TextView tvXP;

    private NavigationView navigationView;

    private int totalXP;
    private double totalMinutes;
    private ValueAnimator xpChangeAnimator;

    private ArrayList<PokemonDataXP> dataList;
    private ExperienceAdapter xpAdapter;
    private int selectedRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xp);
        mContext = getApplicationContext();

        new PokemonEvoLoader(mContext, this).execute("");

        tvXP = (TextView) findViewById(R.id.tv_xp);
        editAutoPokemon = (AutoCompleteTextView) findViewById(R.id.auto_edit_pokemon_xp);
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
        xpAdapter = new ExperienceAdapter(this, R.layout.list_row_xp, dataList);

        ListView listView = (ListView) findViewById(R.id.list_view_xp);
        ViewGroup headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.list_header_xp, listView, false);
        listView.addHeaderView(headerView);
        listView.setAdapter(xpAdapter);
        listView.setSelector(R.drawable.xp_data_selector);

        // animate the change in XP
        xpChangeAnimator = new ValueAnimator();
        xpChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tvXP.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        xpChangeAnimator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });
        xpChangeAnimator.setDuration(1000);
        xpChangeAnimator.setInterpolator(new DecelerateInterpolator());


        // animate removal by sliding out from the list
        final Animation slideOutAnimation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_out_right);
        slideOutAnimation.setDuration(500);
        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                PokemonDataXP dataRow = xpAdapter.getItem(selectedRow);
                // remove the long clicked (selected) row from the dataList and notify adapter of the change
                dataList.remove(dataRow);
                xpAdapter.notifyDataSetChanged();

                int tempTotalXP = totalXP;
                // adjust the total xp and total time spent evolving
                totalXP -= dataRow.getEvolutions()*1000;
                totalMinutes -= ((double)dataRow.getEvolutions())/2;

                xpChangeAnimator.setObjectValues(tempTotalXP, totalXP);
                xpChangeAnimator.start();

                showSnackbar(findViewById(android.R.id.content), getTimeSpentEvolving(), R.color.primary_dark);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                selectedRow = pos-1;    // list view is 1-indexed, but the dataList (ArrayList) is 0-index
                view.startAnimation(slideOutAnimation);

                return true;
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setSubtitle("Experience optimizer");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRequiredFilled()) {
                    // check to see if the pokemon is fully evolved
                    Pokemon pokemon = getPokemonFromName(editAutoPokemon.getText().toString());
                    if (pokemon.isFullyEvolved()) {
                        String msg = pokemon.getName() + " is already fully evolved.\nNo experience can be gained through evolution";
                        showSnackbar(view, msg, R.color.error);
                    } else {
                        // add item to list
                        hideSoftKeyboard(view);
                        addPokemonDataRow();
                    }
                } else {
                    showSnackbar(view, "Fill required fields", R.color.error);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(0).setChecked(true);
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

        } else if (id == R.id.nav_cp) {
            startActivity(new Intent(ExperienceActivity.this, CombatPowerActivity.class));
            overridePendingTransition(R.anim.pull_activity_in_right, R.anim.push_activity_out_left);

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addPokemonDataRow() {
        // store temp value of XP to animate the difference
        int tempTotalXP = totalXP;

        // get the optimized data row object
        PokemonDataXP dataRow = optimizeXP();

        // adds a new row element to the listView
        dataList.add(0, dataRow);
        xpAdapter.notifyDataSetChanged();

        // update main xp view
        xpChangeAnimator.setObjectValues(tempTotalXP, totalXP);
        xpChangeAnimator.start();

        // make snackbar to display time spent evolving
        showSnackbar(findViewById(android.R.id.content), getTimeSpentEvolving(), R.color.primary_dark);

        // reset edit texts
        editAutoPokemon.setText("");
        editCandy.setText("");
        editPokemonAmount.setText("");
        editAutoPokemon.requestFocus();

    }

    private PokemonDataXP optimizeXP() {
        Pokemon pokemon = getPokemonFromName(editAutoPokemon.getText().toString());
        int candy = Integer.parseInt(editCandy.getText().toString());
        int numberOfPokemon = Integer.parseInt(editPokemonAmount.getText().toString());

        int candyNeeded = pokemon.getCandyNeeded();
        int candyMissing = candyNeeded - candy;
        int transfers = 0, evolutions = 0;
        int maxTransfers = numberOfPokemon - 1;

        while (transfers + candyMissing <= maxTransfers && evolutions < numberOfPokemon - transfers) {
            if (candyMissing > 0) {
                candy = 0;
                transfers += candyMissing;
                candyMissing = candyNeeded;
            } else {
                candy -= candyNeeded;
                candyMissing = candyNeeded - candy;
            }
            evolutions++;
            maxTransfers--;
        }

        int finalEvolutions = evolutions;
        int pokemonLeft = numberOfPokemon - transfers - evolutions;
        int postEvoTransfers = 0;

        while (pokemonLeft > 0 && numberOfPokemon > evolutions && pokemonLeft + evolutions > candyMissing) {
            if (evolutions >= candyMissing) {
                evolutions -= candyMissing;
                postEvoTransfers += candyMissing;
            } else {    // we need to include the leftover pokemon
                // this will prioritize transferring non-evolved before evolved pokemon
                transfers += pokemonLeft - 1;
                postEvoTransfers += candyMissing - (pokemonLeft - 1);
                pokemonLeft = 0;

                // this will prioritize transferring evolutions before non-evolved pokemon
//                postEvoTransfers += evolutions;
//                transfers += candyMissing - evolutions;
//                pokemonLeft -= (candyMissing - evolutions); // subtract what has just been transferred
//                evolutions = 0;

            }
            evolutions++;
            finalEvolutions++;
            pokemonLeft--;
        }


        double minutes = ((double)finalEvolutions)/2;    // if one evolution takes 30 sec
        int xp = finalEvolutions*1000;         // 1000xp with Lucky Egg per evolution

        // add new xp and time to totals
        totalXP += xp;
        totalMinutes += minutes;

        // naming control
        String pokemonName = pokemon.getName().startsWith("Eevee") ? "Eevee" : pokemon.getName();

        // return the data row object
        return new PokemonDataXP(pokemonName, transfers, finalEvolutions, postEvoTransfers);
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

    private boolean isRequiredFilled() {
        return PokemonEvoLoader.POKEMON_LIST.contains(getPokemonFromName(editAutoPokemon.getText().toString()))
                && !editPokemonAmount.getText().toString().isEmpty()
                && !editCandy.getText().toString().isEmpty();
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }

    @Override
    public void onTaskComplete(String[] result) {
        editAutoPokemon.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, result));
    }
}