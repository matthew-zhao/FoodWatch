package com.foodwatch;

import android.os.AsyncTask;
import android.util.Log;

import com.foodwatch.activities.RecognizeConceptsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import clarifai2.dto.prediction.Concept;

public class FetchAPI extends AsyncTask<Void, Void, String[]> {

  private List<Concept> concept = new ArrayList<>();
  private RecognizeConceptsActivity rcActivity;

  public FetchAPI(List<Concept> concept, RecognizeConceptsActivity rcActivity) {
    super();
    this.concept = concept;
    this.rcActivity = rcActivity;
  }
  private final String LOG_TAG = FetchAPI.class.getSimpleName();

  @Override
  protected String[] doInBackground(Void... params) {
    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;

    // Will contain the raw JSON response as a string.
    String mobileResourceStr = null;

    try {
      String buffer_url = "";
      for (Concept c : concept) {
        buffer_url += "food=" + c.name() + "&";
      }

      URL url = new URL("http://40.118.164.231/api/v1/service?" + buffer_url.substring(0, buffer_url.length()-1));

      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.connect();

      // Read the input stream into a String
      InputStream inputStream = urlConnection.getInputStream();
      StringBuffer buffer = new StringBuffer();

      if (inputStream == null)
        return null;

      reader = new BufferedReader(new InputStreamReader(inputStream));

      String line;
      while ((line = reader.readLine()) != null) {
        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
        // But it does make debugging a *lot* easier if you print out the completed
        // buffer for debugging.
        buffer.append(line + "\n");
      }

      if (buffer.length() == 0)
        return null;

      mobileResourceStr = buffer.toString();
    } catch (IOException e) {
      Log.e(LOG_TAG, "Error ", e);
      return null;
    } finally {
      if (urlConnection != null)
        urlConnection.disconnect();

      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          Log.e(LOG_TAG, "Error closing stream", e);
        }
      }
    }

    try {
      String[] arr = getAPIData(mobileResourceStr);
      return arr;
    } catch (JSONException e) {
      Log.e(LOG_TAG, e.getMessage(), e);
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected void onPostExecute(String[] items) {
//    if (items != null && adapter != null) {
//      adapter.clear();
//      for (FoodItem item : items) {
//        adapter.add(item);
//      }
//    }
    //RecognizeConceptsAdapter adapter = new RecognizeConceptsAdapter();
    //adapter.setData(items);
    //results = items;
    rcActivity.display_UI(items);
  }

  private String[] getAPIData(String jsonStr) throws JSONException {
    //JSONArray dataArray = new JSONArray(jsonStr);
    JSONObject food = new JSONObject(jsonStr);
    List<String> listData = new ArrayList<>();
    listData.add(food.getString("name"));
    listData.add(food.getString("calories"));
    listData.add(food.getString("protein"));
    listData.add(food.getString("total_fat"));
    listData.add(food.getString("carbs"));
    listData.add(food.getString("fiber"));
    listData.add(food.getString("sugars"));
    listData.add(food.getString("calcium"));
    listData.add(food.getString("iron"));
    listData.add(food.getString("potassium"));
    listData.add(food.getString("sodium"));
    listData.add(food.getString("vit_c"));
    //FoodItem foodItem = new FoodItem(name, info, ordinal, kind, content, phone, url);

    Log.d("test", listData.toString());
    return listData.toArray(new String[listData.size()]);
  }
}
