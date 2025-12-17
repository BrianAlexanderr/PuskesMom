package com.example.puskesmom.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.ChatRoomActivity;
import com.example.puskesmom.R;
import com.example.puskesmom.model.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {
    private Context context;
    private List<Doctor> list;
    private int layoutId;

    public DoctorAdapter(Context context, List<Doctor> list, int layoutId) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(this.layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = list.get(position);
        holder.tvName.setText(doctor.getName());
        holder.tvSpecialty.setText(doctor.getSpecialty());

        if (holder.tvHours != null && doctor.getJamKerja() != null) {
            holder.tvHours.setText(doctor.getJamKerja());
        }

        if (holder.btnChat != null) {
            holder.btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatRoomActivity.class);
                intent.putExtra("doctor_data", doctor);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecialty, tvHours, btnChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_doc_name);
            tvSpecialty = itemView.findViewById(R.id.tv_doc_specialty);
            tvHours = itemView.findViewById(R.id.tv_doc_hours);
            btnChat = itemView.findViewById(R.id.btn_chat_doctor);
        }
    }

}
