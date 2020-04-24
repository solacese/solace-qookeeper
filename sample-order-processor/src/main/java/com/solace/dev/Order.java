package com.solace.dev;

public class Order {
    public enum Side { BUY, SELL }
    public Order(long id, Side side, String instrument, double price, double quantity) {
        this.id = id;
        this.side = side;
        this.instrument = instrument;
        this.price = price;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }
    public Side getSide() {
        return side;
    }
    public String getInstrument() {
        return instrument;
    }
    public double getPrice() {
        return price;
    }
    public double getQuantity() {
        return quantity;
    }

    private long id;
    private String instrument;
    private double price;
    private double quantity;
    private Side side;
}
