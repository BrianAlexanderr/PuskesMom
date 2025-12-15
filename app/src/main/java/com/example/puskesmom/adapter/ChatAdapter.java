package com.example.puskesmom.adapter;

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
    private List<ChatMessage> chatList;
    private String currentUid;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
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

    @Override
    public int getItemViewType(int position) {
        if (chatList.get(position).getSenderId().equals(currentUid)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
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
