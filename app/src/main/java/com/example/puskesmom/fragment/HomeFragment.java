package com.example.puskesmom.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puskesmom.R;
import com.example.puskesmom.adapter.CalendarAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements CalendarAdapter.OnItemListener {

    public HomeFragment() {
        // Required empty public constructor
    }

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

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

        initWidgets(view);

        selectedDate = LocalDate.now();
        setMonthView();

        ImageView btnPrev = view.findViewById(R.id.btn_prev_month);
        ImageView btnNext = view.findViewById(R.id.btn_next_month);

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

    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        List<String> eventDays = new ArrayList<>();
        eventDays.add("5");  // Example: Event on the 5th
        eventDays.add("11"); // Example: Event on the 11th
        eventDays.add("21"); // Example: Event on the 21st

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, eventDays, this);

        // Use requireContext() for LayoutManager in Fragments
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

        // Calculate empty cells at the start
        int emptyCells = dayOfWeek - 1;

        // Calculate total cells needed (days + empty start slots)
        int totalCells = daysInMonth + emptyCells;

        // Calculate exact number of rows needed (dividing by 7 columns)
        // If totalCells is 31, rows = 5. If totalCells is 38, rows = 6.
        int rows = (int) Math.ceil((double) totalCells / 7);

        // The final size must be a multiple of 7 to complete the grid
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
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.isEmpty()) {
            String message = "Selected: " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

}