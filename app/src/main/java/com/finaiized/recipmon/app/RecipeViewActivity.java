package com.finaiized.recipmon.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class RecipeViewActivity extends Activity {
    public static Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        Bundle b = i.getBundleExtra(MainActivity.MainActivityFragment.RECIPE_NAME_PRESSED);
        currentRecipe = new Recipe(b.getString(Recipe.bundleName),
                b.getString(Recipe.bundleDescription), b.getString(Recipe.bundleImage));

        setContentView(R.layout.activity_recipe_view);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_recipe, new RecipeViewFragment())
                    .commit();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipe_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_delete:
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
                deleteDialog.setMessage(R.string.delete_message);
                deleteDialog.setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            List<Recipe> recipes = Recipe.readPreferencesAsList(RecipeViewActivity.this);
                            Recipe r = Recipe.findRecipeByName(recipes, currentRecipe.name);
                            if (r.image != null) {
                                File f = new File(r.image);
                                f.delete();
                            }
                            recipes.remove(r);
                            Recipe.writePreferences(RecipeViewActivity.this, recipes);
                            Toast.makeText(RecipeViewActivity.this, R.string.delete_recipe_confirmation, Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(RecipeViewActivity.this, MainActivity.class));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                deleteDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                deleteDialog.create();
                deleteDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class RecipeViewFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View name = inflater.inflate(R.layout.fragment_recipe_view, container, false);
            TextView tv = ((TextView) name.findViewById(R.id.recipe_view_name));
            tv.setText(currentRecipe.name);
            tv.setSelected(true); // enables ellipsize: marquee

            TextView description = ((TextView) name.findViewById(R.id.recipe_description_label));
            description.setText(currentRecipe.description);

            if (currentRecipe.image != null) {
                ImageView image = ((ImageView) name.findViewById(R.id.recipe_view_image));
                image.setImageBitmap(BitmapFactory.decodeFile(currentRecipe.image));
            }

            getActivity().getActionBar().setTitle(currentRecipe.name);
            return name;
        }
    }
}
