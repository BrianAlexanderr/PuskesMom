package com.example.puskesmom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.adapter.ChildAdapter;
import com.example.puskesmom.model.Child;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyChildActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChildAdapter adapter;
    private List<Child> childList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_child);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup Tombol
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnAdd = findViewById(R.id.btn_add_child_header);

        btnBack.setOnClickListener(v -> finish());

        // Logika Tombol ADD CHILD di pojok kanan atas
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MyChildActivity.this, AddChildActivity.class);
            startActivity(intent);
        });

        // Setup Recycler
        recyclerView = findViewById(R.id.rv_child_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        childList = new ArrayList<>();
        adapter = new ChildAdapter(this, childList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Memuat ulang data setiap kali halaman ini dibuka
        loadChildren();
    }

    private void loadChildren() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("children")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childList.clear(); // Bersihkan list lama agar tidak duplikat
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert Firestore document ke Object ChildModel
                        Child child = doc.toObject(Child.class);
                        child.setDocumentId(doc.getId()); // Simpan ID dokumen jika butuh edit/hapus nanti
                        childList.add(child);
                    }
                    adapter.notifyDataSetChanged(); // Update tampilan
                })
                .addOnFailureListener(e -> {
                    Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                });
    }
}