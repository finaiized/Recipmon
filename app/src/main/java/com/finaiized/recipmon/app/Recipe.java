package com.finaiized.recipmon.app;

import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Recipe {

    public String name;
    public String description;
    public String image;

    public static String bundleName = "name";
    public static String bundleDescription = "description";
    public static String bundleImage = "image";

    public Recipe(String n, String d, String i) {
        name = n;
        description = d;
        image = i;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString(bundleName, name);
        b.putString(bundleDescription, description);
        b.putString(bundleImage, image);
        return b;
    }

    public static List<String> filterRecipeDataByName(List<Recipe> recipes) {
        List<String> names = new ArrayList<String>();
        for (Recipe r : recipes) {
            names.add(r.name);
        }
        return names;
    }

    public static String loadSampleData() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginArray();

        addRecipe("Sprinkle Cupcakes", null, "", writer);
        addRecipe("Chocolate Pie", null, "", writer);

        writer.endArray();
        writer.flush();
        return sw.toString();
    }

    public static void addRecipe(String name, String image, String description, JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("name").value(name);
        if (image == null) {
            writer.name("image").nullValue();
        } else {
            writer.name("image").value(description);
        }
        writer.name("description").value(description);
        writer.endObject();
    }

    public static List<Recipe> readJsonRecipeStream(String s) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(s));

        try {
            return readRecipesArray(reader);
        } finally {
            reader.close();
        }
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
            } else if (name.equals("description")) {
                description = reader.nextString();
            } else if (name.equals("image") && reader.peek() != JsonToken.NULL) {
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
            if (r.name == name) {
                return r;
            }
        }
        return null;
    }
}


