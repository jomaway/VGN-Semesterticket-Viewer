package de.jomaway.semesterticket;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 99 ;
    private static final int RESULT_LOAD_IMG = 1;
    private static final String TICKET_IMAGE_PATH = "TicketImagePath";
    String imgDecodableString;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restore Preferences which contain the Image path.
        // Use MODE_PRIVATE for default operation
        SharedPreferences ticketSettings = getPreferences(MODE_PRIVATE);
        imgDecodableString = ticketSettings.getString(TICKET_IMAGE_PATH,null);
        if (imgDecodableString != null) {
            setTicketImage();
        } else {
            showSelectTicketBtn();
        }
    }

    // Add an Menu which shows an Edit button to change the ticket picture
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    // Callback function from a click on the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_selectTicket:
                setTicket();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSelectTicketBtn() {
        Button button = (Button) findViewById(R.id.btn_selectTicket);
        button.setVisibility(View.VISIBLE);
    }

    // gets called on a click to the button
    public void selectTicket(View view) {
        setTicket();
    }

    // function to select a Ticket
    private void setTicket() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG,"permission Granted - load Image from Gallery");
            loadImageFromGallery();
        } else {
            Log.i(TAG,"permission not Granted - ask User for Permission");
            askUserForPermission();
        }

    }
    // Function to ask the user for permission
    private void askUserForPermission() {
        // Should we show an explanation?
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.i(TAG,"needs to show permission dialog");

            //TODO: show dialog box async and then make the request after user interaction

        } else {
            // no explanation needed, we can request the permission
            makePermissionRequest();
        }
    }

    // Function makes the request for the permission
    private void makePermissionRequest() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
    }

    // Funktion to load an Image from the Gallery
    private void loadImageFromGallery() {
        //create Intent to Open Image application like Gallery, Google Photos , etc...
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    // Callback function from the the Permission Dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is canceld the result array is empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG,"Permission was granted");
                    loadImageFromGallery();
                } else {
                    Log.i(TAG,"Permission was NOT granted");
                    Toast.makeText(this, R.string.toast_noPermissionGrantedFromUser, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Callback function from the Gallery App
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        try {
            //When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
                //Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA};

                //Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn,null,null,null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                saveImagePathToPreferences();

                //set the Image to the ImageView
                setTicketImage();

                Button button = (Button) findViewById(R.id.btn_selectTicket);
                button.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, R.string.toast_noImageSelected, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImagePathToPreferences() {
        // Save string to SharedPreferences
        Log.d(TAG, "store imgDecodableStrign");
        Log.d(TAG, imgDecodableString);
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(TICKET_IMAGE_PATH,imgDecodableString);

        // Commit the edits!
        editor.commit();
    }

    private void setTicketImage() {
        ImageView imageView = (ImageView) findViewById(R.id.ticketImage);
        //Set the Image in ImageView after decoding the String
        imageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
    }
}
