package dunkeydev.shohayota;

import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

public class KeyService extends AccessibilityService {

    private final String TAG = "AccessKeyDetector";
    private int i = 0, j = 0;

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        Handler handler = new Handler();
        Handler handler2 = new Handler();

        SharedPreferences sharedPreferences = getSharedPreferences("SData",MODE_PRIVATE);
        boolean mode = sharedPreferences.getBoolean("mode", false);

        if (mode)
        {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
            {
                i++;
                System.out.println(i);
                if(i==4){
                    System.out.println("Run");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RunBgServiceStart();
                            i = 0;
                        }
                    },1000);

                }
            }
            else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
            {
                j++;
                System.out.println(j);
                if(j==4){
                    System.out.println("Run");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RunBgServiceStop();
                            j = 0;
                        }
                    },1000);
                }
            }

            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    i = 0;
                    j = 0;
                }
            },5000);
        }

        return super.onKeyEvent(event);
    }

    private void RunBgServiceStop() {
        Intent stopIntent = new Intent(KeyService.this, BgService.class);
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
    }

    private void RunBgServiceStart() {
        Intent startIntent = new Intent(KeyService.this, BgService.class);
        startIntent.setAction("start");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if (!foregroundServiceRunning())
                startForegroundService(startIntent);
        }
        else
        {
            if (!foregroundServiceRunning())
                startService(startIntent);
        }
    }


    @Override
    protected void onServiceConnected() {
        Log.i(TAG,"Service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }


    @Override
    public void onInterrupt() {

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