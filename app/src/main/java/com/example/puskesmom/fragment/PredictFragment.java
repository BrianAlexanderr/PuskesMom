package com.example.puskesmom.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puskesmom.R;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.File;


public class PredictFragment extends Fragment {

    // Views
    private LinearLayout layoutResultSection;
    private Button btnPredict;
    private EditText etUmur, etBerat, etTinggi;
    private TextView tvStatusResult;

    // TFLite Interpreter
    private Interpreter tfliteInterpreter;

    // CHANGE THIS to match the exact name in your Firebase Console
    private static final String REMOTE_MODEL_NAME = "Stunt_Prediction";

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_predict, container, false);

        // 1. Initialize Views
        layoutResultSection = view.findViewById(R.id.layout_result_section);
        btnPredict = view.findViewById(R.id.btn_predict);
        etUmur = view.findViewById(R.id.et_umur);
        etTinggi = view.findViewById(R.id.et_tinggi);
        tvStatusResult = view.findViewById(R.id.tv_status_result);

        // 2. Initialize Model (Download or Load Local)
        initializeFirebaseModel();

        // 3. Button Click
        btnPredict.setOnClickListener(v -> {
            if (etUmur.getText().toString().isEmpty() ||
                    etTinggi.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Harap isi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get Inputs
            float age = Float.parseFloat(etUmur.getText().toString());
            float height = Float.parseFloat(etTinggi.getText().toString());

            runInference(age, height, false);
        });

        return view;
    }

    private void initializeFirebaseModel() {
        // Download conditions: Only download if on Wifi (optional)
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        // Download the model
        FirebaseModelDownloader.getInstance()
                .getModel(REMOTE_MODEL_NAME, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(model -> {
                    // Download complete. Initialize the TFLite interpreter
                    File modelFile = model.getFile();
                    if (modelFile != null) {
                        tfliteInterpreter = new Interpreter(modelFile);
                        // Optional: Enable button only when model is ready
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

        // 1. DEFINE CONSTANTS (Replace these with the output from Step 1 above!)
        // Order: Age, Height, Male, Female
        float[] MEANS = {18.91121446f, 80.02609441f,  0.49919859f,  0.50078827f}; // REPLACE ME
        float[] SCALES = {11.14843547f, 14.53023095f,  0.49999936f,  0.49999938f}; // REPLACE ME

        // 2. PREPARE RAW INPUTS
        float valMale = isMale ? 1.0f : 0.0f;
        float valFemale = isMale ? 0.0f : 1.0f;

        // 3. NORMALIZE ( (Value - Mean) / Scale )
        float normAge = (rawAge - MEANS[0]) / SCALES[0];
        float normHeight = (rawHeight - MEANS[1]) / SCALES[1];
        float normMale = (valMale - MEANS[2]) / SCALES[2];
        float normFemale = (valFemale - MEANS[3]) / SCALES[3];

        // 4. CREATE INPUT ARRAY [1, 4]
        float[][] inputs = new float[1][4];
        inputs[0][0] = normAge;
        inputs[0][1] = normHeight;
        inputs[0][2] = normMale;
        inputs[0][3] = normFemale;

        // 5. OUTPUT ARRAY [1, 4] (Since you have 4 classes: 0, 1, 2, 3)
        // The model outputs probabilities for: Normal, Severely Stunted, Stunted, Tinggi
        float[][] outputs = new float[1][4];

        // 6. RUN
        tfliteInterpreter.run(inputs, outputs);

        // 7. INTERPRET RESULT (Find the index with the highest probability)
        int maxIndex = -1;
        float maxProb = -1.0f;

        for (int i = 0; i < 4; i++) {
            if (outputs[0][i] > maxProb) {
                maxProb = outputs[0][i];
                maxIndex = i;
            }
        }

        // Map index to Label (Based on your label encoder in Python)
        // You need to check your python code: le.classes_ to see the order
        // Usually alphabetical: 0=Normal, 1=Severely Stunted, 2=Stunted, 3=Tinggi
        String[] labels = {"Normal", "Severely Stunted", "Stunted", "Tinggi"};
        String resultStatus = labels[maxIndex];

        boolean isSafe = resultStatus.equals("Normal") || resultStatus.equals("Tinggi");
        updateUI(resultStatus, isSafe);
    }

    private void updateUI(String status, boolean isSafe) {
        layoutResultSection.setVisibility(View.VISIBLE);
        tvStatusResult.setText(status);

        if (isSafe) {
            tvStatusResult.setTextColor(Color.parseColor("#1B5E20")); // Green
        } else {
            tvStatusResult.setTextColor(Color.RED); // Red
        }

        layoutResultSection.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close interpreter to avoid memory leaks
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }
}