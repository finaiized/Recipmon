package com.finaiized.recipmon.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_main, new MainActivityFragment())
                    .commit();
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.add_recipe:
                startActivity(new Intent(this, AddRecipeActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    public static class MainActivityFragment extends Fragment {

        public static final String RECIPE_NAME_PRESSED = "com.finaiized.recipmon.MainActivity" +
                ".MainActivityFragment.RECIPE_NAME_PRESSED";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onResume() {
            super.onResume();
            /* Uncomment to load sample data for the first run
            try {
                Recipe.writePreferences(getActivity(), Recipe.loadSampleData());
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            final List<Recipe> recipeDataSourceList = new ArrayList<Recipe>();

            // Read it back
            try {
                recipeDataSourceList.addAll(Recipe.readPreferencesAsList(getActivity()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ListView listView = (ListView) getActivity().findViewById(R.id.recipeList);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, Recipe.filterRecipeDataByName(recipeDataSourceList)));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String title = (String) adapterView.getItemAtPosition(i);
                    Recipe recipe = Recipe.findRecipeByName(recipeDataSourceList, title);

                    if (recipe != null) {
                        Intent intent = new Intent(getActivity(), RecipeViewActivity.class);
                        intent.putExtra(RECIPE_NAME_PRESSED, recipe.toBundle());
                        startActivity(intent);
                    }
                }
            });
        }
    }
}
