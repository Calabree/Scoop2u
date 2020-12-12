package com.example.scoop2u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class registration extends AppCompatActivity implements View.OnClickListener {

    private Spinner accounttype;
    private FirebaseAuth mAuth;
    private EditText userName, email, password, passwordConf;
    private Button registerButton;
    private ProgressBar progressBar;
    //private PlacesClient placesClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //spinner
        accounttype = (Spinner) findViewById(R.id.accountType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.account_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accounttype.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();

        userName = (EditText) findViewById(R.id.usernameRegister);
        email = (EditText) findViewById(R.id.emailRegister);
        password = (EditText) findViewById(R.id.passwordRegister);
        passwordConf = (EditText) findViewById(R.id.passwordRegisterConfirm);
        progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        registerButton = (Button) findViewById(R.id.button);
        registerButton.setOnClickListener(this);

    }

    boolean emailIsValid(String emailToCheck) {
        String emailValidationRegex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return emailToCheck.matches(emailValidationRegex);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String rEmail = email.getText().toString().trim();
        String rPassword = password.getText().toString().trim();
        String rPasswordConf = passwordConf.getText().toString().trim();
        String rUsername = userName.getText().toString().trim();
        String rAccountType = accounttype.getSelectedItem().toString();
        String rAccountTypeIndex = getResources().getStringArray(R.array.account_type)[accounttype.getSelectedItemPosition()];

        String currentDriverID ="nothing";
        double longitude= 200;
        double latitude = 200;

        if(rUsername.isEmpty()){
            userName.setError("Username is Required");
            userName.requestFocus();
            return;
        }

        if(rAccountTypeIndex.equals("Please Select One")){
            ((TextView)accounttype.getChildAt(0)).setError("Message");
            return;
        }

        if(rEmail.isEmpty()){
            email.setError("Email is Required!");
            email.requestFocus();
            return;
        }

        if (!emailIsValid(rEmail)){
            email.setError("Please Enter a Valid Email!");
            email.requestFocus();
            return;
        }

        if(rPassword.isEmpty()){
            password.setError("Password is Required!");
            password.requestFocus();
            return;
        }
        if (rPassword.length() < 6){
            password.setError("Password Must be at Least 6 Characters!");
            password.requestFocus();
            return;
        }
        if(!rPassword.equals(rPasswordConf)){
            passwordConf.setError("Passwords Do Not Match!");
            passwordConf.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(rEmail,rPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    User user = new User(rUsername, rAccountType, rEmail, currentDriverID, longitude, latitude);
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(registration.this, "User Successfully Registered!", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(registration.this, LoginScreen.class));
                                finish();
                            } else{
                                Toast.makeText(registration.this, "Failed to Register, please try again", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }  else{
                    Toast.makeText(registration.this, "Failed to Register, please try again", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }

            }
        });
    }

}