package com.matechco.gcmcallcsr1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class TService extends Service {
    public static final String THE_FILE_PATH = "com.matech.csrcall.THE_FILE_PATH";
    MediaRecorder recorder;
    File audiofile;
    String name, phonenumber;
    String audio_format;
    public String Audio_Type;
    int audioSource;
    Context context;
    private Handler handler;
    Timer timer;
    Boolean offHook = false, ringing = false;
    Toast toast;
    Boolean isOffHook = false;
    private boolean recordstarted = false;

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallBr br_call;




    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // final String terminate =(String)
        // intent.getExtras().get("terminate");//
        // intent.getStringExtra("terminate");
        // Log.d("TAG", "service started");
        //
        // TelephonyManager telephony = (TelephonyManager)
        // getSystemService(Context.TELEPHONY_SERVICE); // TelephonyManager
        // // object
        // CustomPhoneStateListener customPhoneListener = new
        // CustomPhoneStateListener();
        // telephony.listen(customPhoneListener,
        // PhoneStateListener.LISTEN_CALL_STATE);
        // context = getApplicationContext();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

        // if(terminate != null) {
        // stopSelf();
        // }
        return START_NOT_STICKY;
    }

    public class CallBr extends BroadcastReceiver {

        Bundle bundle;
        String state;
        String inCall, outCall;
        public boolean wasRinging = false;
        public String recordedFilePath;
        private SharedPreferences sharedPreferences;
        public static final String MY_PREF = "MY_PREF";
        public static final String REFERENCE_KEY = "REFERENCE_KEY";

        @Override
        public void onReceive(Context context, Intent intent) {

            Integer API_VERSION = Build.VERSION.SDK_INT;
            Log.e("SDK version", String.valueOf(API_VERSION));

            if(API_VERSION >= 23) {

                // this piece of code is supposed to be working for android 5 and above but can't be tested
                // the new version is not available with my physical device
                //
                /*switch (intent.getIntExtra(TelephonyManager.EXTRA_FOREGROUND_CALL_STATE, -2) {
                    case PreciseCallState.PRECISE_CALL_STATE_IDLE:
                        Log.d(This.LOG_TAG, "IDLE");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_DIALING:
                        Log.d(This.LOG_TAG, "DIALING");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_ALERTING:
                        Log.d(This.LOG_TAG, "ALERTING");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_ACTIVE:
                        Log.d(This.LOG_TAG, "ACTIVE");
                        break;
                }*/


            }


                if (intent.getAction().equals(ACTION_IN)) {
                    if ((bundle = intent.getExtras()) != null) {
                        state = bundle.getString(TelephonyManager.EXTRA_STATE);
                        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                            inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                            wasRinging = true;
                            Toast.makeText(context, "IN : " + inCall, Toast.LENGTH_LONG).show();
                        } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                            inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                            if (wasRinging == true) {
                                Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show();

                                //doRecord(inCall);



                            }
                        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                            wasRinging = false;
                            Toast.makeText(context, "REJECT || DISCO", Toast.LENGTH_LONG).show();
                            if (recordstarted) {

                                try {
                                    recorder.stop();
                                    recordstarted = false;

                                    showNotification();
                                    showProgressNotification(recordedFilePath);

                                    Toast.makeText(context, "Call Recorded", Toast.LENGTH_LONG).show();

                                } catch (Exception e) {

                                }

                            }
                            TService.this.stopSelf();
                        }
                    }
                } else if (intent.getAction().equals(ACTION_OUT)) {
                    if ((bundle = intent.getExtras()) != null) {


                        outCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                        Toast.makeText(context, "OUT : " + outCall, Toast.LENGTH_LONG).show();

                        doRecord(outCall);

                    }
                }





        }



        private void showNotification() {
            Log.e("Notif1", "inside first notification");
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(recordedFilePath); // set your audio path
            intent.setDataAndType(Uri.fromFile(file), "audio/*");

            PendingIntent pIntent = PendingIntent.getActivity(TService.this, 0, intent, 0);

            Notification noti = new NotificationCompat.Builder(TService.this)
                    .setContentTitle("Recording Complete")
                    .setContentText("upload in progress")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent).build();

            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, noti);


        }

        private void doRecord(String outCall) {
            Log.e("Dorecord", "inside record block");
            String out = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/TestRecording");
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }
            String file_name = outCall+"-";

            try {
                audiofile = File.createTempFile(file_name, ".amr", sampleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();

            recorder = new MediaRecorder();

            //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recordedFilePath = audiofile.getAbsolutePath();

            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
            recordstarted = true;

        }


    }

    private void showProgressNotification(final String recordedFilePath) {
        Log.e("Notif2", "inside second notification");
        final NotificationManager mNotifyManager;
        final NotificationCompat.Builder mBuilder;
        final int id = 1;
        final String[] reference = new String[1];

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("UPLOAD")
                .setContentText("upload in progress")
                .setSmallIcon(R.drawable.ic_download);

        final FileUpload fileupload = new FileUpload();


        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        // Sets the progress indicator to a max value, the
                        // current completion percentage, and "determinate"
                        // state
                        mBuilder.setProgress(0, 0, true);
                        // Displays the progress bar for the first time.
                        mNotifyManager.notify(id, mBuilder.build());

                        if(fileupload.UploadTheFile(recordedFilePath)){
                            // When the loop is finished, updates the notification

                            String ref = fileupload.reference;
                            mBuilder.setContentText("complete: "+ref).setProgress(0,0,false);

                        }else{
                            // When the loop is finished, updates the notification
                            mBuilder.setContentText("Upload Failed").setProgress(0,0,false);
                        }

                        mNotifyManager.notify(id, mBuilder.build());


                    }
                }
// Starts the thread by calling the run() method in its Runnable
        ).start();




    }
}
