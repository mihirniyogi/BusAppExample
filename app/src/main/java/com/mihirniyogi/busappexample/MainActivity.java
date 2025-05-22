package com.mihirniyogi.busappexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.appdistribution.FirebaseAppDistribution;
import com.google.firebase.appdistribution.InterruptionLevel;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String LOCATION_PERM_STRING = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String NOTIF_PERM_STRING = Manifest.permission.POST_NOTIFICATIONS;
    private double latitude;
    private double longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationTextView;
    private FirebaseAppDistribution distribution;
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean locationGranted = result.getOrDefault(LOCATION_PERM_STRING, false);
                Boolean notificationGranted = result.getOrDefault(NOTIF_PERM_STRING, false);

                if (locationGranted != null && locationGranted) {
                    Log.d("Permission", "Location granted");
                    getCurrentLocation();
                } else {
                    Log.d("Permission", "Location denied");
                }

                if (notificationGranted != null && notificationGranted) {
                    Log.d("Permission", "Notification granted");
                    distribution.showFeedbackNotification(R.string.additionalFormText, InterruptionLevel.HIGH);
                } else {
                    Log.d("Permission", "Notification denied");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // initialise
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationTextView = findViewById(R.id.locationTextView);
        distribution = FirebaseAppDistribution.getInstance();

        /* -------- permissions -------- */
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        // add location perm
        if (!isPermissionGranted(LOCATION_PERM_STRING)) {
            permissionsToRequest.add(LOCATION_PERM_STRING);
        }

        // add notif perm
        if (BuildConfig.BUILD_TYPE.equals("beta") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPermissionGranted(NOTIF_PERM_STRING)) {
            permissionsToRequest.add(NOTIF_PERM_STRING);
        }

        // request added permissions
        if (!permissionsToRequest.isEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
        /* -------- end of permissions -------- */

        // get location and set text
        if (isPermissionGranted(LOCATION_PERM_STRING)) {
            getCurrentLocation();
        }

        // show feedback notification
        if (BuildConfig.BUILD_TYPE.equals("beta") && isPermissionGranted(NOTIF_PERM_STRING)) {
            distribution.showFeedbackNotification(R.string.additionalFormText, InterruptionLevel.HIGH);
        }
    }

    private boolean isPermissionGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        if (!isPermissionGranted(LOCATION_PERM_STRING)) return;

        locationTextView.setText("Getting location...");

        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();
        fusedLocationClient
                .getCurrentLocation(request, null)
                .addOnSuccessListener(
                        this,
                        location -> {
                            if (location == null) {
                                Log.d("Location", "Location is null");
                                return;
                            }
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d("Location", "Lat: " + latitude + ", Lng: " + longitude);
                            setLocationTextView();
                        })
                .addOnFailureListener(
                        this,
                        e -> Log.e("LocationError", "Error getting current location", e));
    }

    private void setLocationTextView() {
        String toShow = String.format(Locale.US, "Lat: %.6f, Lng: %.6f", latitude, longitude);
        locationTextView.setText(toShow);
    }
}