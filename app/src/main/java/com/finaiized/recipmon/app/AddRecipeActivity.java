package com.finaiized.recipmon.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public class AddRecipeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_add_recipe, new AddActivityFragment())
                    .commit();
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.add_recipe);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.add_recipe_done:
                try {
                    List<Recipe> recipes = Recipe.readPreferencesAsList(this);
                    String recipeName = ((EditText) findViewById(R.id.editTextRecipeName)).getText().toString();
                    String recipeDescription = ((EditText) findViewById(R.id.editTextRecipeDescription)).getText().toString();

                    Recipe newRecipe = new Recipe(recipeName, recipeDescription, "");
                    String status = Recipe.verifyRecipeData(newRecipe);
                    if (!status.equals("")) {
                        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    recipes.add(newRecipe);
                    Recipe.writePreferences(this, recipes);

                    Toast.makeText(this, R.string.add_recipe_confirmation, Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, MainActivity.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return super.onOptionsItemSelected(item);
    }


    public static class AddActivityFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_add_recipe, container, false);
        }
    }
}
