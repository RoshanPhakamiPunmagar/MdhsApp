/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mdhsapp;

import java.io.Serializable;

public class DeliverySchedule implements Serializable {
    private int postcode;
    private String deliveryDay;
     private double deliveryCost;

  public DeliverySchedule(int postcode, String deliveryDay, double deliveryCost) {
    this.postcode = postcode;
    this.deliveryDay = deliveryDay;
    this.deliveryCost = deliveryCost;
}

    // Getters and Setters

    public int getPostcode() {
        return postcode;
    }

    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }

    public String getDeliveryDay() {
        return deliveryDay;
    }

    public void setDeliveryDay(String deliveryDay) {
        this.deliveryDay = deliveryDay;
    }

    public double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    @Override
    public String toString() {
        return "DeliverySchedule{" + "postcode=" + postcode + ", deliveryDay=" + deliveryDay + ", deliveryCost=" + deliveryCost + '}';
    }
    
    

   
    
}
