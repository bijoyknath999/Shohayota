package dunkeydev.shohayota;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Messenger;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {

    public static CardView EmergencyMode, AppSettings;
    private static GpsTracker gpsTracker;
    public static String latitude,longitude;
    private TextView MethodText;
    private RelativeLayout MainLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EmergencyMode = findViewById(R.id.emergency_mode);
        AppSettings = findViewById(R.id.app_settings);
        gpsTracker = new GpsTracker(MainActivity.this);

        MethodText = findViewById(R.id.method_text);

        MainLayout = findViewById(R.id.main_layout);

        sharedPreferences = getSharedPreferences("SData",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        boolean mode = sharedPreferences.getBoolean("mode", false);
        if (mode)
            MethodText.setText("Emergency Mode : On");
        else
            MethodText.setText("Emergency Mode : Off");



        EmergencyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getApplicationContext())
                        .withPermissions(Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.CALL_PHONE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                if (multiplePermissionsReport.areAllPermissionsGranted())
                                {
                                    boolean mode = sharedPreferences.getBoolean("mode", false);
                                    if (mode) {
                                        MethodText.setText("Emergency Mode : Off");
                                        editor.putBoolean("mode", false);
                                        stopall();
                                    }
                                    else {
                                        editor.putBoolean("mode", true);
                                        MethodText.setText("Emergency Mode : On");
                                    }
                                    editor.commit();
                                }
                                else
                                    Toast.makeText(MainActivity.this, "Please allow all permissions.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError dexterError) {
                        Toast.makeText(MainActivity.this, "Error :"+dexterError, Toast.LENGTH_SHORT).show();
                    }
                }).check();
            }
        });

        AppSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        Dexter.withContext(getApplicationContext())
                .withPermissions(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError dexterError) {
                Toast.makeText(MainActivity.this, "Error :"+dexterError, Toast.LENGTH_SHORT).show();
            }
        }).check();

        checkAccessibilityPermission();
    }

    private void stopall() {
        Intent stopIntent = new Intent(MainActivity.this, BgService.class);
        stopIntent.setAction("stop");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if (foregroundServiceRunning())
                startForegroundService(stopIntent);
        }
        else
        {
            if (foregroundServiceRunning())
                startService(stopIntent);
        }

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
        amanager.setStreamVolume(AudioManager.STREAM_SYSTEM,100,0);
*/
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

    public boolean checkAccessibilityPermission() {
        int accessEnabled=0;
        try {
            accessEnabled = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (accessEnabled==0) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            /** request permission via start activity for result */
            startActivity(intent);
            return false;
        } else {
            return true;
        }
    }


    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(BgService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}