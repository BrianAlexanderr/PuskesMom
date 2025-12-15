package com.example.puskesmom.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puskesmom.R;
import com.example.puskesmom.adapter.CalendarAdapter;
import com.example.puskesmom.adapter.ImmunizationAdapter;
import com.example.puskesmom.model.Immunization;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ScheduleFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private RecyclerView scheduleRecyclerView; // The bottom list
    private LocalDate selectedDate;

    private LinearLayout emptyStateLayout;

    // Data Storage
    private List<LocalDate> allImmunizationDates = new ArrayList<>();
    private List<Immunization> allSchedules = new ArrayList<>(); // Store ALL schedules here

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initWidgets(view);

        selectedDate = LocalDate.now();
        setMonthView(); // Initial setup

        // Navigation Arrows
        view.findViewById(R.id.btn_prev_month_sched).setOnClickListener(v -> {
            selectedDate = selectedDate.minusMonths(1);
            setMonthView();
        });

        view.findViewById(R.id.btn_next_month_sched).setOnClickListener(v -> {
            selectedDate = selectedDate.plusMonths(1);
            setMonthView();
        });

        // Load Data
        loadChildData();
    }

    private void initWidgets(View view) {
        calendarRecyclerView = view.findViewById(R.id.rv_calendar_sched);
        monthYearText = view.findViewById(R.id.text_month_sched);
        scheduleRecyclerView = view.findViewById(R.id.rv_schedule_full);

        // Setup Schedule List
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateLayout = view.findViewById(R.id.layout_empty_schedule);
    }

    private void loadChildData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String selectedChildId = prefs.getString("selected_child_id", null);

        if (selectedChildId != null) {
            loadImmunizations(uid, selectedChildId);
        } else {
            // Fallback: Fetch first child if none selected (Reuse logic from Home)
            db.collection("users").document(uid).collection("children").limit(1).get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String childId = query.getDocuments().get(0).getId();
                            prefs.edit().putString("selected_child_id", childId).apply();
                            loadImmunizations(uid, childId);
                        }
                    });
        }
    }

    private void loadImmunizations(String userId, String childId) {
        db.collection("users").document(userId)
                .collection("children").document(childId)
                .collection("immunization")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allImmunizationDates.clear();
                    allSchedules.clear();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String status = doc.getString("status");
                        String dueDateStr = doc.getString("dueDate");

                        if (dueDateStr != null) {
                            try {
                                LocalDate date = LocalDate.parse(dueDateStr, formatter);

                                // 1. Add to Dates List (For Calendar Dots)
                                allImmunizationDates.add(date);

                                // 2. Add to Full Schedule List
                                allSchedules.add(new Immunization(
                                        name != null ? name : "Immunization",
                                        status != null ? status : "Upcoming",
                                        dueDateStr,
                                        date
                                ));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Sort data by date
                    Collections.sort(allSchedules);

                    // Refresh Views
                    setMonthView(); // Update Calendar Dots
                    updateScheduleList(); // Update Bottom List
                })
                .addOnFailureListener(e -> Log.d("ERROR", Objects.requireNonNull(e.getMessage())));
    }

    // --- LOGIC: Update Calendar & List based on Month ---
    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        // Filter Dots for Calendar
        List<String> eventDays = new ArrayList<>();
        for (LocalDate date : allImmunizationDates) {
            if (date.getMonthValue() == selectedDate.getMonthValue() &&
                    date.getYear() == selectedDate.getYear()) {
                eventDays.add(String.valueOf(date.getDayOfMonth()));
            }
        }

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, eventDays, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Also update the list below whenever month changes
        updateScheduleList();
    }

    private void updateScheduleList() {

        List<Immunization> filteredList = new ArrayList<>();

        for (Immunization item : allSchedules) {
            LocalDate date = item.getDueDate();
            if (date.getMonthValue() == selectedDate.getMonthValue() &&
                    date.getYear() == selectedDate.getYear()) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()){
            scheduleRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            scheduleRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);

            ImmunizationAdapter adapter = new ImmunizationAdapter(filteredList);
            scheduleRecyclerView.setAdapter(adapter);
        }
    }

    // --- Helper Methods (Same as HomeFragment) ---
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter).toUpperCase();
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int emptyCells = dayOfWeek - 1;
        int size = (int) Math.ceil((double) (daysInMonth + emptyCells) / 7) * 7;

        for (int i = 1; i <= size; i++) {
            if (i <= emptyCells || i > daysInMonth + emptyCells) daysInMonthArray.add("");
            else daysInMonthArray.add(String.valueOf(i - emptyCells));
        }
        return daysInMonthArray;
    }

    @Override
    public void onItemClick(int position, String dayText) {

    }
}