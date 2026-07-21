package com.mkbilgisayar.tscrts.model;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String companyName;

    public Customer() {}

    public Customer(int id, String name, String phone, String companyName) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.companyName = companyName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @Override
    public String toString() {
        return name + (companyName != null && !companyName.isEmpty() ? " [" + companyName + "]" : "");
    }
}
