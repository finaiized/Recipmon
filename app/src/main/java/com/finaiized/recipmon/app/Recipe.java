package com.finaiized.recipmon.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recipe {

    public static final String bundleName = "name";
    public static final String bundleDescription = "description";
    public static final String bundleImage = "image";
    public String name;
    public String description;
    public String image;

    public Recipe(String n, String d, String i) {
        name = n;
        description = d;
        image = i;
    }


    public static void writePreferences(Activity a, List<Recipe> recipes) throws IOException {

        writePreferences(a, createJsonRecipeStream(recipes));

    }

    public static void writePreferences(Activity a, String recipes) {

        SharedPreferences sp = a.getSharedPreferences(
                a.getString(R.string.preference_key_recipe), Context.MODE_PRIVATE);

        // Write data
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(a.getString(R.string.preference_key_recipe), recipes);

        editor.apply();
    }

    private static String readPreferencesAsJson(Activity a) {
        SharedPreferences sp = a.getSharedPreferences(
                a.getString(R.string.preference_key_recipe), Context.MODE_PRIVATE);

        return sp.getString(a.getString(R.string.preference_key_recipe), "");
    }

    public static List<Recipe> readPreferencesAsList(Activity a) throws IOException {
        return readJsonRecipeStream(readPreferencesAsJson(a));
    }

    public static List<String> filterRecipeDataByName(List<Recipe> recipes) {
        List<String> names = new ArrayList<String>();
        for (Recipe r : recipes) {
            names.add(r.name);
        }
        return names;
    }

    public static void removeRecipeData(Recipe r, List<Recipe> recipes) {
        if (r.image != null) {
            File f = new File(r.image);
            f.delete();
        }
    }

    /**
     * Checks if the given recipe is considered complete (all necessary fields given)
     *
     * @param r The recipe to check
     * @return "" if there is no error; otherwise, the reason for the error
     */
    public static String verifyRecipeData(Recipe r) {
        if (r.name.equals("")) {
            return "The recipe must have a name.";
        }

        return "";
    }

    public static String loadSampleData() throws IOException {
        Recipe r1 = new Recipe("Sprinkle Cupcakes", "A treat for the youth among us!", null);
        Recipe r2 = new Recipe("Chocolate Pie", "Stupendous amounts of chocolate wrapping the classic pie.", null);
        List<Recipe> recipes = Arrays.asList(r1, r2);
        return createJsonRecipeStream(recipes);
    }

    private static String createJsonRecipeStream(List<Recipe> recipes) throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginArray();

        for (Recipe r : recipes) {
            addRecipe(r.name, r.description, r.image, writer);
        }

        writer.endArray();
        writer.flush();
        return sw.toString();
    }

    private static List<Recipe> readJsonRecipeStream(String s) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(s));

        try {
            return readRecipesArray(reader);
        } finally {
            reader.close();
        }
    }

    private static void addRecipe(String name, String description, String image, JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("name").value(name.trim());
        writer.name("description").value(description);
        if (image == null) {
            writer.name("image").nullValue();
        } else {
            writer.name("image").value(image);
        }
        writer.endObject();
    }

    private static List<Recipe> readRecipesArray(JsonReader reader) throws IOException {
        List<Recipe> recipes = new ArrayList<Recipe>();
        reader.beginArray();

        while (reader.hasNext()) {
            recipes.add(readRecipe(reader));
        }

        reader.endArray();
        return recipes;
    }

    private static Recipe readRecipe(JsonReader reader) throws IOException {
        String name = "";
        String description = "";
        String image = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("name")) {
                name = reader.nextString();
            } else if (key.equals("description")) {
                description = reader.nextString();
            } else if (key.equals("image") && reader.peek() != JsonToken.NULL) {
                image = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Recipe(name, description, image);
    }

    public static Recipe findRecipeByName(List<Recipe> recipes, String name) {
        for (Recipe r : recipes) {
            if (r.name.equals(name)) {
                return r;
            }
        }
        return null;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString(bundleName, name);
        b.putString(bundleDescription, description);
        b.putString(bundleImage, image);
        return b;
    }
}


