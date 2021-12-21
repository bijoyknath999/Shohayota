package dunkeydev.shohayota;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        rootLayout = findViewById(R.id.splashScreen);
    }

        private void initFunctionality() {
            rootLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            }, SPLASH_DURATION);
        }

        @Override
        protected void onResume() {
            super.onResume();
            initFunctionality();
        }
}