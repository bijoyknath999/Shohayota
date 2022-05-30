package dunkeydev.shohayota;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class BgService extends Service implements RecognitionListener {

    public static final String CHANNEL_ID = "ShohayotaForegroundServiceChannel";
    private SharedPreferences sharedPreferences;
    private static GpsTracker gpsTracker;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "ShohayotaVoiceRecognitionActivity";
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private boolean listening = false;
    private FusedLocationProviderClient mFusedLocationClient;
    public static String latitude,longitude;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null)
        {
            if (intent.getAction().equals("start")) {
                gpsTracker = new GpsTracker(getApplicationContext());
                createNotificationChannel();
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Shohayota")
                        .setContentText("Voice Recognition Security is running")
                        .setSmallIcon(R.drawable.ic_notifications)
                        .build();
                startForeground(1, notification);
                Log.d("Bg","Bging");


                // start speech recogniser
                resetSpeechRecognizer();

                /*AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    amanager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);

                } else {
                    amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                    amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
                    amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    amanager.setStreamMute(AudioManager.STREAM_RING, true);
                    amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                }*/

                setRecogniserIntent();
                speech.startListening(recognizerIntent);
                listening = true;
            }
            else if (intent.getAction().equals("stop")) {
                Log.d("Bg","Bging");
                resetSpeechRecognizer();
                speech.stopListening();

                /*AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    final int originalVolume = amanager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    amanager.setStreamVolume(AudioManager.STREAM_ALARM,originalVolume,0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
                    amanager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);

                } else {
                    amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                    amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
                    amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    amanager.setStreamMute(AudioManager.STREAM_RING, false);
                    amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                }

                amanager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,100,0);
                amanager.setStreamVolume(AudioManager.STREAM_ALARM,100,0);
                amanager.setStreamVolume(AudioManager.STREAM_MUSIC,100,0);
                amanager.setStreamVolume(AudioManager.STREAM_RING,100,0);
                amanager.setStreamVolume(AudioManager.STREAM_SYSTEM,100,0);*/

                listening = false;
                stopall();
            }
        }

        return START_STICKY;
    }

    private void resetSpeechRecognizer() {

        if(speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if(SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
    }

    private void setRecogniserIntent() {

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    public  void stopall()
    {
        stopForeground(true);
        stopSelf();
    }

    public static String getLat(){
        if(gpsTracker.canGetLocation()){

            latitude = String.valueOf(gpsTracker.getLatitude());

        }else{
            gpsTracker.showSettingsAlert();
        }
        return latitude;
    }

    public static String getLon(){
        if(gpsTracker.canGetLocation()){

            longitude = String.valueOf(gpsTracker.getLongitude());

        }else{
            gpsTracker.showSettingsAlert();
        }
        return longitude;
    }


    @Override
    public void onDestroy() {
        speech.destroy();
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Woman Sea Diver Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        speech.stopListening();
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";

        System.out.println(text);

        sharedPreferences = getSharedPreferences("SData",MODE_PRIVATE);

        String emergencyphone = sharedPreferences.getString("emergencyphone","");
        String emergencymessagephone = sharedPreferences.getString("emergencymessagephone","");
        String emergencymessagetext = sharedPreferences.getString("emergencymessagetext","");
        String policephone = sharedPreferences.getString("policephone","");
        String ambulancephone = sharedPreferences.getString("ambulancephone","");
        String firephone = sharedPreferences.getString("firephone","");
        String finalmessage = "";
        if (!emergencymessagetext.isEmpty())
            finalmessage = emergencymessagetext.replace("$location","https://maps.google.com/?q="+getLat()+","+getLon());


        if(matches.get(0).contains("help"))
        {
            if (!emergencymessagephone.isEmpty() && !finalmessage.isEmpty())
            {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(emergencymessagephone,null,finalmessage,null,null);
            }
            else
                System.out.println("Empty phone or text message!!");
        }
        if(matches.get(0).contains("call"))
        {
            if (!emergencyphone.isEmpty())
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setData(Uri.parse("tel:"+emergencyphone));
                startActivity(callIntent);
            }
            else
                System.out.println("Empty phone!!");
        }
        if(matches.get(0).contains("police"))
        {
            if (!policephone.isEmpty())
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setData(Uri.parse("tel:"+policephone));//change the number
                startActivity(callIntent);
            }
            else
                System.out.println("Empty phone!!");
        }
        if(matches.get(0).contains("ambulance"))
        {
            if (!ambulancephone.isEmpty())
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setData(Uri.parse("tel:"+ambulancephone));//change the number
                startActivity(callIntent);
            }
            else
                System.out.println("Empty phone!!");
        }
        if(matches.get(0).contains("fire"))
        {
            if (!firephone.isEmpty())
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setData(Uri.parse("tel:"+firephone));//change the number
                startActivity(callIntent);
            }
            else
                System.out.println("Empty phone!!");
        }
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.i(LOG_TAG, "FAILED " + errorMessage);
        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}