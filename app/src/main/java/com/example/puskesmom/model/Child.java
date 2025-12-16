package com.example.puskesmom.model; // Sesuaikan package Anda

public class Child {
    private String name, gender, birthDate, bloodType, documentId;

    // Empty Constructor untuk Firebase
    public Child() {
    }

    public Child(String name, String gender, String birthDate, String bloodType) {
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.bloodType = bloodType;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getBloodType() {
        return bloodType;
    }

    public String getDocumentId() {
        return documentId;
    }

    // Setter for ID
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
