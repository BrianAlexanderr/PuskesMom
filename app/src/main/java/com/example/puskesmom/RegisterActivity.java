package com.example.puskesmom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;import android.widget.Toast;

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
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding.btnRegister.setOnClickListener(view -> validateAndRegister());

        binding.tvLoginLink.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateAndRegister() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        if (email.isEmpty()) {
            binding.tvErrorEmail.setText("Email cannot be empty");
            binding.tvErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvErrorEmail.setText("Please enter a valid email");
            binding.tvErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorEmail.setVisibility(View.GONE);
        }

        if (password.isEmpty()) {
            binding.tvErrorPassword.setText("Password cannot be empty");
            binding.tvErrorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (password.length() < 6) {
            binding.tvErrorPassword.setText("Password must be at least 6 characters");
            binding.tvErrorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorPassword.setVisibility(View.GONE);
        }

        if (!confirmPassword.equals(password)) {
            binding.tvErrorConfirm.setText("Passwords do not match");
            binding.tvErrorConfirm.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorConfirm.setVisibility(View.GONE);
        }

        if (isValid) {
            createUserAccount(email, password);
        }
    }

    private void createUserAccount(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        saveUserToFirestore(email, auth.getCurrentUser().getUid());
                    } else {
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String email, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RegisterActivity", "User data saved to Firestore.");
                    auth.signOut();
                    Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("RegisterActivity", "Error writing document", e);
                    Toast.makeText(this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                });
    }
}
