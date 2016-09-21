package com.tabs.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;

/**
 * Created by schan on 8/23/16.
 */
public class EnableLocation extends AppCompatActivity{

    private Button enableLocationButton;
    private View mLayout;
    private Integer REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enable_location);

        mLayout = findViewById(R.id.enable_location_layout);
        enableLocationButton = (Button) findViewById(R.id.enable_location_ok_button);

        boolean isLocationPermitted = showLocation();

        if(isLocationPermitted) {
            Intent intent = new Intent(this, login.class);
            if (intent != null) {
                startActivity(intent);
            }
        } else {

        }

        enableLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(EnableLocation.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        });
    }

    /**
     * Called when the 'show camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public boolean showLocation() {
        // BEGIN_INCLUDE(camera_permission)
        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            return false;
        } else {
            return true;
        }
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {
        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mLayout, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(EnableLocation.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
        // END_INCLUDE(camera_permission_request)
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
                Intent intent = new Intent(this, login.class);
                if (intent != null) {
                    startActivity(intent);
                }
            } else {
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();
            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
