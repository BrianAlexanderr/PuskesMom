package com.example.puskesmom;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPw;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPw = findViewById(R.id.etConfirmPw);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateRegister();
            }
        });

        // Text Login → balik ke LoginActivity
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void validateRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPw = etConfirmPw.getText().toString().trim();

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

        if (TextUtils.isEmpty(confirmPw)) {
            etConfirmPw.setError("Confirm password wajib diisi");
            etConfirmPw.requestFocus();
            return;
        }

        if (!password.equals(confirmPw)) {
            etConfirmPw.setError("Password tidak sama");
            etConfirmPw.requestFocus();
            return;
        }

        // Kalo udah Register, user bakal disuruh login
        // Mungkin bisa diubah juga jadi langsung ke Home Page kalo mau
        // Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
