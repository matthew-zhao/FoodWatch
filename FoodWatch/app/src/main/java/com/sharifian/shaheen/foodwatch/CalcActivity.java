package com.sharifian.shaheen.foodwatch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CalcActivity extends AppCompatActivity {
    public static final String TAG = "CalcActivity";
    public static final String DB_NAME = "Food";
    protected static Manager manager;
    private Database database;
    //This is bad practice, but fkin yolo, not enuf time
    private String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        createDB();
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

    public String createTagFood(String tag, HashSet<String> food) {
        // Create a new document and add date
        Document document = database.createDocument();
        docId = document.getId();
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
        return docId;
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
