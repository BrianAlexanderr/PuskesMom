package com.example.puskesmom;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.adapter.ChatAdapter;
import com.example.puskesmom.model.ChatMessage;
import com.example.puskesmom.model.Doctor;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView btnSend;
    private TextView tvName, tvJob;

    private Doctor doctor;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String chatRoomId;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Get Doctor Data
        doctor = (Doctor) getIntent().getSerializableExtra("doctor_data");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Init Views
        tvName = findViewById(R.id.tvDoctorName);
        tvJob = findViewById(R.id.tvDoctorSpecialty);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.rvChatMessages);

        tvName.setText(doctor.getName());
        tvJob.setText(doctor.getSpecialty());

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        FirebaseUser currentUser = auth.getCurrentUser();

        // Setup Chat Room ID (Unique for this pair)
        chatRoomId = getChatRoomId(auth.getCurrentUser().getUid(), doctor.getUid());

        // Setup Recycler
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(this, chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Listen for Messages
        readMessages();

        // Send Message
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
            }
        });
    }

    private void sendMessage(String msg) {
        assert auth.getCurrentUser() != null;
        ChatMessage chat = new ChatMessage(
                auth.getCurrentUser().getUid(),
                doctor.getUid(),
                msg,
                new Timestamp(new Date())
        );

        db.collection("chats").document(chatRoomId)
                .collection("messages")
                .add(chat)
                .addOnSuccessListener(documentReference -> etMessage.setText(""));


        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("active_chats").document(doctor.getUid())
                .set(doctor);
    }

//    private void readMessages() {
//        db.collection("chats").document(chatRoomId)
//                .collection("messages")
//                .orderBy("timestamp", Query.Direction.ASCENDING)
//                .addSnapshotListener((value, error) -> {
//                    if (error != null) return;
//                    if (value != null) {
//                        chatList.clear();
//                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
//                            chatList.add(doc.toObject(ChatMessage.class));
//                        }
//                        adapter.notifyDataSetChanged();
//                        recyclerView.scrollToPosition(chatList.size() - 1);
//                    }
//                });
//    }

    private void readMessages() {
        db.collection("chats").document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatRoomActivity", "Error listening for messages.", error);
                        return;
                    }
                    if (value != null) {
                        chatList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            if (message != null) { // Add a null check for safety
                                chatList.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Scroll to bottom only if there are messages
                        if (!chatList.isEmpty()) {
                            recyclerView.scrollToPosition(chatList.size() - 1);
                        }
                    }
                });
    }


    //    private void addDoctorToActiveChats(String currentUserId) {
//        // Create a map object from the Doctor model.
//        // This will store the doctor's name and specialty.
//        // We don't need to store the UID because it will be the document's ID.
//        Map<String, Object> doctorData = new HashMap<>();
//        doctorData.put("name", doctor.getName());
//        doctorData.put("specialty", doctor.getSpecialty());
//        // NOTE: We are NOT putting the UID here on purpose.
//
//        // Get a reference to the doctor's document inside the user's 'active_chats' sub-collection
//        DocumentReference doctorRef = db.collection("users").document(currentUserId)
//                .collection("active_chats").document(doctor.getUid());
//
//        // Use .set() to create or overwrite the document. This ensures that
//        // even if the user chats 100 times, the doctor is only listed once.
//        doctorRef.set(doctorData)
//                .addOnSuccessListener(aVoid -> {
//                    androidx.camera.camera2.pipe.core.Log.d("ChatRoomActivity", "Successfully added doctor to active chats.");
//                })
//                .addOnFailureListener(e -> {
//                    androidx.camera.camera2.pipe.core.Log.w("ChatRoomActivity", "Error adding doctor to active chats.", e);
//                });
//    }
    private String getChatRoomId(String uid1, String uid2){
        if (uid1 == null || uid2 == null) {
            Log.e("ChatRoomId", "Cannot generate chat room ID, one of the UIDs is null.");
            // Return a default or error ID to prevent a crash
            return "error_null_uids";
        }
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }
}