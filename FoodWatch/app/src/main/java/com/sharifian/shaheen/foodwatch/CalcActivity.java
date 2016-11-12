package com.sharifian.shaheen.foodwatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.TextView;


import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class CalcActivity extends Activity {
    private MobileServiceClient mClient;
    private MobileServiceTable<TagToFood> mTagToFood;
    private List<TagToFood> tagToFoodList;
    private HashSet<String> foodSet;
    public static final String TAG = "CalcActivity";
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
        try {
            mClient = new MobileServiceClient("https://foodweb.azure-mobile.net/", "KfCuCQzAIBJyEsZXdMGSUkEkxNFZPk98", this).withFilter(new ProgressFilter());
            Log.d(TAG, "We have created mobile service client");
            mTagToFood = mClient.getTable(TagToFood.class);
        } catch (MalformedURLException e) {
            Log.e(TAG, "URL for azure broken");
        }
        //createDB(); //create database
    /*
        String[] tags = {"dairy product", "dairy", "slice", "butter"};
        populate("butter", tags);*/

        String[] tag = {"dairy product"};
        populate("butter", tag);
/*
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
        populate("water", tags16);*/
        try {
            initLocalStore().get();
        } catch (Exception e) {
            Log.e(TAG,"Error caught: " + e);
        }

        //if (pic_to_tag == null) {
        //    return null; // an error in clairifai occurred, do something?
        //}
        //Initialize a LoadViewTask object and call the execute() method
        new LoadViewTask().execute(photo_path);
    }
    public void populate(String food, String[] arr) {
        for (String s : arr) {
            Log.d(TAG, "Got to populate");
            update(s, food);
            Log.d(TAG, "Got after update");
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
                List<RecognitionResult> results = client.recognize(new RecognitionRequest(file));
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
    private class LoadViewTask extends AsyncTask<String, Integer, HashMap<File, List<Tag>>>
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
        protected HashMap<File, List<Tag>> doInBackground(String... photo_path)
        {
            /* This is just a code that delays the thread execution 4 times,
             * during 850 milliseconds and updates the current progress. This
             * is where the code that is going to be executed on a background
             * thread must be placed.
             */
            HashMap<File, List<Tag>> pic_to_tag = null;
            try
            {
                //Get the current thread's token
                synchronized (this)
                {
                    File[] file_arr = new File[1];
                    //get the uri from the intent which contains extra content from the camera
                    file_arr[0] = new File(photo_path[0]);
                    pic_to_tag = getTags(file_arr);
                    return pic_to_tag;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return pic_to_tag;
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
        protected void onPostExecute(HashMap<File, List<Tag>> pic_to_tag)
        {
            //close the progress dialog
            progressDialog.dismiss();
            //initialize the View

            Map<File, String> bestFood = calculate(pic_to_tag); //use daniel's function to calculate the name
            String[] foods = new String[bestFood.keySet().size()];
            int i = 0;
            for (File k : bestFood.keySet()) {
                foods[i] = bestFood.get(k);
                i++;
            }
            NutritionSearch dummy = new NutritionSearch(foods[0]);
            //calories per 100 grams
            String energy_content = dummy.search(foods[0], 0); //just calculate the first for now
            //if (energy_content == null) {
            //    return null; // an error in food search occurred, do something?
            //}
            String[] result = new String[2];
            result[0] = foods[0];
            result[1] = energy_content;
            Intent intent2 = new Intent(getApplicationContext(), TaggingActivity.class);
            // TODO: Uncomment when Shaheen finishes this
            intent2.putExtra("Photo", photo_path);
            intent2.putExtra("Food", result[0]);
            intent2.putExtra("Calories per 100 gram", result[1]);
            startActivity(intent2);
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
                Object o = getFood(tagString); //todo
                if (o == null) {
                    continue;
                }
                Set<String> results = (HashSet<String>) o;
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

    public void createTagFood(String tag, HashSet<String> food) {

        final TagToFood newMapping = new TagToFood(tag, convertToString(food));
        //AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            //@Override
            //protected Void doInBackground(Void... params) {
                try {
                    mTagToFood.insert(newMapping).get();
                } catch (final Exception e) {
                    Log.e(TAG, "Exceptions");
                }
                //return null;
            //}
        //};

        //runAsyncTask(task);
    }

    //ths should actually return a hashset
    public Object getFood(final String tag) {
        /*final MobileServiceList<TagToFood> result;
        try {
            // this should only return 1 result, for loop below should only run once
            ListenableFuture<MobileServiceList<TagToFood>> process = mTagToFood.where().field("tag").eq(tag).execute();
            result = process.get(3, TimeUnit.SECONDS);
            TagToFood mapping = null;
            for (TagToFood map : result) {
                map = mapping;
            }
            if (mapping != null) {
                return convertToHashSet(mapping.foods);
            }
        } catch (Exception e) {
            Log.e(TAG, "Some exception caught:" + e);
        }
        return null;*/
        //AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            //@Override
            //protected Void doInBackground(Void... params) {
                HashSet<String> foodSet = null;
                try {
                    List<TagToFood> results = refreshItemsFromMobileServiceTable(tag);
                    TagToFood mapping = null;
                    for (TagToFood map : results) {
                        mapping = map;
                    }
                    if (mapping != null) {
                        foodSet = convertToHashSet(mapping.getFoods());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Some exception caught:" + e);
                }
                //return foodSet;
                //return null;
            //}
            /*
            @Override
            protected void onPostExecute(String[] result)
            {
                //close the progress dialog
                progressDialog.dismiss();
                //initialize the View

                if (result[0] == null && result[1] == null) {
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), TaggingActivity.class);
                // TODO: Uncomment when Shaheen finishes this
                intent.putExtra("Photo", photo_path);
                intent.putExtra("Food", result[0]);
                intent.putExtra("Calories per 100 gram", result[1]);
                startActivity(intent);
                //setContentView(R.layout.main);
            }*/
        //};
        //runAsyncTask(task);
        return foodSet;
    }

    public void update(final String tag, final String food) {
        //AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            //@Override
            //protected Void doInBackground(Void... params) {
                try {
                    List<TagToFood> results = refreshItemsFromMobileServiceTable(tag);

                    TagToFood mapping = null;
                    if (results != null) {
                        for (TagToFood map : results) {
                            mapping = map;
                        }
                        //adding food to the hashset of foods associated to that tag
                        HashSet<String> setFoods = convertToHashSet(mapping.getFoods());
                        setFoods.add(food);
                        String newFoodString = convertToString(setFoods);
                        mTagToFood.update(mapping);
                    } else {
                        //no tag so create one
                        HashSet<String> newFoodSet = new HashSet<String>();
                        newFoodSet.add(food);
                        TagToFood newMapping = new TagToFood(tag, convertToString(newFoodSet));
                        try {
                            mTagToFood.insert(newMapping);
                        } catch (Exception f) {
                            Log.e(TAG, "Exceptions");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "EXCEPTION");
                }
                //return null;
            //}
        //};
        //runAsyncTask(task);
        /*
        final MobileServiceList<TagToFood> result;
        try {
            result = mTagToFood.where().field("tag").eq(tag).execute().get();
            //result = process.get();
            TagToFood mapping = null;
            for (TagToFood map : result) {
                mapping = map;
            }
            if (mapping != null) {
                //adding food to the hashset of foods associated to that tag
                HashSet<String> setFoods = convertToHashSet(mapping.foods);
                setFoods.add(food);
                String newFoodString = convertToString(setFoods);
                mTagToFood.update(mapping).get(1, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Some exception caught: " + e);
            //no tag so create one
            TagToFood newMapping = new TagToFood();
            newMapping.tag = tag;
            HashSet<String> newFoodSet = new HashSet<String>();
            newFoodSet.add(food);
            newMapping.foods = convertToString(newFoodSet);
            try {
                mTagToFood.insert(newMapping).get(1, TimeUnit.SECONDS);
            } catch (Exception f) {
                Log.e(TAG, "Exceptions");
            }
        }*/
    }

    private List<TagToFood> refreshItemsFromMobileServiceTable(String tag) throws ExecutionException, InterruptedException {
        final ArrayList<List<TagToFood>> tagToFoodListArray = new ArrayList<List<TagToFood>>();
        try {
            MobileServiceClient mClient = new MobileServiceClient("https://foodweb.azure-mobile.net/", "KfCuCQzAIBJyEsZXdMGSUkEkxNFZPk98", this).withFilter(new ProgressFilter());
            MobileServiceTable<TagToFood> mTagToFood = mClient.getTable(TagToFood.class);

            mTagToFood.where().field("tag").eq(tag).execute(new TableQueryCallback<TagToFood>() {
                @Override
                public void onCompleted(List<TagToFood> result, int count, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        //Log.d(TAG, result.toString());
                        //tagToFoodList = result;
                        tagToFoodListArray.add(result);
                        TextView textView = (TextView) findViewById(R.id.textview);
                        for (TagToFood map : tagToFoodList) {
                            textView.setText("Ready");
                        }
                    } else {
                        Log.e(TAG, "Timeout error");
                        TextView textView = (TextView) findViewById(R.id.textview);
                        textView.setText("Ready");
                    }
                }
            });
            //return mTagToFood.where().field("tag").eq(tag).execute().get();

            /*for (TagToFood map : tagToFoodList) {
                Log.d(TAG, map.getTag());
                Log.d(TAG, map.getFoods());
            }*/
        } catch (Exception e) {
            Log.e(TAG, "Error");
        }
        //return tagToFoodListArray.get(0);
        TextView textView = (TextView) findViewById(R.id.textview);
        while (!textView.getText().equals("Ready")) {
            Thread.sleep(1000);
        }
        textView.setText("Dummy");
        return tagToFoodListArray.get(0);
        //mClient.getTable(TagToFood.class).where().field("tag").eq(tag).execute
    }

    public String convertToString(HashSet<String> foods) {
        String result = ""; //this MUST be consistent
        for (String food : foods) {
            result = result.concat(food);
            result = result.concat(";");
        }
        if (!result.equals("")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public HashSet<String> convertToHashSet(String foods) {
        HashSet<String> result = new HashSet<String>();
        if (foods != null && !foods.equals("")) {
            String[] foodStrings = foods.split(";");
            for (String food : foodStrings) {
                result.add(food);
            }
        }
        return result;
    }

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }

    /**
     * Initialize local storage
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("text", ColumnDataType.String);
                    tableDefinition.put("complete", ColumnDataType.Boolean);

                    localStore.defineTable("ToDoItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    Log.e(TAG, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    /**
     * Run an ASync task on the corresponding executor
     * @param task
     * @return
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    private AsyncTask<Void, Void, HashSet<String>> runAsyncTask2(AsyncTask<Void, Void, HashSet<String>> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }
}
