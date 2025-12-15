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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        tvName = findViewById(R.id.tv_toolbar_name);
        tvJob = findViewById(R.id.tv_toolbar_job);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.rv_messages);

        tvName.setText(doctor.getName());
        tvJob.setText(doctor.getSpecialty());
        findViewById(R.id.btn_back_chat).setOnClickListener(v -> finish());

        // Setup Chat Room ID (Unique for this pair)
        chatRoomId = getChatRoomId(auth.getCurrentUser().getUid(), doctor.getUid());

        // Setup Recycler
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList);
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

    private void readMessages() {
        db.collection("chats").document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        chatList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            chatList.add(doc.toObject(ChatMessage.class));
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(chatList.size() - 1);
                    }
                });
    }

    private String getChatRoomId(String uid1, String uid2){
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }
}