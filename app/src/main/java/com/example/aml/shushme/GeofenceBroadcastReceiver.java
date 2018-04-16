package com.example.aml.shushme;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by aml on 15/04/18.
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName() ;
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the Geofene eventb from the intent sent through
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG , String.format("Error code : %d" , geofencingEvent.getErrorCode()));

return;
        }
        // get the transition type
        int geofenceTransition =  geofencingEvent.getGeofenceTransition();
        // Check with transition type has triggered this event
        if (geofenceTransition  == Geofence.GEOFENCE_TRANSITION_ENTER) {
            setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            setRingerMode(context , AudioManager.RINGER_MODE_NORMAL);
        } else  {
            Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition));

            return;
        }
        // send the notification
sendNotification(context , geofenceTransition);


    }






    private void  sendNotification (Context context , int transitionType){
        // Create an explicit content  intent  that stars the main activity
        Intent notificationIntent = new Intent(context , MainActivity.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        //Add the main activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);

        // Push the Content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);


        // Get a pending intent containg the entire back stack
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0 , PendingIntent.FLAG_UPDATE_CURRENT);

        //Get a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        //Check the transition type to display relevant icon image

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp).
                    setLargeIcon(BitmapFactory.decodeResource(context.getResources() , R.drawable.ic_volume_off_white_24dp))
            .setContentTitle(context.getString(R.string.silent_mode_activated));

        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources() , R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(context.getString(R.string.back_to_normal));
        }

        // continue building the notification
        builder.setContentText(context.getString(R.string.touch_to_relaunch));
        builder.setContentIntent(notificationPendingIntent);

        //Dismiss the notification once the user touch it
        builder.setAutoCancel(true);

        //Get an instance of notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Issue the notification
        notificationManager. notify(0 , builder.build());


    }

    /**
     * Changes the ringer mode on the device to either silent or back to normal
     *
     *  context The context to access AUDIO_SERVICE
     *  mode    The desired mode to switch device to, can be AudioManager.RINGER_MODE_SILENT or
     *                AudioManager.RINGER_MODE_NORMAL
     */
    private void  setRingerMode (Context context , int mode) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT < 24 ||
                (Build.VERSION.SDK_INT >= 24 && !notificationManager.isNotificationPolicyAccessGranted())) {
//            ActivityCompat.requestPermissions(MainActivity.class.,
//                    new String[]{ Manifest.permission.READ_CONTACTS },
//                    111);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
    }
}
