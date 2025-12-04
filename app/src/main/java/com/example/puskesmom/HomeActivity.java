package com.example.puskesmom;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.puskesmom.databinding.ActivityHomeBinding;
import com.example.puskesmom.fragment.HomeFragment;
import com.example.puskesmom.fragment.PredictFragment;
import com.example.puskesmom.fragment.ScheduleFragment;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        binding.bottomNavigation.setItemIconTintList(null);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_alert) {
                showAlertDialog();
                return false;
            }

            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_schedule) {
                selectedFragment = new ScheduleFragment();
            } else if (itemId == R.id.nav_community) {
                selectedFragment = new PredictFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ScheduleFragment();
            }

            // Replace the fragment
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage("Are you sure you want to send an SOS signal?")
                .setPositiveButton("YES", (dialog, which) -> {
                    // Code to send alert goes here
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}