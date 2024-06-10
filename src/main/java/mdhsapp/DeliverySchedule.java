/*
Author: Roshan Phakami Punmagar
StudentId- 12201590
FileName-DeliverySchedule.java
Date-07/06/2024
Purpose: This class represents a delivery schedule with details about postcode, delivery day, and delivery cost.
*/
package mdhsapp;

import java.io.Serializable;

 // The DeliverySchedule class represents a delivery schedule with details  about the postcode, delivery day, and delivery cost.
public class DeliverySchedule implements Serializable {
    private int postcode;
    private String deliveryDay;
    private double deliveryCost;

 
    //Constructor to initialize a new DeliverySchedule with given details.
    public DeliverySchedule(int postcode, String deliveryDay, double deliveryCost) {
        this.postcode = postcode;
        this.deliveryDay = deliveryDay;
        this.deliveryCost = deliveryCost;
    }

   
    
     //Gets the postcode for the delivery area. 
    public int getPostcode() {
        return postcode;
    }

   
      //Sets the postcode for the delivery area.
    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }

   
      //Gets the day of delivery.
    public String getDeliveryDay() {
        return deliveryDay;
    }

    
     // Sets the day of delivery. 
    public void setDeliveryDay(String deliveryDay) {
        this.deliveryDay = deliveryDay;
    }

  
      //Gets the cost of delivery.
    public double getDeliveryCost() {
        return deliveryCost;
    }

    
     // Sets the cost of delivery.   
    public void setDeliveryCost(double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    
      //Returns a string representation of the delivery schedule  
    @Override
    public String toString() {
        return "DeliverySchedule{" + "postcode=" + postcode + ", deliveryDay=" + deliveryDay + ", deliveryCost=" + deliveryCost + '}';
    }
}
