package com.example.puskesmom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puskesmom.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndRegister();
            }
        });

        binding.tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void validateAndRegister() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        String name = binding.etName.getText().toString().trim();

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

        // --- VALIDASI NAMA ---
        if (name.isEmpty()){
            binding.tvErrorName.setText("Nama tidak boleh kosong");
            binding.tvErrorName.setVisibility(View.VISIBLE);
            isValid = false;
        } else{
            binding.tvErrorName.setVisibility(View.GONE);
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

        // --- VALIDASI CONFIRM PASSWORD ---
        if (!confirmPassword.equals(password)) {
            binding.tvErrorConfirm.setText("Password tidak sama");
            binding.tvErrorConfirm.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorConfirm.setVisibility(View.GONE);
        }

        // --- JIKA SEMUA VALID ---
        if (isValid) {
            redirectLogin();
        }
    }

    private void redirectLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String name = binding.etName.getText().toString().trim();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        assert auth.getCurrentUser() != null;
                        String userID = auth.getCurrentUser().getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", email);
                        userMap.put("name", name);
                        FieldValue FiledValue;
                        userMap.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("users").document(userID)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    auth.signOut();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                                });
                    }
                });
    }
}