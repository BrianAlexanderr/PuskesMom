package com.example.puskesmom;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Lupa Password → sengaja kosong (belum dipakai)
        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: nanti
            }
        });
    }

    private void validateLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Email Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email wajib diisi");
            etEmail.requestFocus();
            return;
        }

        if (email.length() < 4) {
            etEmail.setError("Email minimal 4 karakter");
            etEmail.requestFocus();
            return;
        }

        // Password Validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password wajib diisi");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Password minimal 8 karakter");
            etPassword.requestFocus();
            return;
        }

        // Kalo udah bener pindah ke Home Page
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
