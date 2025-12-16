package com.example.puskesmom.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.R;
import com.example.puskesmom.model.Child;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ViewHolder> {

    private Context context;
    private List<Child> childList;

    public ChildAdapter(Context context, List<Child> childList) {
        this.context = context;
        this.childList = childList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Child child = childList.get(position);
        holder.tvName.setText(child.getName());
        holder.tvGender.setText(child.getGender());

        // Hitung Umur
        if (child.getBirthDate() != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate birthDate = LocalDate.parse(child.getBirthDate(), formatter);
                LocalDate now = LocalDate.now();
                Period period = Period.between(birthDate, now);
                holder.tvAge.setText(period.getYears() + " Thn " + period.getMonths() + " Bln");
            } catch (Exception e) {
                holder.tvAge.setText("-");
            }
        }

        holder.itemView.setOnClickListener(v -> {
            // 1. Save the selected Child ID to SharedPreferences
            android.content.SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.putString("selected_child_id", child.getDocumentId()); // Save the Firestore ID
            editor.apply();

            // 2. Show feedback
            android.widget.Toast.makeText(context, "Selected: " + child.getName(), android.widget.Toast.LENGTH_SHORT).show();

            // 3. Close the MyChildActivity to go back to Home
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAge, tvGender;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvAge = itemView.findViewById(R.id.tv_child_age);
            tvGender = itemView.findViewById(R.id.tv_child_gender);
        }
    }
}
