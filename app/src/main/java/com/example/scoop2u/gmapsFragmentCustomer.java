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

public class gmapsFragmentCustomer extends Fragment implements OnMapReadyCallback, View.OnClickListener {

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

    private Marker driverMark;

    private FirebaseAuth mAuth;

    FirebaseUser currentuser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gmaps_customer, container, false);

        map = view.findViewById(R.id.cmap);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = LocationRequest.create();

        initGoogleMap(savedInstanceState);

        pingButton = (Button) view.findViewById(R.id.pingButton);
        stopPingButton = (Button) view.findViewById(R.id.stopPingButton);
        pingButton.setOnClickListener(this);
        stopPingButton.setOnClickListener(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);   // update location every 2 - 4 seconds 
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return view;
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.pingButton:
               pingButton.setVisibility(View.GONE);
               stopPingButton.setVisibility(View.VISIBLE);
               ping();
               activeOrder(orderInProgress);
               break;
           case R.id.stopPingButton:
                stopPings();
               FirebaseDatabase.getInstance().getReference().child("Users")
                       .addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                               FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentDriverID").setValue("null");
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError error) {

                           }
                       });
               break;
       }
    }

    private void stopPings() {
        stopPingButton.setVisibility(View.GONE);
        pingButton.setVisibility(View.VISIBLE);
        stopLocationUpdates();
    }

    private void ping(){    // Gets Users location and finds nearest Driver, driver is assigned a delivery to that driver, and user starts recieveing the drivers location updates
        mAuth = FirebaseAuth.getInstance();

        mAuth.getCurrentUser();

        currentuser = mAuth.getCurrentUser();

        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        System.out.println(currentuser.getUid());
                        String type = snapshot.child(currentuser.getUid()).child("accountType").getValue().toString();
                        System.out.println(type);
                        if (type.equals("Customer")) {
                            double lat = Double.parseDouble(snapshot.child(currentuser.getUid()).child("latitude").getValue().toString());
                            double lon = Double.parseDouble(snapshot.child(currentuser.getUid()).child("longitude").getValue().toString());
                            System.out.println(lat + ", " + lon);
                            findNearestDriverId(lat, lon);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
            getLastLocation();
        } else {
            askPermission();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        map.onStart();
    }

    private void getLastLocation() {    //Find last location of the customer
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {  // Found the initial location
                if (location != null) {
                    Log.d(TAG, "onSuccess: " + location.toString());
                    Log.d(TAG, "onSuccess: " + location.getLongitude()); // Log information
                    Log.d(TAG, "onSuccess: " + location.getLatitude());

                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(loc);
                    gmap.addMarker(markerOptions); // Add a marker to the customers location

                    Criteria criteria = new Criteria();

                    if (location != null) // If a location was found
                    {
                        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                        CameraPosition cameraPosition = new CameraPosition.Builder()    // Zoom to the customers location on map startup
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(17)
                                .bearing(90)
                                .build();
                        gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }

                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {  // Set the users coordinate location in firebase
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

    private void askPermission() {  // Get permission to use location services in a dialog.
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {   // Determine what to do when a result is recieved
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();  // Get the location if permission given
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
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) // Check permissions before trying to enable current location
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


    private void findNearestDriverId(double lat1, double lon1) {    // find the nearest driver when a ping is recieved.

        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) { // Loop through each user int he database and find drivers coordinates

                        orderInProgress = false;

                        double distance = 0;

                        for (DataSnapshot snap : snapshot.getChildren()) { // Loop through users in the firebase db

                            String type = snap.child("accountType").getValue().toString(); // Get account type

                            if (type.equals("Driver")) { // If the user is a driver

                                double lat2 = Double.parseDouble(snap.child("latitude").getValue().toString());
                                double lon2 = Double.parseDouble(snap.child("longitude").getValue().toString());

                                double d = calculateDistance(lat1, lon1, lat2, lon2); // Use calculateDistance function to determines the distance to customer

                                if (d < distance || distance == 0) {

                                    distance = d;

                                    String driverID= snap.getKey();
                                    //String driverEmail = snap.child("email").getValue().toString();

                                    // Save the closest drivers id to the customers account, this marks a delivery to this user.
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentDriverID").setValue(driverID);

                                    orderInProgress = true;

                                    System.out.println("Closest Driver: " + driverID);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) { // Calculates distance from one set of latitude / longitude to another
        double t = lon1 - lon2;
        double distance = Math.sin(lat1 * Math.PI / 180.0) * Math.sin(lat2 * Math.PI / 180.0) + Math.cos(lat1 * Math.PI / 180.0) * Math.cos(lat2 * Math.PI / 180.0) * Math.cos(Math.toRadians(t));
        distance = Math.acos(distance);
        distance = (distance * 180.0 / Math.PI);
        distance = distance * 60 * 1.1515;
        return distance;
    }

    private void activeOrder(boolean orderInProgress) { // When a ping is recieved start the location service

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSetting();
        } else {
            askPermission();
        }
    }



    private void checkSetting() { // Check the location setting and if they are correct start location updates

        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();

        SettingsClient client = LocationServices.getSettingsClient(context);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {  // If permission are given start location updates
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

    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {   // location callback, this runs every time location is updated.

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {  // get the drivers id who is serving this customer
                            String driverID = snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentDriverID").getValue().toString();

                            if (driverID.equals("null")) {  // If it is null stop the ping and location updates, as there is no driver to recieve them
                                stopLocationUpdates();
                                stopPings();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            if (locationResult == null) { // If no location found
                return;
            }

            for(Location location : locationResult.getLocations()) {

                if (driverMark!= null) {
                    driverMark.remove();
                }

                Log.d(TAG, "OnLocationResult " + location.toString());

                FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String currentDriver = snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentDriverID").getValue().toString();

                        if(driverMark!= null){
                            driverMark.remove();
                        }

                        try {

                            double lat2 = Double.parseDouble(snapshot.child(currentDriver).child("latitude").getValue().toString());
                            double lon2 = Double.parseDouble(snapshot.child(currentDriver).child("longitude").getValue().toString());

                            LatLng loc = new LatLng(lat2, lon2);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(loc);
                            driverMark = gmap.addMarker(markerOptions);

                        } catch (NullPointerException e) {
                            System.out.println("NullPointerException thrown!");
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
    };

}
