package com.example.admin.navisuber;

import java.util.Date;

public class Order {
    private int orderID;
    private String originPlace;
    private String destinationPlace;
    private Date pickupTime;
    private Date orderedTime;
    private String carType;
    private int seatNumber;

    public Order(int orderID, String originPlace, String destinationPlace, Date pickupTime, Date orderedTime, int seatNumber) {
        this.orderID = orderID;
        this.originPlace = originPlace;
        this.destinationPlace = destinationPlace;
        this.pickupTime = pickupTime;
        this.orderedTime = orderedTime;
        this.seatNumber = seatNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getOriginPlace() {
        return originPlace;
    }

    public String getDestinationPlace() {
        return destinationPlace;
    }

    public Date getPickupTime() {
        return pickupTime;
    }

    public Date getOrderedTime() {
        return orderedTime;
    }
}
