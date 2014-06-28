package com.finaiized.recipmon.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
                return true;
            case R.id.add_recipe:
                startActivity(new Intent(this, AddRecipeActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    public static class MainActivityFragment extends Fragment {

        private List<String> recipeDataSourceList = new ArrayList<String>();

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
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            SharedPreferences sp = getActivity().getSharedPreferences(
                    getString(R.string.preference_key_recipe), MODE_PRIVATE);

            // Write data
            SharedPreferences.Editor editor = sp.edit();
            try {
                editor.putString(getString(R.string.preference_key_recipe), Recipe.loadSampleData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            editor.commit();

            // Read it back
            try {
                String json = sp.getString(getString(R.string.preference_key_recipe), "");
                recipeDataSourceList.addAll(Recipe.filterRecipeDataByName(
                        Recipe.readJsonRecipeStream(json)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ListView listView = (ListView) getActivity().findViewById(R.id.recipeList);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, recipeDataSourceList));
        }
    }
}
