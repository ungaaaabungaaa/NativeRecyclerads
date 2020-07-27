package com.example.adstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private FirebaseAuth mAuth;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        loginEmailText = findViewById(R.id.reg_email);
        loginPassText = findViewById(R.id.reg_confirm_pass);
        loginBtn = findViewById(R.id.login_btn);
        loginRegBtn = findViewById(R.id.login_reg_btn);
        loginProgress = findViewById(R.id.login_progress);
        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(regIntent);

            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = loginEmailText.getText().toString();
                String loginPass = loginPassText.getText().toString();
                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){
                    loginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                sendToMain();

                            } else {

                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }
    private void sendToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}



