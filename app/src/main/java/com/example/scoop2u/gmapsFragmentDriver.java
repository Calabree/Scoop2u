package com.example.scoop2u;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.Fragment;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.os.SystemClock.sleep;

public class gmapsFragmentDriver extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private MapView map;

    private GoogleMap gmap;

    private static final String MAPVIEW_BUNDLE_KEY = "API_KEY";

    private static final String TAG = "gmapsFragment";

    private FragmentActivity context;

    private Button pingButton, stopPingButton;

    private boolean orderInProgress;

    private int LOCATION_REQUEST_CODE = 4321;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationRequest locationRequest;

    private Marker m, m2;

    private String customerID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gmaps_driver, container, false);

        map = view.findViewById(R.id.map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = LocationRequest.create();

        initGoogleMap(savedInstanceState);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        findCustomer();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.pingButton:

                pingButton.setVisibility(View.GONE);
                stopPingButton.setVisibility(View.VISIBLE);

                break;
            case R.id.stopPingButton:

                stopPingButton.setVisibility(View.GONE);
                pingButton.setVisibility(View.VISIBLE);

                stopLocationUpdates();

                break;
        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        map.onCreate(mapViewBundle);

        map.getMapAsync(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        map.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {

        super.onResume();
        map.onResume();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSetting();
        } else {
            askPermission();
        }
    }

    @Override
    public void onStart() {

        super.onStart();
        map.onStart();
    }

    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, "onSuccess: " + location.toString());
                    Log.d(TAG, "onSuccess: " + location.getLongitude());
                    Log.d(TAG, "onSuccess: " + location.getLatitude());
                    Log.d(TAG, "onSuccess: " + location.toString());

                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(loc);
                    gmap.addMarker(markerOptions);

                    Criteria criteria = new Criteria();

                    if (location != null)
                    {
                        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                        CameraPosition cameraPosition = new CameraPosition.Builder()    // zoom camera into users current location
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(17)
                                .bearing(90)
                                .build();
                        gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }

                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {  // update the users location in the database
                                    System.out.println("ok");
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("latitude").setValue(location.getLatitude());
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("longitude").setValue(location.getLongitude());

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                } else {
                    Log.d(TAG, "onSuccess: location was null");
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
            }
        });

        locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
            }
        });
    }

    private void askPermission() {  // get permission to use location services
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askPermission: show alert dialog");
                new AlertDialog.Builder(getActivity())
                        .setTitle("permission request")
                        .setMessage("give permission")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // Handle the result from permissions request
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {

            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        map.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap map1) {
        gmap = map1;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) // Check if location permission given
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gmap.setMyLocationEnabled(true);
    }

    @Override
    public void onPause() {
        map.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        map.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    public void onAttach(Activity activity) {
        context = (FragmentActivity) activity;
        super.onAttach(activity);
    }


    private void findCustomer() { // find the customer who is currently pinging this driver

        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) { // Loop through drivers
                    String type = snap.child("accountType").getValue().toString();
                    String driverID = snap.child("currentDriverID").getValue().toString();
                    if (type.equals("Customer")) {
                        if (driverID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) { // If this user is a customer and has our driver id set as the driver who is serving them
                            String customerID = snap.getKey();
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentDriverID").setValue(customerID);

                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double t = lon1 - lon2;
        double distance = Math.sin(lat1 * Math.PI / 180.0) * Math.sin(lat2 * Math.PI / 180.0) + Math.cos(lat1 * Math.PI / 180.0) * Math.cos(lat2 * Math.PI / 180.0) * Math.cos(Math.toRadians(t));
        distance = Math.acos(distance);
        distance = (distance * 180.0 / Math.PI);
        distance = distance * 60 * 1.1515;
        return distance;
    }




    //check for location privilege - if set starts location loop
    private void checkSetting() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();

        SettingsClient client = LocationServices.getSettingsClient(context);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(context, 1234);
                    } catch(IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });
    }
    //start location loop
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    //stops location loop
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
    //updates driver location every 2-4 seconds
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            if (locationResult == null) {
                return;
            }
            for(Location location : locationResult.getLocations()) {

                if (m!= null) { // Remove marker
                    m.remove();
                }

                Log.d(TAG, "OnLocationResult " + location.toString());

                double lat = location.getLatitude();
                double lon = location.getLongitude();

                FirebaseDatabase.getInstance().getReference().child("Users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                String ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                FirebaseDatabase.getInstance().getReference("Users").child(ID).child("latitude").setValue(lat);
                                FirebaseDatabase.getInstance().getReference("Users").child(ID).child("longitude").setValue(lon);

                                customerID = snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentDriverID").getValue().toString();

                                if (m2 != null) {
                                    m2.remove();
                                }
                                try { // Try to find the customers location

                                    double lat2 = Double.parseDouble(snapshot.child(customerID).child("latitude").getValue().toString());
                                    double lon2 = Double.parseDouble(snapshot.child(customerID).child("longitude").getValue().toString());

                                    LatLng loc = new LatLng(lat2, lon2);
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(loc);
                                    m2 = gmap.addMarker(markerOptions);

                                    double distance = calculateDistance(lat, lon, lat2, lon2);

                                    if (distance <= 1.0) {

                                        System.out.println("stopped, driver within 5 miles");

                                        FirebaseDatabase.getInstance().getReference("Users").child(customerID).child("currentDriverID").setValue("null");
                                        FirebaseDatabase.getInstance().getReference("Users").child(ID).child("currentDriverID").setValue("null");
                                    }
                                } catch(NullPointerException e) { // If we cant find the customers location who we are serving, go to the next customer.

                                    Log.e(TAG, "LocationCallback: NullPointerException Thrown!");

                                    FirebaseDatabase.getInstance().getReference("Users").child(ID).child("currentDriverID").setValue("null");
                                    findCustomer();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(loc);
                m = gmap.addMarker(markerOptions);
            }
        }
    };
}