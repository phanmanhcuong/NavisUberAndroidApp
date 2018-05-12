package com.example.admin.navisuber;

public class Order {
    private int orderID;
    private int carID;
    private String originPlace;
    private String destinationPlace;
    private String pickupTime;
    private String orderedTime;
    private String carType;
    //private int seatNumber;

    public Order(int orderID, int carID, String originPlace, String destinationPlace, String pickupTime, String orderedTime) {
        this.orderID = orderID;
        this.carID = carID;
        this.originPlace = originPlace;
        this.destinationPlace = destinationPlace;
        this.pickupTime = pickupTime;
        this.orderedTime = orderedTime;
        //this.seatNumber = seatNumber;
    }

    public int getOrderID() {
        return orderID;
    }

    public int getCarID() {
        return carID;
    }

//    public int getSeatNumber() {
//        return seatNumber;
//    }

    public String getOriginPlace() {
        return originPlace;
    }

    public String getDestinationPlace() {
        return destinationPlace;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public String getOrderedTime() {
        return orderedTime;
    }
}
