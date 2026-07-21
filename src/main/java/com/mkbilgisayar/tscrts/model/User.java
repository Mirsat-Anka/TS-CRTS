package com.mkbilgisayar.tscrts.model;

public class User {
    private int id;
    private String name;
    private String role; // 'manager', 'engineer', 'technician', 'it_staff', 'customer_support'
    private String passwordHash;

    public User() {}

    public User(int id, String name, String role, String passwordHash) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
