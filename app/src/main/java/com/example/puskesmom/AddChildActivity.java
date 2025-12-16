package com.example.puskesmom;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddChildActivity extends AppCompatActivity {

    // UI Components
    private EditText etName, etBirthDate;
    private RadioGroup rgGender;
    private Spinner spinnerBloodType;
    private Button btnSave;
    private ImageView btnBack;

    // Logic Variables
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String selectedDateStr = ""; // To store the date for database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        // Hide Action Bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        etName = findViewById(R.id.et_child_name);
        etBirthDate = findViewById(R.id.et_birth_date);
        rgGender = findViewById(R.id.rg_gender);
        spinnerBloodType = findViewById(R.id.spinner_blood_type);
        btnSave = findViewById(R.id.btn_save_child);
        btnBack = findViewById(R.id.btn_back);

        // Setup Blood Type Spinner
        setupSpinner();

        // Setup Date Picker
        etBirthDate.setOnClickListener(v -> showDatePicker());

        // Setup Save Button
        btnSave.setOnClickListener(v -> saveChildData());

        // Setup Back Button
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        // Create an array of blood types
        String[] bloodTypes = {"Select Blood Type", "A", "B", "AB", "O"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bloodTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(adapter);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Formatting the date: DD-MM-YYYY
                    // Note: Month is 0-indexed, so we add 1
                    String dayStr = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                    String monthStr = ((monthOfYear + 1) < 10) ? "0" + (monthOfYear + 1) : String.valueOf(monthOfYear + 1);

                    selectedDateStr = dayStr + "-" + monthStr + "-" + year1;
                    etBirthDate.setText(selectedDateStr);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveChildData() {
        String name = etName.getText().toString().trim();
        String bloodType = spinnerBloodType.getSelectedItem().toString();

        // Get Selected Gender
        String gender = "";
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton rb = findViewById(selectedId);
            gender = rb.getText().toString();
        }

        // --- VALIDATION ---
        if (name.isEmpty()) {
            Log.d("ERROR", "Name is empty");
            return;
        }
        if (gender.isEmpty()) {
            Log.d("ERROR", "gender is empty");
            return;
        }
        if (selectedDateStr.isEmpty()) {
            Log.d("ERROR", "Date is empty");
            return;
        }
        if (bloodType.equals("Select Blood Type")) {
            Log.d("ERROR", "BloodType is empty");
            return;
        }

        // --- SAVE TO FIRESTORE ---
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("name", name);
        childMap.put("gender", gender);
        childMap.put("birthDate", selectedDateStr);
        childMap.put("bloodType", bloodType);


        db.collection("users").document(userId).collection("children")
                .add(childMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddChildActivity.this, "Child Profile Added!", Toast.LENGTH_SHORT).show();
                    String newChildId = documentReference.getId();
                    createImmunizationSchedule(userId, newChildId, selectedDateStr);
                    finish(); // Go back to Home
                })
                .addOnFailureListener(e -> {
                    Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                });
    }

    private void createImmunizationSchedule(String userId, String newChildId, String selectedDateStr) {
        try {
            // 1. Calculate Dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate birthDate = LocalDate.parse(selectedDateStr, formatter);

            LocalDate dateA = birthDate.plusMonths(1); // Next 1 Month
            LocalDate dateB = birthDate.plusMonths(2); // Next 2 Months

            String dateStringA = dateA.format(formatter);
            String dateStringB = dateB.format(formatter);

            // 2. Prepare Data Maps
            Map<String, Object> immunA = new HashMap<>();
            immunA.put("name", "Immunization A");
            immunA.put("dueDate", dateStringA);
            immunA.put("status", "Upcoming");

            Map<String, Object> immunB = new HashMap<>();
            immunB.put("name", "Immunization B");
            immunB.put("dueDate", dateStringB);
            immunB.put("status", "Upcoming");

            // 3. Use WriteBatch for efficiency (Writes multiple documents at once)
            WriteBatch batch = db.batch();

            // Reference to the new subcollection
            // Path: users/{uid}/children/{childId}/immunization/{randomId}
            batch.set(
                    db.collection("users").document(userId)
                            .collection("children").document(newChildId)
                            .collection("immunization").document(), // .document() generates random ID
                    immunA
            );

            batch.set(
                    db.collection("users").document(userId)
                            .collection("children").document(newChildId)
                            .collection("immunization").document(),
                    immunB
            );

            // 4. Commit the Batch
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("SUCCESS", "Immunization schedules created successfully");
                        finish(); // Close activity only after everything is saved
                    })
                    .addOnFailureListener(e -> {
                        Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
        }
    }
}