package com.example.admin.navisuber;

/**
 * Created by Admin on 3/26/2018.
 */

public class Car {
    private String carType;

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public Car(String carType) {

        this.carType = carType;
    }
}
