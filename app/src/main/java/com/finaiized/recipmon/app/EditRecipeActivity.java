package com.finaiized.recipmon.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            editedRecipe = Recipe.fromBundle(b);
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
                String uid = editedRecipe != null ? editedRecipe.uid : null;

                Recipe newRecipe = new Recipe(recipeName, recipeDescription, null, uid);

                String status = Recipe.verifyRecipeData(newRecipe);
                if (!status.equals("")) {
                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    return false;
                }

                String recipeImagePath;

                try {
                    if (newImageCreatedOrLoaded()) {
                        if (oldImageExists()) {
                            new File(editedPhotoUri).delete();
                        }
                        recipeImagePath = imageWasChosen() ? saveImage(loadedImage) : photoUri;
                    } else {
                        recipeImagePath = editedPhotoUri;
                    }
                    newRecipe.image = recipeImagePath;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                List<String> steps = new ArrayList<String>();
                LinearLayout stepView = (LinearLayout) findViewById(R.id.edit_view_steps);
                for (int i = 0; i < stepView.getChildCount(); i++) {
                    View stepChild = stepView.getChildAt(i);
                    if (stepChild instanceof EditText) {
                        String step = ((EditText) stepChild).getText().toString().trim();
                        if (!step.isEmpty()) {
                            steps.add(step);
                        }
                    }
                }
                newRecipe.steps = steps.toArray(new String[steps.size()]);

                try {
                    List<Recipe> recipes = Recipe.readPreferencesAsList(this);
                    if (!isEditing) {
                        recipes.add(newRecipe);
                        Toast.makeText(this, R.string.add_recipe_confirmation, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        Recipe r = Recipe.findRecipeById(recipes, editedRecipe.uid);
                        recipes.set(recipes.indexOf(r), newRecipe);
                        Toast.makeText(this, R.string.edit_recipe_confirmation, Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(this, RecipeViewActivity.class);
                        i.putExtra(MainActivity.MainActivityFragment.RECIPE_NAME_PRESSED, newRecipe.toBundle());
                        startActivity(i);
                    }

                    Recipe.writePreferences(this, recipes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean imageWasChosen() {
        return loadedImage != null;
    }

    private boolean oldImageExists() {
        return isEditing && editedPhotoUri != null;
    }

    private boolean newImageCreatedOrLoaded() {
        return photoUri != null || loadedImage != null;
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

    private String saveImage(Bitmap bmp) throws IOException {

        FileOutputStream out;
        File imgFile = createLocalImageFile();
        out = new FileOutputStream(imgFile);
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
        out.close();

        return imgFile.getAbsolutePath();
    }

    /* From http://stackoverflow.com/questions/4005728/hide-default-keyboard-on-click-in-android/7241790#7241790
        Used to hide the keyboard when any other view is pressed.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }


    public static class AddActivityFragment extends Fragment implements View.OnClickListener {
        static final int PICK_IMAGE_REQUEST = 1;

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST) {
                if (resultCode == RESULT_OK) {
                    // Remove previous data
                    loadedImage = null;
                    if (prevPhotoUri != null) {
                        new File(prevPhotoUri).delete();
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
                } else {
                    // If the request fails, undo createLocalImageFile()
                    new File(photoUri).delete();
                    photoUri = null;
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

            final LinearLayout stepView = (LinearLayout) view.findViewById(R.id.edit_view_steps);
            stepView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    child.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                // Add a new EditText right below this one unless one already exists or this one is empty
                                int index = stepView.indexOfChild(view);
                                EditText et = (EditText) stepView.getChildAt(index + 1);
                                if ((et == null || (!(et.getText().toString().trim().isEmpty()))) &&
                                        !((EditText) view).getText().toString().trim().isEmpty()) {
                                    stepView.addView(getActivity().getLayoutInflater().inflate(R.layout.edit_step_template, stepView, false), index + 1);
                                    stepView.getChildAt(index + 1).requestFocus();
                                } else {
                                    EditText v = (EditText) stepView.getChildAt(index + 1);
                                    if (v != null) {
                                        v.requestFocus();
                                        v.setSelection(v.getText().length());
                                    }
                                }
                                return true;
                            } else if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                                    (keyCode == KeyEvent.KEYCODE_DEL)) {
                                if (view instanceof EditText) {
                                    EditText step = (EditText) view;
                                    if (step.getText().toString().isEmpty()) {
                                        int index = stepView.indexOfChild(view);
                                        if (index == 0) return true;
                                        stepView.removeView(view);
                                        EditText et = (EditText) stepView.getChildAt(index - 1);
                                        if (et != null) {
                                            et.requestFocus();
                                            et.setSelection(et.getText().length());
                                        }
                                    }
                                }
                                return false;
                            }
                            return false;
                        }
                    });
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {

                }
            });

            if (editedRecipe != null) {
                EditText name = (EditText) view.findViewById(R.id.editTextRecipeName);
                name.setText(editedRecipe.name);

                EditText description = (EditText) view.findViewById(R.id.editTextRecipeDescription);
                description.setText(editedRecipe.description);

                if (editedRecipe.image != null) {
                    iv.setImageBitmap(BitmapFactory.decodeFile(editedRecipe.image));
                }

                for (String step : editedRecipe.steps) {
                    EditText stepText = ((EditText) inflater.inflate(R.layout.edit_step_template, stepView, false));
                    stepText.setText(step);
                    stepView.addView(stepText);
                }
            }

            // Add a step view as a placeholder for the next step
            stepView.addView(inflater.inflate(R.layout.edit_step_template, stepView, false));
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
