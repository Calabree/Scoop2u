package com.example.scoop2u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity implements View.OnClickListener{


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText email, password;
    private Button login;
    TextView register,forgotPassword;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        mAuth = FirebaseAuth.getInstance();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        forgotPassword = (TextView) findViewById(R.id.passwordReset);
        progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        register = (TextView) findViewById(R.id.register);

        register.setOnClickListener(this);
        login.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.register:
                startActivity(new Intent(this, registration.class));
                break;
            case R.id.passwordReset:
                forgotPassword();
                break;

            case R.id.login:
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

        mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String accountType = snapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("accountType").getValue().toString();

                            if(accountType.equals("Customer")){
                                startActivity(new Intent(LoginScreen.this, CustomerWelcomeScreen.class));
                                progressBar.setVisibility(View.GONE);
                                finish();
                            }
                            if(accountType.equals("Driver")){
                                startActivity(new Intent(LoginScreen.this, DriverWelcomeScreen.class));
                                progressBar.setVisibility(View.GONE);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(LoginScreen.this, "Database Load Error! Please try again!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }else{
                    Toast.makeText(LoginScreen.this, "Login failed! Please try again!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void forgotPassword(){

        String loginEmail = email.getText().toString().trim();

        if(loginEmail.isEmpty()){
            email.setError("Email is Required");
            email.requestFocus();
            return;
        }

        if(!emailIsValid(loginEmail)){
            email.setError("Please Provide a Valid Email!");
            email.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(loginEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginScreen.this, "Password Reset Sent to Your Email!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(LoginScreen.this, "Could Not Send Reset Email, Please Try Again", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

}
