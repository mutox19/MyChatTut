package com.example.danilo.mychattut;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    private Button needNewAccountBtn, alreadyHaveAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        needNewAccountBtn = (Button) findViewById(R.id.needAccountBtn);
        alreadyHaveAccountBtn = (Button) findViewById(R.id.alreadyHaveAccountBtn);


        alreadyHaveAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginActivity = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(loginActivity);
            }
        });

        needNewAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerActivity = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(registerActivity);
            }
        });
    }
}
