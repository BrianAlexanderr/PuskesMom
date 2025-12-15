package com.example.puskesmom.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.puskesmom.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class PredictFragment extends Fragment {

    // Views
    private LinearLayout layoutResultSection;
    private Button btnPredict;
    private EditText etTinggi; // Removed etUmur
    private TextView tvStatusResult;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Logic Variables
    private boolean isChildMale = true;
    private float calculatedAgeInMonths = -1f; // Age is stored here internally

    // TFLite Interpreter
    private Interpreter tfliteInterpreter;

    private static final String REMOTE_MODEL_NAME = "Stunt_Prediction";

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_predict, container, false);

        // 1. Initialize Views (Removed etUmur)
        layoutResultSection = view.findViewById(R.id.layout_result_section);
        btnPredict = view.findViewById(R.id.btn_predict);
        etTinggi = view.findViewById(R.id.et_tinggi);
        tvStatusResult = view.findViewById(R.id.tv_status_result);

        // 2. Initialize Model
        initializeFirebaseModel();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 3. Load Data (Gender AND Age calculation)
        loadSelectedChildData();

        // 4. Button Click
        btnPredict.setOnClickListener(v -> {
            // Validation: Only check Height now
            if (etTinggi.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Harap isi tinggi badan!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if age was successfully calculated from DB
            if (calculatedAgeInMonths == -1f) {
                Toast.makeText(getContext(), "Sedang memuat data umur anak...", Toast.LENGTH_SHORT).show();
                // Try reloading if it failed or is slow
                loadSelectedChildData();
                return;
            }

            // Get Input Height
            float height = Float.parseFloat(etTinggi.getText().toString());

            // Run Inference with CALCULATED Age (Internal variable)
            runInference(calculatedAgeInMonths, height, isChildMale);
        });

        return view;
    }

    private void loadSelectedChildData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String selectedId = prefs.getString("selected_child_id", null);

        if (selectedId != null) {
            fetchChildFromFirestore(uid, selectedId);
        } else {
            db.collection("users").document(uid).collection("children")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            prefs.edit().putString("selected_child_id", doc.getId()).apply();
                            fetchChildFromFirestore(uid, doc.getId());
                        }
                    });
        }
    }

    private void fetchChildFromFirestore(String uid, String childId) {
        db.collection("users").document(uid).collection("children").document(childId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // 1. Get Gender
                        String gender = document.getString("gender");
                        if (gender != null) {
                            if (gender.equalsIgnoreCase("Boy") || gender.equalsIgnoreCase("Laki-laki")) {
                                isChildMale = true;
                            } else {
                                isChildMale = false;
                            }
                        }

                        // 2. Get BirthDate and Calculate Age Internally
                        String birthDateStr = document.getString("birthDate"); // Format: "dd-MM-yyyy"
                        calculateAgeFromDate(birthDateStr);

                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load child data", Toast.LENGTH_SHORT).show());
    }

    private void calculateAgeFromDate(String birthDateStr) {
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                // Parse date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate birthDate = LocalDate.parse(birthDateStr, formatter);
                LocalDate currentDate = LocalDate.now();

                // Calculate Period
                Period period = Period.between(birthDate, currentDate);

                // Convert to Total Months (Years * 12 + Months)
                int totalMonths = (period.getYears() * 12) + period.getMonths();

                // Store in logic variable
                calculatedAgeInMonths = (float) totalMonths;


            } catch (Exception e) {
                calculatedAgeInMonths = -1f;
            }
        } else {
            calculatedAgeInMonths = -1f;
        }
    }

    private void initializeFirebaseModel() {
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .build();

        FirebaseModelDownloader.getInstance()
                .getModel(REMOTE_MODEL_NAME, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(model -> {
                    File modelFile = model.getFile();
                    if (modelFile != null) {
                        tfliteInterpreter = new Interpreter(modelFile);
                        btnPredict.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal download model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void runInference(float rawAge, float rawHeight, boolean isMale) {
        if (tfliteInterpreter == null) {
            Toast.makeText(getContext(), "Model not ready.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- CONSTANTS ---
        float[] MEANS = {18.91121446f, 80.02609441f,  0.49919859f,  0.50078827f};
        float[] SCALES = {11.14843547f, 14.53023095f,  0.49999936f,  0.49999938f};

        float valMale = isMale ? 1.0f : 0.0f;
        float valFemale = isMale ? 0.0f : 1.0f;

        float normAge = (rawAge - MEANS[0]) / SCALES[0];
        float normHeight = (rawHeight - MEANS[1]) / SCALES[1];
        float normMale = (valMale - MEANS[2]) / SCALES[2];
        float normFemale = (valFemale - MEANS[3]) / SCALES[3];

        float[][] inputs = new float[1][4];
        inputs[0][0] = normAge;
        inputs[0][1] = normHeight;
        inputs[0][2] = normMale;
        inputs[0][3] = normFemale;

        float[][] outputs = new float[1][4];

        tfliteInterpreter.run(inputs, outputs);

        int maxIndex = -1;
        float maxProb = -1.0f;

        for (int i = 0; i < 4; i++) {
            if (outputs[0][i] > maxProb) {
                maxProb = outputs[0][i];
                maxIndex = i;
            }
        }

        String[] labels = {"Normal", "Severely Stunted", "Stunted", "Tinggi"};
        String resultStatus = labels[maxIndex];

        boolean isSafe = resultStatus.equals("Normal") || resultStatus.equals("Tinggi");
        updateUI(resultStatus, isSafe);
    }

    private void updateUI(String status, boolean isSafe) {
        layoutResultSection.setVisibility(View.VISIBLE);
        tvStatusResult.setText(status);

        if (isSafe) {
            tvStatusResult.setTextColor(Color.parseColor("#1B5E20"));
        } else {
            tvStatusResult.setTextColor(Color.RED);
        }

        layoutResultSection.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }
}