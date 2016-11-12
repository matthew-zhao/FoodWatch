package com.sharifian.shaheen.foodwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CaptureActivity extends Activity {
    private static final String TAG = "TAGGING  ACTIVITY";
    private static final int CONTENT_REQUEST = 1;

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;
    private File mImageFile = null;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private List<TagToFood> tagToFoodList;
    private List<ToDoItem> toDoItemList;

    /*
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        //        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            mCurrentPhotoPath = pictureFile.getPath();
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_capture2);
    }
    public void capturePhoto(View v) {
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mediaFile;
        //mediaFile = dir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg";
        mediaFile = mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg";
        setCurrentPhotoPath(mediaFile);

        File output=new File(mediaFile);

        try {
            if(output.exists() == false) {
                output.getParentFile().mkdirs();
                output.createNewFile();
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not create file.", e);
        }
        Log.i(TAG, mediaFile);

        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));

        startActivityForResult(i, CONTENT_REQUEST);
    }

    public void getMilk(View v) {
        //for (int i = 0; i < 2; i++) {
            try {
                MobileServiceClient mClient = new MobileServiceClient("https://foodweb.azure-mobile.net/", "KfCuCQzAIBJyEsZXdMGSUkEkxNFZPk98", this).withFilter(new ProgressFilter());
                Log.d(TAG, "We have created mobile service client");
                MobileServiceTable<TagToFood> mTagToFood = mClient.getTable(TagToFood.class);
                try {

                    mTagToFood.where().field("tag").eq("dairy").execute(new TableQueryCallback<TagToFood>() {
                        @Override
                        public void onCompleted(List<TagToFood> result, int count, Exception exception, ServiceFilterResponse response) {
                            if (exception == null) {
                                tagToFoodList = result;
                                TextView textView = (TextView) findViewById(R.id.textview);
                                for (TagToFood map : tagToFoodList) {
                                    textView.setText(map.getTag() + ":" + map.getFoods());
                                }
                            } else {
                                Log.e(TAG, "Timeout error");
                            }
                        }
                    });
                    //tagToFoodList = mTagToFood.where().field("tag").eq("dairy").execute().get();
                    //toDoItemList = mTagToFood.where().field("complete").eq(false).execute().get();

                    /*ToDoItem mapping = null;
                    for (ToDoItem map : toDoItemList) {
                        Log.d(TAG, map.getText());
                    }*/

                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e);
            }
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        displayConfirmation();
    }

    public void displayConfirmation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CaptureActivity.this);
        alertDialog.setMessage("Do you wish to use this picture?").setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Action for 'Yes' Button
                Intent intent = new Intent(getBaseContext(), CalcActivity.class);
                intent.putExtra("CurrentPhotoPath", mCurrentPhotoPath);
                //intent.putExtra("ImageFile", mImageFile);

                startActivity(intent);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        //alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
        //    @Override
        //    public void onClick(DialogInterface dialog, int which) {
        //        dialog.dismiss();
        //    }
        //});
        alertDialog.show();
        //AlertDialog alert = alertDialog.create();
        //alertDialog.setTitle("Are you sure?");
        //alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tagging, menu);
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

    @Override
    protected void onPause() {
        super.onPause();
        //releaseCamera();              // release the camera immediately on pause event
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    public Uri getCapturedImageURI() {
        return mCapturedImageURI;
    }

    public void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
    }
    public File getCapturedFile() {
        return mImageFile;
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
}
