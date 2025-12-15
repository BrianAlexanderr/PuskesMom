package com.example.puskesmom.model;

import java.time.LocalDate;

public class Immunization implements Comparable<Immunization>{
    private String name;
    private String status;
    private String dueDateStr;
    private LocalDate dueDate; // Helper for sorting

    public Immunization(String name, String status, String dueDateStr, LocalDate dueDate) {
        this.name = name;
        this.status = status;
        this.dueDateStr = dueDateStr;
        this.dueDate = dueDate;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getDueDateStr() { return dueDateStr; }
    public LocalDate getDueDate() { return dueDate; }

    // Allows sorting by date automatically
    @Override
    public int compareTo(Immunization o) {
        return this.dueDate.compareTo(o.dueDate);
    }
}
