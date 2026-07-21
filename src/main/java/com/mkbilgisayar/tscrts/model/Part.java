package com.mkbilgisayar.tscrts.model;

public class Part {
    private int id;
    private String name;
    private int stockCount;

    public Part() {}

    public Part(int id, String name, int stockCount) {
        this.id = id;
        this.name = name;
        this.stockCount = stockCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStockCount() { return stockCount; }
    public void setStockCount(int stockCount) { this.stockCount = stockCount; }

    @Override
    public String toString() {
        return name + " (Stock: " + stockCount + ")";
    }
}
