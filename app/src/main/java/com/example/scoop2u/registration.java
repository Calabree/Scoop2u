package com.example.scoop2u;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class registration extends AppCompatActivity {
    private Spinner accounttype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        accounttype = (Spinner) findViewById(R.id.accountType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.account_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accounttype.setAdapter(adapter);
    }
}