package com.example.puskesmom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puskesmom.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateLogin();
            }
        });

        binding.tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void validateLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        boolean isValid = true;

        if (email.isEmpty()) {
            binding.tvErrorEmail.setText("Email tidak boleh kosong");
            binding.tvErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!email.contains("@gmail.com")) {
            binding.tvErrorEmail.setText("Email harus menggunakan @gmail.com");
            binding.tvErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorEmail.setVisibility(View.GONE); // Sembunyikan jika benar
        }

        // --- VALIDASI PASSWORD ---
        if (password.isEmpty()) {
            binding.tvErrorPassword.setText("Password tidak boleh kosong");
            binding.tvErrorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (password.length() < 6) {
            binding.tvErrorPassword.setText("Password minimal 6 karakter");
            binding.tvErrorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorPassword.setVisibility(View.GONE);
        }

        // --- JIKA SEMUA VALID ---
        if (isValid) {
            Login();
            Toast.makeText(this, "Data Valid! Mendaftarkan...", Toast.LENGTH_SHORT).show();
        }
    }

    private void Login() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login Berhasil
                        Toast.makeText(LoginActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        // Animasi transisi
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        finish(); // Tutup halaman login
                    } else {
                        // Login Gagal
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login Gagal";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });

    }
}