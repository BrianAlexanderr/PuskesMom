package com.example.puskesmom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puskesmom.databinding.ActivityLauncherBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LauncherActivity extends AppCompatActivity {

    ActivityLauncherBinding binding;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean isLoggedIn = auth.getCurrentUser() != null;

                        Intent intent = null;

                        if (isLoggedIn) {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                        }else {
                            intent = new Intent(getApplicationContext(), LoginActivity.class);
                        }

                        startActivity(intent);

                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                }, 1500);
    }
}