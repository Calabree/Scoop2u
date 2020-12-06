package com.example.scoop2u;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText email, password;
    private Button login;
    TextView register;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.button);

        register = (TextView) findViewById(R.id.register);
        TextView forgotPassword = (TextView) findViewById(R.id.passwordReset);

        register.setOnClickListener(this);
        login.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.register:
                startActivity(new Intent(this, registration.class));
                break;
            case R.id.passwordReset:

                break;

            case R.id.button:
                login();
                break;
        }
    }

    boolean emailIsValid(String emailToCheck) {
        String emailValidationRegex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return emailToCheck.matches(emailValidationRegex);

    }

    private void login(){
        String loginEmail = email.getText().toString().trim();
        String loginPassword = password.getText().toString().trim();

        if(loginEmail.isEmpty()){
            email.setError("Email is Required");
            email.requestFocus();
            return;
        }

        if(!emailIsValid(loginEmail)){
            email.setError("Pleae Provide a Valid Email!");
            email.requestFocus();
            return;
        }

        if(loginPassword.isEmpty()){
            password.setError("Please Provide a Password!");
            password.requestFocus();
            return;
        }

        if(loginPassword.length() < 6){
            password.setError("Password Must be at Least 6 Characters!");
            password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
    }
}