package com.matechco.omnisales;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Toast.makeText(MyGcmListenerService.this, "received from", Toast.LENGTH_SHORT).show();

        String received_data = data.getString("tel");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "GCM_RETURN_Message: " + data);
        Log.d(TAG, "GCM_RETURN_Message: " + received_data);
        sharedPreferences.edit().putString(QuickstartPreferences.RECEIVED_CALL_NUMBER, received_data).apply();

        /*Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);*/

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + received_data));
        startActivity(intent);

        Intent intent2 = new Intent(this, TService.class);
        if(isMyServiceRunning(TService.class)){

            stopService(intent2);
            Log.e("TserviceTag","Stopped");

            startService(intent2);
            Log.e("TserviceTag","Started");
            //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        }else{
            startService(intent2);
            Log.e("TserviceTag","Started");
            //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        }












        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(received_data);
        // [END_EXCLUDE]
    }


    // check if service running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }





    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param received_data GCM message received.
     */
    private void sendNotification(String received_data) {

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("Test")
                .setContentText(received_data);
        notificationManager.notify(1, mBuilder.build());


        /*Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 / Request code /, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 / ID of notification /, notificationBuilder.build());*/
    }


}
