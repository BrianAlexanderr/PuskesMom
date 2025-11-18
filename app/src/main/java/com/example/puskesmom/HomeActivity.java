package com.example.puskesmom;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.puskesmom.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    Task<String> message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        createNotificationChannel();

        binding = ActivityHomeBinding.inflate(getLayoutInflater());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        message = FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()){
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                    }

                    String token = task.getResult();
                    Log.d("FCM Token", token);
                    Toast.makeText(this, "FCM Ready", Toast.LENGTH_SHORT).show();

                });

        String userEmail = getIntent().getStringExtra("email");
        binding.text.setText(userEmail);

    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "general_channel";
            String description = "General Notifications";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;

            android.app.NotificationChannel channel =
                    new android.app.NotificationChannel("general", name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager =
                    getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}