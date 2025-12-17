package com.example.puskesmom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.R;
import com.example.puskesmom.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<ChatMessage> chatList;
    private String currentUid;

//    public ChatAdapter(List<ChatMessage> chatList) {
//        this.chatList = chatList;
//        this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//    }
    public ChatAdapter(Context context, List<ChatMessage> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Get the current user's ID once to use for comparison
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == MSG_TYPE_RIGHT) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
//            return new ViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
//            return new ViewHolder(view);
//        }
//    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the context to inflate the correct layout (left or right bubble)
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chat = chatList.get(position);
        holder.message.setText(chat.getMessage());
    }

    @Override
    public int getItemCount() { return chatList.size(); }

//    @Override
//    public int getItemViewType(int position) {
//        if (chatList.get(position).getSenderId().equals(currentUid)) {
//            return MSG_TYPE_RIGHT;
//        } else {
//            return MSG_TYPE_LEFT;
//        }
//    }
    @Override
    public int getItemViewType(int position) {
        // Compare the message sender's ID with the current user's ID
        if (chatList.get(position).getSenderId().equals(currentUid)) {
            return MSG_TYPE_RIGHT; // It's our message
        } else {
            return MSG_TYPE_LEFT;  // It's the other person's message
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure both item_chat_left and item_chat_right have a TextView with id "tv_show_message"
            message = itemView.findViewById(R.id.tv_show_message);
        }
    }
}
