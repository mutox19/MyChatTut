package com.example.danilo.mychattut;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;
    private Button saveChangesBtn;
    private EditText statusInput;
    private FirebaseAuth mAuth;
    private DatabaseReference changeStatusRef;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        saveChangesBtn = (Button)findViewById(R.id.saveChangesStatusBtn);
        statusInput = (EditText)findViewById(R.id.statusInput);
        String oldStatus = getIntent().getExtras().get("user_status").toString();
        if(oldStatus != null && !oldStatus.equals(""))
        {
            statusInput.setText(oldStatus);
        }
        loadingBar = new ProgressDialog(this);

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newStatus = statusInput.getText().toString();
                ChangeProfileStatus(newStatus);
            }
        });
    }

    private void ChangeProfileStatus(String newStatus)
    {
        if(TextUtils.isEmpty(newStatus))
        {
            Toast.makeText(StatusActivity.this, "Status cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //change user status
            loadingBar.setTitle("Change Profile Status");
            loadingBar.setMessage("Please Wait While We Are Updating Your Profile Status....");
            loadingBar.show();
            changeStatusRef.child("user_status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(StatusActivity.this, "Profile Status Updated Successfully..", Toast.LENGTH_SHORT).show();
                        Intent settingsIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                        startActivity(settingsIntent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(StatusActivity.this, "Error Occured....", Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });

        }
    }


}
