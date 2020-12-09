package com.example.scoop2u;

import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerWelcomeScreen extends AppCompatActivity {

    gmapsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_welcome_screen);

        fragment=new gmapsFragment();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);


    }

    private final BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod = item -> {
        switch(item.getItemId()){
            case R.id.home:
                System.out.println("Home View");
                break;
            case R.id.map: {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment);
                fragmentTransaction.commit();

                System.out.println("Map View");
            }
                break;
            case R.id.receipt:
                System.out.println("Receipt View");
                break;
            case R.id.account: {
                System.out.println("Account View");
                CustomerAccountFragment caf = new CustomerAccountFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.add(R.id.container, caf, null);
                fragmentTransaction.commit();
            }
                break;
        }
        return true;
    };

}