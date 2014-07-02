package com.finaiized.recipmon.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class EditRecipeActivity extends Activity {
    // Keep track of loaded and saved images
    private static Bitmap loadedImage;
    private static String photoUri;
    private static String prevPhotoUri;

    // For editing recipes only
    private static Recipe editedRecipe;
    private Boolean isEditing = false;
    private String editedPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_add_recipe, new AddActivityFragment())
                    .commit();
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Determine if the recipe is being edited or being created
        Bundle b = getIntent().getBundleExtra(RecipeViewActivity.RECIPE_EDIT);
        if (b != null) {
            editedRecipe = new Recipe(b.getString(Recipe.bundleName),
                    b.getString(Recipe.bundleDescription), b.getString(Recipe.bundleImage));
            isEditing = true;
            editedPhotoUri = editedRecipe.image;
            getActionBar().setTitle(editedRecipe.name);
        } else {
            editedRecipe = null;
            getActionBar().setTitle(R.string.add_recipe);
        }

        // Reset every time a new activity is created
        loadedImage = null;
        photoUri = null;
        prevPhotoUri = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release last image captured and saved by the camera
        if (prevPhotoUri != null) {
            File f = new File(prevPhotoUri);
            f.delete();
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
                String recipeName = ((EditText) findViewById(R.id.editTextRecipeName)).getText().toString();
                String recipeDescription = ((EditText) findViewById(R.id.editTextRecipeDescription)).getText().toString();

                Recipe newRecipe = new Recipe(recipeName, recipeDescription, null);

                String status = Recipe.verifyRecipeData(newRecipe);
                if (!status.equals("")) {
                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    return false;
                }

                String recipeImagePath;

                try {
                    // Has a new image been created or loaded?
                    if (photoUri != null || loadedImage != null) {
                        // Remove the old image
                        if (isEditing && editedPhotoUri != null) {
                            File f = new File(editedPhotoUri);
                            f.delete();
                        }
                        // Assign path based on whether the image was chosen or taken
                        if (loadedImage != null) {
                            recipeImagePath = saveSelectedImage(loadedImage);
                        } else {
                            recipeImagePath = photoUri;
                        }
                    } else {
                        recipeImagePath = editedPhotoUri;
                    }
                    newRecipe.image = recipeImagePath;

                    List<Recipe> recipes = Recipe.readPreferencesAsList(this);
                    if (!isEditing) {
                        recipes.add(newRecipe);
                        Toast.makeText(this, R.string.add_recipe_confirmation, Toast.LENGTH_SHORT).show();
                    } else {
                        Recipe r = Recipe.findRecipeByName(recipes, editedRecipe.name);
                        recipes.set(recipes.indexOf(r), newRecipe);
                        Toast.makeText(this, R.string.edit_recipe_confirmation, Toast.LENGTH_SHORT).show();
                    }

                    Recipe.writePreferences(this, recipes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startActivity(new Intent(this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private File createLocalImageFile() {
        // From http://developer.android.com/training/camera/photobasics.html
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File file = new File(storageDir, imageFileName + ".jpg");
        photoUri = file.getAbsolutePath();
        return file;
    }

    private String saveSelectedImage(Bitmap bmp) throws IOException {

        FileOutputStream out;
        File imgFile = createLocalImageFile();
        out = new FileOutputStream(imgFile);
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
        out.close();

        return imgFile.getAbsolutePath();
    }


    public static class AddActivityFragment extends Fragment implements View.OnClickListener {
        static final int PICK_IMAGE_REQUEST = 1;

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
                // Remove previous data
                loadedImage = null;
                if (prevPhotoUri != null) {
                    File f = new File(prevPhotoUri);
                    f.delete();
                }

                // Image from the gallery
                if (data != null) {
                    // Show - but don't save a copy of - the selected image
                    Uri img = data.getData();
                    try {
                        loadedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), img);
                        ImageView iv = (ImageView) getActivity().findViewById(R.id.add_recipe_image_view);
                        iv.setImageBitmap(loadedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    // Image from camera
                    ImageView iv = (ImageView) getActivity().findViewById(R.id.add_recipe_image_view);
                    Bitmap bmp = BitmapFactory.decodeFile(photoUri);
                    iv.setImageBitmap(bmp);
                    prevPhotoUri = photoUri;
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);
            Button addImageButton = (Button) view.findViewById(R.id.add_image_button);
            addImageButton.setOnClickListener(this);

            // Default image
            ImageView iv = (ImageView) view.findViewById(R.id.add_recipe_image_view);
            iv.setImageResource(R.drawable.pink_cupcake);

            if (editedRecipe != null) {
                EditText name = (EditText) view.findViewById(R.id.editTextRecipeName);
                name.setText(editedRecipe.name);
                EditText description = (EditText) view.findViewById(R.id.editTextRecipeDescription);
                description.setText(editedRecipe.description);
                if (editedRecipe.image != null) {
                    iv.setImageBitmap(BitmapFactory.decodeFile(editedRecipe.image));
                }
            }
            return view;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.add_image_button:
                    Intent pickImageIntent = new Intent();
                    pickImageIntent.setType("image/*");
                    pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        File photo;
                        photo = ((EditRecipeActivity) getActivity()).createLocalImageFile();
                        if (photo != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                        }
                    }

                    Intent chooser = Intent.createChooser(pickImageIntent, getString(R.string.choose_image));
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});

                    startActivityForResult(chooser, PICK_IMAGE_REQUEST);
                    break;
            }
        }
    }
}
