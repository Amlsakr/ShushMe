package com.example.aml.shushme;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.aml.shushme.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity   implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {



    //Constants
    public static final  String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUST_FINE_LOCATION = 111 ;
    private static final int PLACE_PICKER_REQUEST = 1 ;
    // Member Varuable
    private boolean mIsEnabled  ;
    private Geofencing mGeofencing ;
    private PlaceListAdapter placeListAdapter ;
    private RecyclerView RV_locations ;
    private CheckBox   ringerPermissions ;
    private GoogleApiClient mClient ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RV_locations = (RecyclerView) findViewById(R.id.RV_locations);


        placeListAdapter = new PlaceListAdapter(this , null);
        RV_locations.setLayoutManager(new LinearLayoutManager(this));
        RV_locations.setAdapter(placeListAdapter);

        // Initialize the switch state anf=d handle the enable/disable switch state
        Switch enable_switch = (Switch) findViewById(R.id.enable_switch);
        mIsEnabled = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.setting_enabled) , false);
        enable_switch.setChecked(mIsEnabled);
        enable_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.settings ) , isChecked) ;
                mIsEnabled = isChecked ;
                if (isChecked)
                    mGeofencing.registerAllGeofences();
                else
                    mGeofencing.unRegisterAllGeofences();
            }
        });

        // Build up LocationServices API client
        // Uses the addApi method to  request the LocationServices API
        // Also uses enableAutoManage to automatically when to connect/suspend the client

         mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
         mGeofencing = new Geofencing(this , mClient);
        }

    /***
     * Called when the Google API Client is successfully connected
     *
     *  connectionHint Bundle of data provided to clients by Google Play services
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshPlacesData();
        Log.i(TAG, "API Client Connection Successful!");
    }

    /***
     * Called when the Google API Client is suspended
     *
     *  cause cause The reason for the disconnection. Defined by constants CAUSE_*.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG , "API Client Connection Suspended !");
    }
    /***
     * Called when the Google API Client failed to connect to Google Play Services
     *
     *  result A ConnectionResult that can be used for resolving the error
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.i(TAG , "API Client connection Failed !");

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialize Location permission checkbox
        CheckBox locationPermissions = (CheckBox) findViewById(R.id.location_permission_checkbox);
        if(ActivityCompat.checkSelfPermission(MainActivity.this , Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.setChecked(false);
        } else {
            locationPermissions.setChecked(true);
            locationPermissions.setEnabled(false);
        }

        //Initialize ringer checkbox
         ringerPermissions = (CheckBox) findViewById(R.id.ringer_permissions_checkbox);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Check if the API supports such permission change and check if permission is granted
        if (Build.VERSION.SDK_INT >= 24 && !nm.isNotificationPolicyAccessGranted()) {
            ringerPermissions.setChecked(false);

        } else {
            ringerPermissions.setChecked(true);
            ringerPermissions.setEnabled(false);
        }




    }

// Button Click event handler to handle clicking the "Add new location" Button
    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this ,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;

        }

            try {
                // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
                // when a place is selected or with the user cancels.
                PlacePicker.IntentBuilder  builder = new PlacePicker.IntentBuilder();
             Intent   i = builder.build(this);
                startActivityForResult(i , PLACE_PICKER_REQUEST);

            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
                Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
                Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
            } catch (Exception e ) {
                Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));

            }

        }


public void refreshPlacesData(){
    Uri uri = PlaceContract.PlaceEntry.CONTENT_URI ;
    Cursor data =  getContentResolver().query(uri , null , null , null , null);

    if (data == null || data.getCount() == 0)  return;

    List<String> guids = new ArrayList<String>();
    while (data.moveToNext()) {
        guids.add(data.getString(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));
    }



    PendingResult <PlaceBuffer > placResult = Places.GeoDataApi.getPlaceById(mClient ,
            guids.toArray(new String[guids.size()]));

placResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
              @Override
              public void onResult(@NonNull PlaceBuffer places) {
                       placeListAdapter.swapPlaces(places);
                  mGeofencing.updateGeofencesList(places);
                  if (mIsEnabled) mGeofencing.registerAllGeofences();

                           }
           });
}


    public void onRingerPermissionsClicked(View view) {

        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);


    }

    public void onLocationPermissionClicked(View view) {

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION} ,
                PERMISSION_REQUST_FINE_LOCATION );


    }

    /***
     * Called when the Place Picker Activity returns back with a selected place (or after canceling)
     *
     * @param requestCode The request code passed when calling startActivityForResult
     * @param resultCode  The result code specified by the second activity
     * @param data        The Intent that carries the result data.
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this , data);
            if (place == null) {
                Log.i(TAG , "No Places are selected");
                return;
            }
            // Extract the place information from the API
            String placeID = place.getId();
            String placeName = place.getName().toString() ;
            String placeAddress = place.getAddress().toString();

            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID , placeID );
            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI , contentValues);

            refreshPlacesData();
        }
    }
}
