//package com.sharifian.shaheen.foodwatch;
//
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.util.Log;
//
//import com.couchbase.lite.CouchbaseLiteException;
//import com.couchbase.lite.Database;
//import com.couchbase.lite.Document;
//import com.couchbase.lite.Manager;
//import com.couchbase.lite.android.AndroidContext;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//
//public class CalcActivity extends AppCompatActivity {
//    public static final String TAG = "CalcActivity";
//    public static final String DB_NAME = "Food";
//    protected static Manager manager;
//    private Database database;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_calc);
//
//        //createDB();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_calc, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void createDB() {
//        //Manager manager = null;
//        //Database database = null;
//        try {
//            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
//            database = manager.getDatabase(DB_NAME);
//        } catch (Exception e) {
//            Log.e(TAG, "Error getting database", e);
//            return;
//        }
//        //Follows CRUD: Create, Retrieve, Update, Add
//        // Create the document
//        String documentId = createDocument(database);
//        /* Get and output the contents */
//        outputContents(database, documentId);
//        /* Update the document and add an attachment */
//        updateDoc(database, documentId);
//        // Add an attachment
//        addAttachment(database, documentId);
//        /* Get and output the contents with the attachment */
//        outputContentsWithAttachment(database, documentId);
//    }
//
//    // We make Manager/Database references singletons
//    public Database getDatabaseInstance() throws CouchbaseLiteException {
//        if ((this.database == null) & (this.manager != null)) {
//            this.database = manager.getDatabase(DB_NAME);
//        }
//        return database;
//    }
//    public Manager getManagerInstance() throws IOException {
//        if (manager == null) {
//            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
//        }
//        return manager;
//    }
//
//    private void createTagFood(String tag, FoodSet<String> food) {
//        // Create a new document and add data
//        Document document = database.createDocument();
//        String documentId = document.getId();
//        Map<String, FoodSet<String>> map = new HashMap<String, FoodSet<String>>();
//        map.put(tag, food);
//        try {
//            // Save the properties to the document
//            document.putProperties(map);
//        } catch (CouchbaseLiteException e) {
//            Log.e(TAG, "Error putting", e);
//        }
//        return documentId;
//    }
//
//    private String getFood() {
//
//    }
//
//
//}
