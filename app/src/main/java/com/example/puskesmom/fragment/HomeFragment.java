package com.example.puskesmom.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puskesmom.AddChildActivity;
import com.example.puskesmom.LoginActivity;
import com.example.puskesmom.MyChildActivity;
import com.example.puskesmom.R;
import com.example.puskesmom.RegisterActivity;
import com.example.puskesmom.adapter.CalendarAdapter;
import com.example.puskesmom.adapter.DoctorAdapter;
import com.example.puskesmom.adapter.ImmunizationAdapter;
import com.example.puskesmom.model.Doctor;
import com.example.puskesmom.model.Immunization;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements CalendarAdapter.OnItemListener {

    public HomeFragment() {
        // Required empty public constructor
    }

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    private RecyclerView rvSchedule, rvHomeDoctors;

    private ConstraintLayout cardChildProfile;

    private LinearLayout layoutAddChild;

    private TextView txtChildName, txtChildAge;

    private TextView userName;

    private List<LocalDate> allImmunizationDates = new ArrayList<>();
    FirebaseAuth auth;

    FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initWidgets(view);

        View btnLogout = view.findViewById(R.id.btn_logout);

        selectedDate = LocalDate.now();
        setMonthView();

        rvSchedule = view.findViewById(R.id.rv_schedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvHomeDoctors = view.findViewById(R.id.rv_home_doctors);
        rvHomeDoctors.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        loadHomeDoctors();

        ImageView btnPrev = view.findViewById(R.id.btn_prev_month);
        ImageView btnNext = view.findViewById(R.id.btn_next_month);
        ImageView btnChildDetail = view.findViewById(R.id.btn_child_detail);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.minusMonths(1);
                setMonthView();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.plusMonths(1);
                setMonthView();
            }
        });

        layoutAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), AddChildActivity.class);
                startActivity(i);
            }
        });

        btnChildDetail.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MyChildActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });

    }

    private void loadHomeDoctors() {
        if (auth.getCurrentUser() == null) return;
        String currentUid = auth.getCurrentUser().getUid();

        // 1. First, get the list of doctors the user has ALREADY contacted
        db.collection("users").document(currentUid)
                .collection("active_chats")
                .get()
                .addOnSuccessListener(chatSnapshot -> {
                    // Create a list of IDs to exclude
                    List<String> contactedIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : chatSnapshot) {
                        // Assuming the document ID in 'active_chats' is the Doctor's UID
                        contactedIds.add(doc.getId());
                    }

                    // 2. Pass this list to the next function to filter the doctors
                    fetchUncontactedDoctors(contactedIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error fetching active chats: " + e.getMessage());
                });
    }

    private void fetchUncontactedDoctors(List<String> contactedIds) {
        db.collection("doctor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Doctor> homeList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Doctor doctor = doc.toObject(Doctor.class);

                        // --- Manual UID Fix (Your existing logic) ---
                        String realUid = doctor.getUid();
                        if (realUid == null) {
                            realUid = doc.getId();
                            // Reconstruct object if setter is missing
                            doctor = new Doctor(realUid, doctor.getName(), doctor.getSpecialty());
                        }

                        // --- THE FILTERING LOGIC ---
                        // Only add to the list if the UID is NOT in the contactedIds list
                        if (!contactedIds.contains(realUid)) {
                            homeList.add(doctor);
                        }
                    }

                    // Set Adapter with the filtered list
                    DoctorAdapter homeAdapter = new DoctorAdapter(getContext(), homeList, R.layout.item_doctor_home);
                    rvHomeDoctors.setAdapter(homeAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.d("HomeFragment", "Failed to load doctors: " + e.getMessage());
                });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserName();
        loadChildData();
        loadHomeDoctors();
    }

    private void loadChildData() {
        FirebaseUser currUser = auth.getCurrentUser();
        if (currUser == null) return;
        String uid = currUser.getUid();

        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String selectedChildId = prefs.getString("selected_child_id", null);

        if (selectedChildId != null) {
            loadSpecificChild(uid, selectedChildId);
        } else {
            loadDefaultChild(uid);
        }
    }

    private void loadSpecificChild(String userId, String childId) {
        db.collection("users").document(userId).collection("children").document(childId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateChildUI(userId, documentSnapshot);
                    } else {
                        loadDefaultChild(userId);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error loading child", Toast.LENGTH_SHORT).show());
    }

    private void loadDefaultChild(String userId) {
        db.collection("users").document(userId).collection("children")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document found
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        // Save this as the default selection so it remembers next time
                        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("selected_child_id", document.getId()).apply();

                        updateChildUI(userId, document);
                    } else {
                        // No children at all
                        cardChildProfile.setVisibility(View.GONE);
                        layoutAddChild.setVisibility(View.VISIBLE);
                        allImmunizationDates.clear();
                        setMonthView();
                    }
                });
    }

    private void updateChildUI(String userId, DocumentSnapshot document) {
        cardChildProfile.setVisibility(View.VISIBLE);
        layoutAddChild.setVisibility(View.GONE);

        String childName = document.getString("name");
        String birthDateStr = document.getString("birthDate");

        txtChildName.setText(childName != null ? childName : "Nama Anak");

        // Calculate Age Logic
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                java.time.LocalDate birthDate = java.time.LocalDate.parse(birthDateStr, formatter);
                java.time.LocalDate currentDate = java.time.LocalDate.now();

                if (birthDate.isAfter(currentDate)) {
                    txtChildAge.setText("Belum Lahir");
                } else {
                    java.time.Period period = java.time.Period.between(birthDate, currentDate);
                    String ageText = period.getYears() + " Tahun " + period.getMonths() + " Bulan";
                    txtChildAge.setText(ageText);
                }
            } catch (Exception e) {
                txtChildAge.setText("-");
            }
        } else {
            txtChildAge.setText("-");
        }

        loadChildImmunizations(userId, document.getId());
    }

    private void loadChildImmunizations(String userId, String childId) {
        db.collection("users").document(userId)
                .collection("children").document(childId)
                .collection("immunization")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allImmunizationDates.clear(); // Clear previous child's data
                    List<Immunization> scheduleList = new ArrayList<>();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    LocalDate today = LocalDate.now();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String status = doc.getString("status");
                        String dueDateStr = doc.getString("dueDate"); // "12-01-2026"
                        if (dueDateStr != null) {
                            try {
                                LocalDate date = LocalDate.parse(dueDateStr, formatter);
                                allImmunizationDates.add(date);
                                if (!date.isBefore(today)) {
                                    scheduleList.add(new Immunization(name, status, dueDateStr, date));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // Refresh calendar to show dots
                    setMonthView();

                    Collections.sort(scheduleList);

                    List<Immunization> topTwo = new ArrayList<>();
                    if (scheduleList.size() > 0) topTwo.add(scheduleList.get(0));
                    if (scheduleList.size() > 1) topTwo.add(scheduleList.get(1));

                    // Set Adapter
                    ImmunizationAdapter adapter = new ImmunizationAdapter(topTwo);
                    rvSchedule.setAdapter(adapter);

                })
                .addOnFailureListener(e -> {
                    Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                });
    }

    private void loadUserName() {
        FirebaseUser currUser = auth.getCurrentUser();

        if (currUser != null) {
            String uid = currUser.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {

                            String name = documentSnapshot.getString("name");


                            if (name != null) {
                                userName.setText(name);
                            } else {
                                userName.setText("User");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Use requireContext() for Toast inside Fragment
                        Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                    });
        }
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
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
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter).toUpperCase();
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Mon, 7 = Sun


        int emptyCells = dayOfWeek - 1;


        int totalCells = daysInMonth + emptyCells;


        int rows = (int) Math.ceil((double) totalCells / 7);


        int size = rows * 7;

        for (int i = 1; i <= size; i++) {
            if (i <= emptyCells || i > daysInMonth + emptyCells) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - emptyCells));
            }
        }
        return daysInMonthArray;
    }

    private void initWidgets(View view) {
        calendarRecyclerView = view.findViewById(R.id.calendar_grid_recycler);
        monthYearText = view.findViewById(R.id.text_month);
        userName = view.findViewById(R.id.txt_user_name);

        cardChildProfile = view.findViewById(R.id.card_child_profile);
        layoutAddChild = view.findViewById(R.id.layout_add_child);
        txtChildName = view.findViewById(R.id.txt_child_name_display);
        txtChildAge = view.findViewById(R.id.txt_child_age_display);
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.isEmpty()) {
            String message = "Selected: " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

}