package com.example.scoop2u;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.app.Fragment;

import android.os.TestLooperManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.LinkedBlockingDeque;

public class CustomerAccountFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference reference;
    private TextView accountEmail, accountUsername, accountType;
    private EditText etCurrPass, etNewPass, etReNewPass;
    private Button changePassword, logout;
    private FirebaseUser user;

    public CustomerAccountFragment() {
        //test comment for download
    }

    public static CustomerAccountFragment newInstance() {
        return new CustomerAccountFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_account, container, false);

        mAuth = FirebaseAuth.getInstance();

        accountEmail = (TextView) view.findViewById(R.id.accountEmail);
        accountUsername = (TextView) view.findViewById(R.id.accountUsername);
        accountType = (TextView) view.findViewById(R.id.accountType);
        logout = (Button) view.findViewById(R.id.Logout);
        changePassword = (Button) view.findViewById(R.id.accountChangePassword);

        changePassword.setOnClickListener(this);
        logout.setOnClickListener(this);
        mAuth.getCurrentUser();
        user = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String email = snapshot.child("Users").child(user.getUid()).child("email").getValue().toString();
                String username = snapshot.child("Users").child(user.getUid()).child("username").getValue().toString();
                String type = snapshot.child("Users").child(user.getUid()).child("accountType").getValue().toString();

                accountEmail.setText(email);
                accountUsername.setText(username);
                accountType.setText(type);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return view;
    }

    public void logout(){

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginScreen.class);
        startActivity(intent);
        getActivity().finish();
    }


    public void changePassword() {

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);

        final EditText p1 = new EditText(getActivity());
        final EditText p2 = new EditText(getActivity());
        final EditText p3 = new EditText(getActivity());

        p1.setWidth(100);
        p2.setWidth(100);
        p3.setWidth(100);

        p1.setHint(R.string.currPass);
        p2.setHint(R.string.newPass);
        p3.setHint(R.string.reNewPass);

        ll.addView(p1);
        ll.addView(p2);
        ll.addView(p3);

        alert.setView(ll);

        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (p2.getText().toString() != p3.getText().toString()) {
                    if (!isEmpty(p1) || !isEmpty(p2) || !isEmpty(p3)) {


                        final String email = user.getEmail();

                        AuthCredential credential = EmailAuthProvider.getCredential(email, p1.getText().toString());

                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            user.updatePassword(p2.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getActivity(), "Password Reset", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getActivity(), "Error Resetting Password", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {

                                            Toast.makeText(getActivity(), "Auth Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {

                        p2.setError("Passwords must match");
                        p3.setError("Passwords must match");
                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.create();
        alert.show();
    }

    @Override
    public void onClick(View v)  {
        switch (v.getId()) {

            case R.id.accountChangePassword:
                changePassword();
                break;
            case R.id.Logout:
                logout();
                break;
        }
    }

    private boolean isEmpty(EditText et) {
        if (et.getText().length() == 0) {

            et.setError("Can not be empty");
        return true;
        } return false;
    }
}