package com.toonwire.pokemongotool;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

public class CombatPowerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Context mContext;

    private AutoCompleteTextView editAutoPokemon;
    private EditText editCP;
    private TextView tvMinCP, tvMaxCP;

    private NavigationView navigationView;

    private ArrayList<Object> dataList;
    private CombatPowerAdapter cpAdapter;

    private int initialCP = 0, minCP = 0, maxCP = 0;
    private ValueAnimator cpMinChangeAnimator, cpMaxChangeAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp);
        mContext = getApplicationContext();

        tvMinCP = (TextView) findViewById(R.id.tv_cp_min);
        tvMaxCP = (TextView) findViewById(R.id.tv_cp_max);
        editAutoPokemon = (AutoCompleteTextView) findViewById(R.id.auto_edit_pokemon_cp);
        editAutoPokemon.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PokemonEvoLoader.POKEMON_NAMES));
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

        final ListView listView = (ListView) findViewById(R.id.list_view_cp);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setAdapter(cpAdapter);
        listView.setSelector(R.drawable.cp_data_selector);


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                int dataPosition = pos-1;     // list view click is 1-indexed, but the dataList (ArrayList) is 0-index

                int newMinCP = initialCP;
                int newMaxCP = initialCP;
                for (int i = 0; i < dataPosition; i += 2) {     // i += 2 to skip INFO_ROWs
                    newMinCP *= ((PokemonDataCP) dataList.get(i)).getPokemon().getMinMult();
                    newMaxCP *= ((PokemonDataCP) dataList.get(i)).getPokemon().getMaxMult();
                }
                cpMinChangeAnimator.setObjectValues(minCP, newMinCP);
                cpMaxChangeAnimator.setObjectValues(maxCP, newMaxCP);
                cpMinChangeAnimator.start();
                cpMaxChangeAnimator.start();
                minCP = newMinCP;
                maxCP = newMaxCP;

                // reset all row item background colors
                for (int i = 0; i < parent.getChildCount(); i++) {
                    parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }
                // then assign the long clicked view with the focus color
                view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.cp_selected_background));

                return true;
            }
        });


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
                        showSnackbar(view, msg, R.color.error);
                    } else {
                        hideSoftKeyboard(view);
                        dataList.clear();   // only show one family at a time, clear in between
                        initialCP = Integer.parseInt(editCP.getText().toString());

                        // animate change in cp (first evolution)
                        int tempMinCP = minCP;
                        int tempMaxCP = maxCP;

                        minCP = (int) (initialCP * pokemon.getMinMult());
                        maxCP = (int) (initialCP * pokemon.getMaxMult());
                        cpMinChangeAnimator.setObjectValues(tempMinCP, minCP);
                        cpMaxChangeAnimator.setObjectValues(tempMaxCP, maxCP);
                        cpMinChangeAnimator.start();
                        cpMaxChangeAnimator.start();

                        // add items to list
                        addPokemonDataRows(initialCP);

                        /*
                         * post as runnable to queue the execution 'till after the adapter has
                         * updated the list view, then mark next evolution as selected.
                         */
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < listView.getChildCount(); i++) {
                                    listView.getChildAt(i).setBackground(ContextCompat.getDrawable(mContext, android.R.color.transparent));
                                }
                                listView.getChildAt(2).setBackground(ContextCompat.getDrawable(mContext, R.drawable.cp_selected_background));
                            }
                        });
                    }
                } else {
                    showSnackbar(view, "Fill required fields", R.color.error);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(1).setChecked(true);
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

        // handle special case for Eevee - multiple evolutions
        if (pokemon.getName().startsWith("Eevee")) {
            Pokemon eevee = new Pokemon("Eevee", pokemon.getCandyNeeded(), pokemon.getMinMult(), pokemon.getMaxMult(), pokemon.getAvgMult());
            dataList.add(new PokemonDataCP(eevee, cp, getPokemonIcon(eevee)));
            cp = (int) (cp * eevee.getAvgMult());
            dataList.add(new InfoDataCP(eevee.getCandyNeeded(), eevee.getAvgMult()));

            // get the evolution name : "Eevee -> " = 9 length
            Pokemon eeveeEvolution = new Pokemon(pokemon.getName().substring(9));
            dataList.add(new PokemonDataCP(eeveeEvolution, cp, getPokemonIcon(eeveeEvolution)));

        } else {
            int familyIndex = pokemon.getFamilyIndex();

            // add pokemon and all its evolutions as data row objects and add them to the list view
            for (int i = familyIndex; i < pokemon.getFamily().size(); i++) {
                if (i != familyIndex)   // only add pokemon in between info data, so ignore the first iteration
                    dataList.add(new InfoDataCP(pokemon.getFamily().get(i - 1).getCandyNeeded(), pokemon.getFamily().get(i - 1).getAvgMult()));

                dataList.add(new PokemonDataCP(pokemon.getFamily().get(i), cp, getPokemonIcon(pokemon.getFamily().get(i))));
                cp = (int) (cp * pokemon.getFamily().get(i).getAvgMult());

            }
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
//        int id = item.getItemId();
//
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

        } else if (id == R.id.nav_xp) {
            startActivity(new Intent(CombatPowerActivity.this, ExperienceActivity.class));
            overridePendingTransition(R.anim.pull_activity_in_left, R.anim.push_activity_out_right);

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

