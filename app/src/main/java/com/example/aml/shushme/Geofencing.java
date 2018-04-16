package com.example.aml.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aml on 15/04/18.
 */

public class Geofencing implements ResultCallback {
    // Constants
    public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCING_RADIOUS = 50 ;
    private static final long GEOFENCING_TIMEOUT = 24 * 60 * 60 * 1000 ;

    private List <Geofence > mGeofencingList ;
    private PendingIntent mGeofencePendingIntent ;
    private GoogleApiClient mGoogleApiClient ;
    private Context context ;

    public Geofencing(Context context, GoogleApiClient mGoogleApiClient) {
        this.context = context;
        this.mGoogleApiClient = mGoogleApiClient;
        mGeofencePendingIntent = null ;
        mGeofencingList = new ArrayList<>();
    }
    /***
     * Registers the list of Geofences specified in mGeofenceList with Google Place Services
     * Uses {@code #mGoogleApiClient} to connect to Google Place Services
     * Uses {@link #getGeofencingRequest} to get the list of Geofences to be registered
     * Uses {@link #getGeofencePendingIntent} to get the pending intent to launch the IntentService
     * when the Geofence is triggered
     * Triggers {@link #onResult} when the geofences have been registered successfully
     */




    public void  registerAllGeofences(){
        // Check that the API client is connected and the list has Geofence in it
        if (mGoogleApiClient == null || mGeofencingList.size() == 0
                 ||  mGeofencingList == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest() ,
                    getGeofencePendingIntent()

            ).setResultCallback(this);
        } catch (SecurityException  securityException) {

            // Catch expection generated if the app does not use ACCESS_FINE_LOCATION permission
            Log.e(TAG , securityException.getMessage());
        }

    }

// Unregister all the Geofences created by this app from google place Services
    public  void unRegisterAllGeofences (){
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
             LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient ,
                     getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission
            Log.e(TAG , securityException.getMessage());
        }
    }

    /***
     * Updates the local ArrayList of Geofences using data from the passed in list
     * Uses the Place ID defined by the API as the Geofence object Id
     *
     *  places the PlaceBuffer result of the getPlaceById call
     */
    public void updateGeofencesList (PlaceBuffer placeBuffer) {
        mGeofencingList = new ArrayList<>();
        if (placeBuffer == null || placeBuffer.getCount() ==0) return;

        for (Place place : placeBuffer) {
            // Read a place information from the data base cursor
            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
             double placeLng = place.getLatLng().longitude;

            // Build a Geofence object
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setExpirationDuration(GEOFENCING_TIMEOUT)
                    .setCircularRegion(placeLat , placeLng , GEOFENCING_RADIOUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofencingList.add(geofence);


        }

    }

    /***
     * Creates a GeofencingRequest object using the mGeofenceList ArrayList of Geofences
     * Used by {@code #registerGeofences}
     *
     * @return the GeofencingRequest object
     */
    private GeofencingRequest getGeofencingRequest (){
        GeofencingRequest.Builder  builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofencingList);
        return builder.build() ;
    }

    private PendingIntent getGeofencePendingIntent (){
        // reuse the pending intent if we already have it
        if (mGeofencePendingIntent != null) return mGeofencePendingIntent ;
        Intent  intent = new Intent(context , GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(context , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;

    }
    @Override

    public void onResult(@NonNull Result result) {

        Log.e(TAG , String.format("Error adding/removing geofence : %s" , result.getStatus().toString()));
    }
}
