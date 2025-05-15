package com.mihirniyogi.busappexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.appdistribution.FirebaseAppDistribution;
import com.google.firebase.appdistribution.InterruptionLevel;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude;
    private double longitude;
    private TextView locationTextView;
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Log.d("Permission", "ACCESS_FINE_LOCATION permission granted");
                    getCurrentLocation();
                }
                else Log.d("Permission", "ACCESS_FINE_LOCATION permission denied");
            }
    );

    FirebaseAppDistribution distribution = FirebaseAppDistribution.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // initialise
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationTextView = findViewById(R.id.locationTextView);

        // get location and set text
        getCurrentLocation();

        // show feedback notification
        distribution.showFeedbackNotification(R.string.additionalFormText, InterruptionLevel.HIGH);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (hasLocationPermission()) return;
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

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