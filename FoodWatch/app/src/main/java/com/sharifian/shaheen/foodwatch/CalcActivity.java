package com.sharifian.shaheen.foodwatch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.Window;


import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalcActivity extends AppCompatActivity {
    public static final String TAG = "CalcActivity";
    public static final String DB_NAME = "Food";
    protected static Manager manager;
    private Database database;
    //A ProgressDialog object
    private ProgressDialog progressDialog;

    private static final String APP_ID = "u4poK47im7v30avUqTMbK7NGQwJjEPpQ4ezpyzx5";
    private static final String APP_SECRET = "TdU7UbHw0-A5-F0yUiIY0KRklvgunEiAFw9A_iJY";
    private ClarifaiClient client = new ClarifaiClient(APP_ID, APP_SECRET);
    private String[] photo_path = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        Intent intent = getIntent();
        photo_path[0] = intent.getStringExtra("CurrentPhotoPath");
        createDB(); //create database

        String[] tags = {"dairy product", "dairy", "slice", "butter"};
        populate("butter", tags);
        String[] tags2 = {"cheese", "cheddar", "dairy", "milk"};
        populate("cheese", tags2);
        String[] tags3 = {"milk", "drink", "glass", "breakfast", "yogurt"};
        populate("milk", tags3);
        String[] tags4 = {"bread", "wheat", "loaf", "food", "flour", "slice", "bakery"};
        populate("bread", tags4);
        String[] tags5 = {"pork", "meat", "cooking", "dish"};
        populate("pork", tags5);
        String[] tags6 = {"tenderloin", "beef", "sirloin", "fillet", "rare", "meat"};
        populate("steak", tags6);
        String[] tags7 = {"egg", "breakfast", "egg yolk", "nutrition", "cholesterol"};
        populate("egg", tags7);
        String[] tags8 = {"candy", "color", "motley", "confection", "sugar", "assortment"};
        populate("candy", tags8);
        String[] tags9 = {"spaghetti", "cuisine", "lunch", "sauce", "tomato"};
        populate("spaghetti", tags9);
        String[] tags10 = {"tomato", "fruit", "vegetable", "healthy"};
        populate("tomato", tags10);
        String[] tags11 = {"sandwich", "bread", "ham", "fast", "bun"};
        populate("sandwich", tags11);
        String[] tags12 = {"chicken", "poultry", "food", "sauce"};
        populate("chicken", tags12);
        String[] tags13 = {"salad", "healthy", "vegetable", "diet"};
        populate("lettuce", tags13);
        String[] tags14 = {"salad", "tomato", "nutrition", "lollies"};
        populate("salad", tags14);
        String[] tags15 = {"soup", "bowl", "spoon", "broth"};
        populate("soup", tags15);
        String[] tags16 = {"water", "glass", "bottle", "drink", "liquid", "table"};
        populate("water", tags16);
        //Initialize a LoadViewTask object and call the execute() method
        new LoadViewTask().execute(photo_path);
    }
    public void populate(String food, String[] arr) {
        for (String s: arr) {
            update(s, food);
        }
    }

    /** Returns Tags for an array of images.
     *  If there is an error in processing the image, return an empty List of tags.
     *  If there is no files, return null and logs error. */
    private HashMap<File, List<Tag>> getTags(File[] files) {
        if(files == null) {
            Log.e(TAG, "no Files found in getTags().");
            return null;
        }
        HashMap<File, List<Tag>> result = new HashMap<File, List<Tag>>();
        //max size for each batch of inputs is 128 images
        if(files.length > 128) {
            File[] newFiles = Arrays.copyOfRange(files, 128, files.length - 1);
            files = Arrays.copyOfRange(files, 0, 127);
            result.putAll(getTags(newFiles));
        }

        try {
            for(File file : files) {
                List<RecognitionResult> results =
                        client.recognize(new RecognitionRequest(file));
                if (results.get(0).getStatusCode() != RecognitionResult.StatusCode.OK) {
                    Log.e(TAG, file.toString() + "'s statuscode is not ok in getTags().");
                    result.put(file, new ArrayList<Tag>());
                } else {
                    result.put(file, results.get(0).getTags());
                }
            }

        } catch (ClarifaiException e) {
            Log.e(TAG, "Clarifai error", e);
            return null;
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<String, Integer, String[]>
    {
        //Before running code in separate thread
        @Override
        protected void onPreExecute()
        {
            //Create a new progress dialog
            progressDialog = new ProgressDialog(CalcActivity.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //Set the dialog title to 'Loading...'
            progressDialog.setTitle("Loading...");
            //Set the dialog message to 'Loading application View, please wait...'
            progressDialog.setMessage("Loading application View, please wait...");
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            //The maximum number of items is 100
            progressDialog.setMax(100);
            //Set the current progress to zero
            progressDialog.setProgress(0);
            //Display the progress dialog
            progressDialog.show();
        }

        //The code to be executed in a background thread.
        @Override
        protected String[] doInBackground(String... photo_path)
        {
            /* This is just a code that delays the thread execution 4 times,
             * during 850 milliseconds and updates the current progress. This
             * is where the code that is going to be executed on a background
             * thread must be placed.
             */
            //try
            //{
            //Get the current thread's token
            synchronized (this)
            {
                //getting the photo
                File[] file_arr = new File[1];
                //get the uri from the intent which contains extra content from the camera
                file_arr[0] = new File(photo_path[0]);
                HashMap<File, List<Tag>> pic_to_tag = getTags(file_arr);
                if (pic_to_tag == null) {
                    return null; // an error in clairifai occurred, do something?
                }
                publishProgress(33);
                Map<File, String> bestFood = calculate(pic_to_tag); //use daniel's function to calculate the name
                String[] foods = new String[bestFood.keySet().size()];
                int i = 0;
                for (File k : bestFood.keySet()) {
                    foods[i] = bestFood.get(k);
                    i++;
                }
                publishProgress(66);
                NutritionSearch dummy = new NutritionSearch();
                //calories per 100 grams
                String energy_content = dummy.search(foods[0], 0); //just calculate the first for now
                if (energy_content == null) {
                    return null; // an error in food search occured, do something?
                }
                publishProgress(100);
                String[] results = new String[2];
                results[0] = foods[0];
                results[1] = energy_content;
                return results;
            }
            //}
            //catch (InterruptedException e)
            //{
            //    e.printStackTrace();
            //}
        }

        //Update the progress
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            //set the current progress of the progress dialog
            progressDialog.setProgress(values[0]);
        }

        //after executing the code in the thread
        @Override
        protected void onPostExecute(String[] result)
        {
            //close the progress dialog
            progressDialog.dismiss();
            //initialize the View

            Intent intent = new Intent(getApplicationContext(), TaggingActivity.class);
            // TODO: Uncomment when Shaheen finishes this
            intent.putExtra("Photo", photo_path);
            intent.putExtra("Food", result[0]);
            intent.putExtra("Calories per 100 gram", result[1]);
            startActivity(intent);
            //setContentView(R.layout.main);
        }
    }

    // Calculates the number of occureneces of a tag within the database and returns the highest result
// associated with the food
    private Map<File, String> calculate(Map<File, List<Tag>> result) {
        Map<File, String> master = new HashMap<File, String>();
        for (File f : result.keySet()) {
            List<Tag> tags = result.get(f);
            Map<String, Integer> frequency = new HashMap<String, Integer>();
            for (Tag tag : tags) {
                String tagString = tag.toString();
                Set<String> results = (HashSet<String>) getFood(tagString); //todo
                for (String s : results) {
                    if (!frequency.containsKey(s)) {
                        frequency.put(s, 0);
                    }
                    frequency.put(s, frequency.get(s) + 1);
                }
            }
            String placeInMaster = determine(frequency);
            master.put(f, placeInMaster);
        }
        return master;
    }

    // Returns the string from map that occurs the most
    private String determine(Map<String, Integer> frequency) {
        String found = "";
        int max = 0;
        for (String s : frequency.keySet()) {
            if (frequency.get(s) > max) {
                found = s;
                max = frequency.get(s);
            }
        }
        return found;
    }

    private void createDB() {
        //Manager manager = null;
        //Database database = null;
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(DB_NAME);
            View viewFoods = database.getView("foods");
            viewFoods.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    Object tag = document.get("tag");
                    if (tag != null) {
                        emitter.emit(tag.toString(), null);
                    }
                }
            }, "1.1.0");
        } catch (Exception e) {
            Log.e(TAG, "Error getting database", e);
            return;
        }
        //Follows CRUD: Create, Retrieve, Update, Delete (Add?)
        // Create the document
        
    }

    // We make Manager/Database references singletons
    public Database getDatabaseInstance() throws CouchbaseLiteException {
        if ((this.database == null) & (this.manager != null)) {
            this.database = manager.getDatabase(DB_NAME);
        }
        return database;
    }
    public Manager getManagerInstance() throws IOException {
        if (manager == null) {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
        }
        return manager;
    }

    public void createTagFood(String tag, HashSet<String> food) {
        // Create a new document and add date
        Document document = database.createDocument();
        //docId = document.getId();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tag", tag);
        map.put("food", food);
        //map.put(tag, food);
        try {
            // Save the properties to the document
            document.putProperties(map);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
        //return docId;
    }

    //ths should actually return a hashset
    public Object getFood(String tag) {
        // Finding all documents, then looking ones with that specific tag
        //Query query = database.createAllDocumentsQuery();
        Query query = database.getView("foods").createQuery();
        List<Object> keys = new ArrayList<Object>();
        keys.add(tag);
        query.setKeys(keys);
        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
        QueryRow row = null;
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            row = it.next();
        }
        Document retrievedDocument = row.getDocument();
        Map<String, Object> tagToFood = new HashMap<String, Object>();
        tagToFood.putAll(retrievedDocument.getProperties());
        return tagToFood.get("food");
    }

    public void update(String tag, String food) {
        Query query = database.getView("foods").createQuery();
        List<Object> keys = new ArrayList<Object>();
        keys.add(tag);
        query.setKeys(keys);
        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Conflict or something happened", e);
        }
        QueryRow row = null;
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            row = it.next();
        }
        //no tag like this was ever created, then we need to create a new document
        if (row == null) {
            HashSet<String> newFoods = new HashSet<String>();
            newFoods.add(food);
            createTagFood(tag, newFoods);
        } else {
            Document document = row.getDocument();
            try {
                Map<String, Object> tagToFood = new HashMap<String, Object>(document.getProperties());
                HashSet<String> foods = (HashSet<String>) tagToFood.get("food");
                foods.add(food);
                tagToFood.put("food", food);
                document.putProperties(tagToFood);
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Conflict or something happened", e);
            }
        }
    }

}
