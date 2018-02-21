package com.matechco.omnisales;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
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

import static android.telephony.TelephonyManager.*;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;

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
    TService tService = self;

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private static final String LOG_TAG = "CALL_LOG_TAG";
    private CallBr br_call;
    private static  TService self;



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

        self = this;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);

        if (this.br_call!=null) {
            this.unregisterReceiver(this.br_call);
        }
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

        private boolean validateMicAvailability(){
            Boolean available = true;
            AudioRecord recorder =
                    new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_DEFAULT, 44100);
            try{
                if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
                    available = false;

                }

                recorder.startRecording();
                if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
                    recorder.stop();
                    available = false;

                }
                recorder.stop();
            } finally{
                recorder.release();
                recorder = null;
            }

            return available;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Integer API_VERSION = Build.VERSION.SDK_INT;
            Log.e("SDK version", String.valueOf(API_VERSION));

            if (intent.getAction().equalsIgnoreCase("ACTIVITY_FINISH"))
            {
                Log.d("INFO", intent.getExtras().getString("FinishMsg"));
                //finish(); // do here whatever you want
            }

            if(API_VERSION >= 23) {

                // this piece of code is supposed to be working for android 5 and above but can't be tested
                // the new version is not available with my physical device
                //


                switch (intent.getIntExtra(EXTRA_STATE, -2)) {
                    case CALL_STATE_IDLE:
                        Log.d(LOG_TAG, "IDLE");
                        break;
                    case CALL_STATE_RINGING:
                        Log.d(LOG_TAG, "RINGING");
                        break;
                    case CALL_STATE_OFFHOOK:
                        Log.d(LOG_TAG, "OFFHOOK");
                        break;

                }


            }




                if (intent.getAction().equals(ACTION_IN)) {
                    if ((bundle = intent.getExtras()) != null) {
                        state = bundle.getString(EXTRA_STATE);
                        if (state.equals(EXTRA_STATE_RINGING)) {
                            inCall = bundle.getString(EXTRA_INCOMING_NUMBER);
                            wasRinging = true;
                            Toast.makeText(context, "IN : " + inCall, Toast.LENGTH_LONG).show();
                        } else if (state.equals(EXTRA_STATE_OFFHOOK)) {
                            inCall = bundle.getString(EXTRA_INCOMING_NUMBER);

                            if (wasRinging == true) {
                                Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show();

                                //doRecord(inCall);



                            }
                        } else if (state.equals(EXTRA_STATE_IDLE)) {
                            wasRinging = false;
                            Toast.makeText(context, "REJECT || DISCO", Toast.LENGTH_LONG).show();
                            if (recordstarted) {

                                try {
                                    try {
                                        Thread.sleep(150);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    recorder.stop();
                                    recorder.reset();
                                    recorder.release();
                                    recordstarted = false;

                                    showNotification();
                                    showProgressNotification(recordedFilePath);

                                    Toast.makeText(context, "Call Recorded", Toast.LENGTH_LONG).show();


                                } catch (Exception e) {

                                }

                            }
                            self.unregisterReceiver(br_call);
                            self.stopSelf();
//                            TService.this.stopSelf();
                        }
                    }
                } else if (intent.getAction().equals(ACTION_OUT)) {
                    if ((bundle = intent.getExtras()) != null) {

                        validateMicAvailability();


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
            String file_name = "/"+outCall+"-";

            try {
                audiofile = File.createTempFile(file_name, ".mp3", sampleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();

            recorder = new MediaRecorder();

            //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recordedFilePath = audiofile.getAbsolutePath();

            try {
                recorder.prepare();
                recorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

                        if(fileupload.UploadTheFile(recordedFilePath, TService.this)){
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
