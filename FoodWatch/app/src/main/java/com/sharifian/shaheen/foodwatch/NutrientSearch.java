package com.sharifian.shaheen.foodwatch;

/**
 * Created by Yiding on 10/10/2015.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.lang.StringBuilder;
import com.google.gson.Gson;
import com.google.gson.*;

import java.io.IOException;
import java.net.MalformedURLException;

class Nutrient {

    public static void main(String[] args) {

        Nutrient n = new Nutrient();
        System.out.println(n.getNutrient(args[0]));

    }

    public String getNutrient(String food_name) {
        NutritionSearch n_search = new NutritionSearch(food_name);
        String ndbno = n_search.search(food_name, 0);
        if (ndbno.equals("FOOD NOT FOUND")) {
            return null;
        }
        IDSearch i_search = new IDSearch(ndbno);
        String label = i_search.energy_retrieve(ndbno);
        if (label.equals("FOOD NOT FOUND")) {
            return null;
        }
        return label;
    }

}

class NutritionSearch {

    public String _upper_url;
    public String _lower_url;
    public String _complete_url;

    public NutritionSearch(String food_name) {
        this._upper_url = "http://api.nal.usda.gov/ndb/search/?format=json&q=";
        this._lower_url = "&sort=n&max=10&offset=0&api_key=gSICsF7KdH8nmOuEBYnAQAXVKH9by1yIStw5IzC0";
        this._complete_url = this._upper_url + food_name + this._lower_url; //concatenate the url and food name for api search
    }

    public NutritionSearch() {
        //does nothing
    }

    /*
        Arguments:
            name -- the name of the food
            index -- the index of the search result intended
        Return:
            the id of the food at the index
        Error:
            if the food is not found
    */
    public String search(String name, int index) {
            String food_name = name;
            NutritionSearch n_search = new NutritionSearch(food_name);
            DownloadJson json = new DownloadJson(this._complete_url);
            String nutrition_json = json.download();
            String food_id = id_retrieve(index, nutrition_json);
            return food_id;
    }

    public String id_retrieve(int index, String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject obj = element.getAsJsonObject();
        JsonObject list_arr = obj.getAsJsonObject("list");
        JsonObject item_arr = list_arr.getAsJsonArray("item").get(index).getAsJsonObject();
        String rtn_id = item_arr.get("ndbno").getAsString();
        return rtn_id;
    }

}

class IDSearch extends NutritionSearch {

    public IDSearch(String ndbno) {
        _upper_url = "http://api.nal.usda.gov/ndb/reports/?ndbno=";
        _lower_url = "&type=f&format=json&api_key=gSICsF7KdH8nmOuEBYnAQAXVKH9by1yIStw5IzC0";
        _complete_url = _upper_url + ndbno + _lower_url;
    }

    public String energy_retrieve(String food_id) {
        IDSearch n_search = new IDSearch(food_id);
        DownloadJson json = new DownloadJson(this._complete_url);
        String nutrition_json = json.download();
        String energy = fact_retrieve(nutrition_json);
        return energy;
    }

    public String fact_retrieve(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject obj = element.getAsJsonObject();
        JsonObject list_arr = obj.getAsJsonObject("report");
        JsonObject ingredient_info = list_arr.getAsJsonObject("food").getAsJsonArray("nutrients").get(2).getAsJsonObject();
        int measurement = ingredient_info.getAsJsonPrimitive("value").getAsInt();
        String rtn = measurement + "" + " kcal per 100 g";
        return rtn;
    }

}

class DownloadJson {
    String _url;

    public DownloadJson(String url) {
        this._url = url;
    }

    public String download() {
        StringBuilder output = new StringBuilder(100);
        try{
            URL target_url = new URL(this._url);
            URLConnection con = target_url.openConnection();
            InputStream is =con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;

            while ((line = br.readLine()) != null) {
                output.append(line);
            }
        } catch (IOException e) {
            return "DOWNLOAD ERROR";
        }

        return output.toString();
    }
}

