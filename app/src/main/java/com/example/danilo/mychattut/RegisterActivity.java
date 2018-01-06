package com.example.danilo.mychattut;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView registerUserName, registerUserEmail, registerUserPassword;
    private Button createAccountBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        registerUserName = (TextView) findViewById(R.id.registerName);
        registerUserEmail = (TextView) findViewById(R.id.registerEmail);
        registerUserPassword = (TextView) findViewById(R.id.registerPassword);
        createAccountBtn = (Button) findViewById(R.id.createAccountBtn);
        loadingBar = new ProgressDialog(this);

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userName = registerUserName.getText().toString();
                String email = registerUserEmail.getText().toString();
                String password = registerUserPassword.getText().toString();
                RegisterNewUser(userName, email, password);
            }
        });

    }

    private void RegisterNewUser(final String userName, String email, String password) {
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(RegisterActivity.this, "Please Enter your name ", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Please Enter your email ", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Please Enter your password ", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Creating New Account ");
            loadingBar.setMessage("Please Wait We Are Creating Your Account...");
            loadingBar.show();
            //register the user with firebase
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        String currentUserID = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        //for storing the user name in getVoted App
                        //String currentUserEmail = mAuth.getCurrentUser().getEmail();
                        storeUserDefaultRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
                        storeUserDefaultRef.child("user_name").setValue(userName);
                        //storeUserDefaultRef.child("user_email").setValue(currentUserEmail);
                        storeUserDefaultRef.child("user_status").setValue("Hey There I am using my chat app");
                        storeUserDefaultRef.child("device_token").setValue(deviceToken);
                        storeUserDefaultRef.child("user_image").setValue("default_profile");
                        storeUserDefaultRef.child("user_thumb_image").setValue("default_image").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    //Toast.makeText(RegisterActivity.this, "Login successfully ", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }
}
