package com.tabs.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.schan.tabs.R;

/**
 * Created by schan on 8/23/16.
 */
public class EnableLocationActivity extends AppCompatActivity{

    private View mLayout;
    private final static Integer REQUEST_LOCATION = 199;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enable_location);
        mLayout = findViewById(R.id.enable_location_layout);
        Button enableLocationButton = (Button) findViewById(R.id.enable_location_ok_button);
        boolean isLocationPermitted = showLocation();
        if (isLocationPermitted) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            if(enableLocationButton != null) {
                enableLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(EnableLocationActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                1);
                    }
                });
            }
        }
    }

    /**
     * Called when the 'show location' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public boolean showLocation() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
