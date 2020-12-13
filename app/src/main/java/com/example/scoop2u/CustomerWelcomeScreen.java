package com.example.scoop2u;

import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;



public class CustomerWelcomeScreen extends AppCompatActivity {

    gmapsFragmentCustomer fragment;
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_welcome_screen);

        fragment=new gmapsFragmentCustomer();

        fm = getFragmentManager();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
        bottomNavigationView.setSelectedItemId(R.id.account);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod = item -> {
        switch(item.getItemId()){
            case R.id.map: {
                fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment);
                fragmentTransaction.commit();
            }
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