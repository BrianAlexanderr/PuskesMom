package com.example.puskesmom;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgot;

    // Firebase Declarations
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup Loading Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Sedang memverifikasi akun...");
        progressDialog.setCancelable(false);

        // Init Views
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

        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add Forgot Password Logic later
                Toast.makeText(LoginActivity.this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show();
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

        // Validation Passed -> Proceed to Firebase Login
        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Login Success
                            Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Destroy LoginActivity so back button doesn't return here
                        } else {
                            // Login Failed
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Login Gagal: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
