/*
Author: Roshan Phakami Punmagar
StudentId: 12201590
FileName: Order.java
Date: 07/06/2024
Purpose: This class represents an order in a delivery system with details about order ID, customer ID, product list, order date, delivery date, total price, and order status.
*/
package mdhsapp;

/**
 *
 * @author rajup
 */
import java.io.Serializable;
import java.util.Date;


// Order class represents an order in a delivery system.
//It encapsulates details about the order including order ID, customer ID, product list,order date, delivery date, total price, and order status.
 

public class Order implements Serializable {
    // Attributes
    private int orderId;
    private int customerId;
    private String productList;
    private Date orderDate;
    private Date deliveryDate;
    private double totalPrice;
    private String status; // e.g., "Pending", "Completed", "Cancelled"

    // Parameterized Constructor
    public Order(int orderId, int customerId, String productList, Date orderDate, Date deliveryDate, double totalPrice, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productList = productList;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
    }

    // Default Constructor
    public Order() {
    }

    // Accessor and Mutator methods
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getProductList() {
        return productList;
    }

    public void setProductList(String productList) {
        this.productList = productList;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString method
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", productList=" + productList +
                ", orderDate=" + orderDate +
                ", deliveryDate=" + deliveryDate +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }
}
