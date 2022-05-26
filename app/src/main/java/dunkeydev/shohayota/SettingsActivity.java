package dunkeydev.shohayota;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.MenuItemCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText EmergencyPhone, MessagePhone, MessageText, PolicePhone, AmbulancePhone, FirePhone;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.settings_app_toolbar);

        EmergencyPhone = findViewById(R.id.emergency_phone);
        MessagePhone = findViewById(R.id.emergency_message_phone);
        MessageText = findViewById(R.id.emergency_message_text);
        PolicePhone = findViewById(R.id.police_phone);
        AmbulancePhone = findViewById(R.id.ambulance_phone);
        FirePhone = findViewById(R.id.fire_phone);

        toolbar.setTitle("App Settings");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences("SData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        RunSetText();
    }

    private void RunSetText() {
        String emergencyphone = sharedPreferences.getString("emergencyphone","");
        String emergencymessagephone = sharedPreferences.getString("emergencymessagephone","");
        String emergencymessagetext = sharedPreferences.getString("emergencymessagetext","");
        String policephone = sharedPreferences.getString("policephone","");
        String ambulancephone = sharedPreferences.getString("ambulancephone","");
        String firephone = sharedPreferences.getString("firephone","");

        if (!emergencyphone.isEmpty())
            EmergencyPhone.setText(emergencyphone);
        if (!emergencymessagephone.isEmpty())
            MessagePhone.setText(emergencymessagephone);
        if (!emergencymessagetext.isEmpty())
            MessageText.setText(emergencymessagetext);
        if (!policephone.isEmpty())
            PolicePhone.setText(policephone);
        if (!ambulancephone.isEmpty())
            AmbulancePhone.setText(ambulancephone);
        if (!firephone.isEmpty())
            FirePhone.setText(firephone);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.toolbar_save_menu_layout);
        View view = MenuItemCompat.getActionView(menuItem);
        AppCompatButton PostBtn = view.findViewById(R.id.toolbar_save_btn);
        PostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emergencyphone = EmergencyPhone.getText().toString();
                String emergencymessagephone = MessagePhone.getText().toString();
                String emergencymessagetext = MessageText.getText().toString();
                String policephone = PolicePhone.getText().toString();
                String ambulancephone = AmbulancePhone.getText().toString();
                String firephone = FirePhone.getText().toString();

                editor.putString("emergencyphone",emergencyphone);
                editor.putString("emergencymessagephone",emergencymessagephone);
                editor.putString("emergencymessagetext",emergencymessagetext);
                editor.putString("policephone",policephone);
                editor.putString("ambulancephone",ambulancephone);
                editor.putString("firephone",firephone);
                editor.commit();

                RunSetText();

                Toast.makeText(SettingsActivity.this, "Saved Successfully!!", Toast.LENGTH_SHORT).show();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}