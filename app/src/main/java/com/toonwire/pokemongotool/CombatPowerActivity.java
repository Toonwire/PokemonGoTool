package com.toonwire.pokemongotool;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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

public class CombatPowerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Context mContext;

    private AutoCompleteTextView editAutoPokemon;
    private EditText editCP;
    private TextView tvMinCP, tvMaxCP;

    private ArrayList<Object> dataList;
    private CombatPowerAdapter cpAdapter;

    private int minCP = 0, maxCP = 0;
    private ValueAnimator cpMinChangeAnimator, cpMaxChangeAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp);
        mContext = getApplicationContext();


        tvMinCP = (TextView) findViewById(R.id.tv_cp_min);
        tvMaxCP = (TextView) findViewById(R.id.tv_cp_max);
        editAutoPokemon = (AutoCompleteTextView) findViewById(R.id.auto_edit_pokemon_cp);
        editAutoPokemon.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, PokemonEvoLoader.POKEMON_NAMES));
        editAutoPokemon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                hideSoftKeyboard(view);
            }
        });
        editCP = (EditText) findViewById(R.id.edit_cp);

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
        editAutoPokemon.setOnEditorActionListener(editorListener);
        editCP.setOnEditorActionListener(editorListener);


        dataList = new ArrayList<>();
        cpAdapter = new CombatPowerAdapter(this, R.layout.list_row_cp, dataList);

        ListView listView = (ListView) findViewById(R.id.list_view_cp);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setAdapter(cpAdapter);

        // animate the change in CP

        // --------- Animate min range -------
        cpMinChangeAnimator = new ValueAnimator();
        cpMinChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tvMinCP.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        cpMinChangeAnimator.setDuration(1000);
        cpMinChangeAnimator.setInterpolator(new DecelerateInterpolator());

        // --------- Animate max range -------
        cpMaxChangeAnimator = new ValueAnimator();
        cpMaxChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tvMaxCP.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        cpMaxChangeAnimator.setDuration(1000);
        cpMaxChangeAnimator.setInterpolator(new DecelerateInterpolator());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setSubtitle("Combat power calculator");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_go);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequiredFilled()) {
                    // check to see if the pokemon is fully evolved
                    Pokemon pokemon = getPokemonFromName(editAutoPokemon.getText().toString());
                    if (pokemon.isFullyEvolved()) {
                        String msg = pokemon.getName() + " is already fully evolved.\nOnly Stardust will increase its combat power";
                        showSnackbar(view, msg, R.color.accent);
                    } else {
                        hideSoftKeyboard(view);
                        dataList.clear();

                        // animate change in cp (first evolution)
                        int tempMinCP = minCP;
                        int tempMaxCP = maxCP;

                        int initialCP = Integer.parseInt(editCP.getText().toString());
                        minCP = (int) (initialCP * pokemon.getMinMult());
                        maxCP = (int) (initialCP * pokemon.getMaxMult());
                        cpMinChangeAnimator.setObjectValues(tempMinCP, minCP);
                        cpMaxChangeAnimator.setObjectValues(tempMaxCP, maxCP);
                        cpMinChangeAnimator.start();
                        cpMaxChangeAnimator.start();

                        // add items to list
                        addPokemonDataRows(initialCP);

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

    public void addPokemonDataRows(int cp) {
        Pokemon pokemon = getPokemonFromName(editAutoPokemon.getText().toString());
        int familyIndex = pokemon.getFamilyIndex();

        // add pokemon and all its evolutions as data row objects and add them to the list view
        for (int i = familyIndex; i < pokemon.getFamily().size(); i++) {
            if (i != familyIndex)   // only add in between info data
                dataList.add(new InfoDataCP(pokemon.getFamily().get(i-1).getCandyNeeded(), pokemon.getFamily().get(i-1).getAvgMult()));

            dataList.add(new PokemonDataCP(pokemon.getFamily().get(i).getName(), cp, getPokemonIcon(pokemon.getFamily().get(i))));
            cp = (int) (cp * pokemon.getFamily().get(i).getAvgMult());

        }
        cpAdapter.notifyDataSetChanged();

        // reset edit texts
        editAutoPokemon.setText("");
        editCP.setText("");
        editAutoPokemon.requestFocus();

    }

    private Drawable getPokemonIcon(Pokemon pokemon) {

        String identifier = pokemon.getName().toLowerCase().replaceAll("[^a-z]", "");
        int pokemonIconResource = mContext.getResources().getIdentifier(identifier, "drawable", mContext.getPackageName());
        return ContextCompat.getDrawable(mContext, pokemonIconResource);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        if (id == R.id.action_cp) {
//            Toast.makeText(mContext, "CP clicked", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if (id == R.id.action_xp) {
//            Toast.makeText(mContext, "XP clicked", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(CombatPowerActivity.this, ExperienceActivity.class));
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cp) {
            Toast.makeText(mContext, "CP clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_xp) {
            Toast.makeText(mContext, "XP clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CombatPowerActivity.this, ExperienceActivity.class));

        } else if (id == R.id.nav_share) {
            Toast.makeText(mContext, "Share clicked", Toast.LENGTH_SHORT).show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public AutoCompleteTextView getSelectView() {
        return editAutoPokemon;
    }

    private boolean isRequiredFilled() {
        return PokemonEvoLoader.POKEMON_LIST.contains(getPokemonFromName(editAutoPokemon.getText().toString()))
                && !editCP.getText().toString().isEmpty();
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

    private void hideSoftKeyboard(View v) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }
}

