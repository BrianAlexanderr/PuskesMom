package com.example.puskesmom.fragment;

import android.os.Bundle;
<<<<<<< Updated upstream
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.R;
import com.example.puskesmom.adapter.DoctorAdapter;
import com.example.puskesmom.model.Doctor;
import com.google.firebase.auth.FirebaseAuth;
=======
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.puskesmom.R;
import com.example.puskesmom.adapter.DoctorAdapter;
import com.example.puskesmom.model.Doctor;
>>>>>>> Stashed changes
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
<<<<<<< Updated upstream
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Doctor> doctorList;
    private DoctorAdapter adapter;

    private LinearLayout layoutEmptyChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rv_doctor_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutEmptyChat = view.findViewById(R.id.layout_empty_chat);

        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(getContext(), doctorList, R.layout.item_doctor);
        recyclerView.setAdapter(adapter);

        loadDoctors();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDoctors();
    }

    private void loadDoctors() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUid)
                .collection("active_chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    doctorList.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // CASE 1: Chats Exist
                        recyclerView.setVisibility(View.VISIBLE);
                        layoutEmptyChat.setVisibility(View.GONE);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Doctor doctor = doc.toObject(Doctor.class);
                            if (doctor.getUid() == null) {

                                doctor = new Doctor(doc.getId(), doctor.getName(), doctor.getSpecialty());
                            }
                            doctorList.add(doctor);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // CASE 2: No Chats
                        recyclerView.setVisibility(View.GONE);
                        layoutEmptyChat.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading chats", Toast.LENGTH_SHORT).show();
                });
    }
}
=======
import java.util.Objects;

public class ChatFragment extends Fragment {

    private RecyclerView rvDoctors;
    private DoctorAdapter doctorAdapter;
    private ArrayList<Doctor> doctorList;
    private FirebaseFirestore db; // Firestore instance

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        rvDoctors = view.findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list and adapter
        doctorList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(doctorList);
        rvDoctors.setAdapter(doctorAdapter);

        // Fetch the doctor data from Firestore
        fetchDoctors();

        return view;
    }

    private void fetchDoctors() {
        // Assuming you have a "users" collection and want to display users who are doctors.
        // You might need a field like "isDoctor" == true to filter them.
        // For this example, we'll fetch all users.
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        doctorList.clear(); // Clear the list before adding new data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Create a Doctor object from the Firestore document
                            // NOTE: You must have a "name" and "specialty" field in your Firestore documents
                            // For workHours, we use a placeholder for now.
                            String name = document.getString("name");
                            String specialty = document.getString("specialty");
                            if (name == null) name = "No Name";
                            if (specialty == null) specialty = "Dokter Umum";

                            doctorList.add(new Doctor(name, specialty, "Jam Kerja: 09.00 - 17.00"));
                        }
                        doctorAdapter.notifyDataSetChanged(); // Refresh the list
                    } else {
                        Log.w("ChatFragment", "Error getting documents.", task.getException());
                    }
                });
    }
}
>>>>>>> Stashed changes
