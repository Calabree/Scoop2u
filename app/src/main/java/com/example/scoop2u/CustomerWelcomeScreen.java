package com.example.scoop2u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerWelcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_welcome_screen);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod = item -> {
        switch(item.getItemId()){
            case R.id.home:
                System.out.println("Home View");
                break;
            case R.id.map:
                System.out.println("Map View");
                break;
            case R.id.receipt:
                System.out.println("Receipt View");
                break;
            case R.id.account:
                System.out.println("Account View");
                break;
        }
        return true;
    };

}