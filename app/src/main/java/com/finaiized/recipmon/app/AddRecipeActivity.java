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


public class AddRecipeActivity extends Activity {
    private static Bitmap loadedImage;
    private static String photoLocation;
    private static String prevPhotoLocation;

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
    protected void onDestroy() {
        super.onDestroy();

        // Release last image captured and saved by the camera
        if (prevPhotoLocation != null) {
            File f = new File(prevPhotoLocation);
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
                try {
                    List<Recipe> recipes = Recipe.readPreferencesAsList(this);
                    String recipeName = ((EditText) findViewById(R.id.editTextRecipeName)).getText().toString();
                    String recipeDescription = ((EditText) findViewById(R.id.editTextRecipeDescription)).getText().toString();
                    String recipeImagePath = saveSelectedImage();
                    if (saveSelectedImage() == null) {
                        recipeImagePath = photoLocation;
                    }

                    Recipe newRecipe = new Recipe(recipeName, recipeDescription, recipeImagePath);
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

    private File createLocalImageFile() {
        // From http://developer.android.com/training/camera/photobasics.html
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File file = new File(storageDir, imageFileName + ".jpg");
        photoLocation = file.getAbsolutePath();
        return file;
    }

    private String saveSelectedImage() throws IOException {
        if (loadedImage == null) return null;

        FileOutputStream out;
        File imgFile = createLocalImageFile();
        out = new FileOutputStream(imgFile);
        loadedImage.compress(Bitmap.CompressFormat.JPEG, 85, out);
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
                if (prevPhotoLocation != null) {
                    File f = new File(prevPhotoLocation);
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

                } else { // Image from camera
                    ImageView iv = (ImageView) getActivity().findViewById(R.id.add_recipe_image_view);
                    Bitmap bmp = BitmapFactory.decodeFile(photoLocation);
                    iv.setImageBitmap(bmp);
                    prevPhotoLocation = photoLocation;
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);
            Button addImageButton = (Button) view.findViewById(R.id.add_image_button);
            addImageButton.setOnClickListener(this);
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
                        photo = ((AddRecipeActivity) getActivity()).createLocalImageFile();
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
