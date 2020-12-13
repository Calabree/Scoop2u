package com.example.scoop2u;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DriverWelcomeScreen extends AppCompatActivity {

    gmapsFragmentDriver fragment;
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_welcome_screen);

        fragment=new gmapsFragmentDriver();

        fm = getFragmentManager();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
        bottomNavigationView.setSelectedItemId(R.id.account);

    }

    LocationListener locationListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            System.out.println("Long:"+longitude+",Lat:"+latitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod = item -> {
        switch(item.getItemId()){
            case R.id.map:
                fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment);
                fragmentTransaction.commit();
                break;
            case R.id.account:
                CustomerAccountFragment caf = new CustomerAccountFragment();
                fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.container, caf);
                fragmentTransaction.commit();

                break;
        }
        return true;
    };

}