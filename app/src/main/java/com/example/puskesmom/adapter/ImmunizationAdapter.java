package com.example.puskesmom.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.R;
import com.example.puskesmom.model.Immunization;

import java.util.List;

public class ImmunizationAdapter extends RecyclerView.Adapter<ImmunizationAdapter.ViewHolder> {

    private List<Immunization> list;

    public ImmunizationAdapter(List<Immunization> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Immunization item = list.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDate.setText(item.getDueDateStr());
        holder.tvStatus.setText("STATUS: " + item.getStatus().toUpperCase());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_schedule_name);
            tvDate = itemView.findViewById(R.id.tv_schedule_date);
            tvStatus = itemView.findViewById(R.id.tv_schedule_status);
        }
    }
}
