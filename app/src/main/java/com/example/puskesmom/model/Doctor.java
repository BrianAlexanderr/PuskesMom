package com.example.puskesmom.model;

import java.io.Serializable;

public class Doctor implements Serializable {
    private String uid;
    private String name;
    private String specialty;
    private String jamKerja;

    public Doctor() { } // Required for Firebase

    public Doctor(String uid, String name, String specialty) {
        this.uid = uid;
        this.name = name;
        this.specialty = specialty;
    }

    public Doctor(String uid, String name, String specialty, String jamKerja) {
        this.uid = uid;
        this.name = name;
        this.specialty = specialty;
        this.jamKerja = jamKerja;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getJamKerja() { return jamKerja; }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
    public void setJamKerja(String jamKerja) {
        this.jamKerja = jamKerja;
    }
}
