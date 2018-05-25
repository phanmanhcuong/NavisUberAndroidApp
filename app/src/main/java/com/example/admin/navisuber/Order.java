package com.example.admin.navisuber;

public class Order {
    private int orderID;
    private int carID;
    private String originPlace;
    private String destinationPlace;
    private String pickupTime;
    private String orderedTime;
    private String driverName;
    private String driverPhoneNumber;
    private String carPlate;
    private String carType;

    public Order(int orderID, int carID, String originPlace, String destinationPlace, String pickupTime, String orderedTime) {
        this.orderID = orderID;
        this.carID = carID;
        this.originPlace = originPlace;
        this.destinationPlace = destinationPlace;
        this.pickupTime = pickupTime;
        this.orderedTime = orderedTime;
    }

    public Order(int orderID, int carID, String originPlace, String destinationPlace, String pickupTime, String orderedTime, String driverName, String driverPhoneNumber, String carPlate, String carType) {
        this.orderID = orderID;
        this.carID = carID;
        this.originPlace = originPlace;
        this.destinationPlace = destinationPlace;
        this.pickupTime = pickupTime;
        this.orderedTime = orderedTime;
        this.driverName = driverName;
        this.driverPhoneNumber = driverPhoneNumber;
        this.carPlate = carPlate;
        this.carType = carType;
    }

    public int getOrderID() {
        return orderID;
    }

    public int getCarID() {
        return carID;
    }

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

    public String getDriverName() {
        return driverName;
    }

    public String getDriverPhoneNumber() {
        return driverPhoneNumber;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public String getCarType() {
        return carType;
    }
}
