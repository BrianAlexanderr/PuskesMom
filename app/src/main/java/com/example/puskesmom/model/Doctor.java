package com.example.puskesmom.model;

import java.io.Serializable;

public class Doctor implements Serializable {
    private String uid;
    private String name;
    private String specialty;

    public Doctor() { } // Required for Firebase

    public Doctor(String uid, String name, String specialty) {
        this.uid = uid;
        this.name = name;
        this.specialty = specialty;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
