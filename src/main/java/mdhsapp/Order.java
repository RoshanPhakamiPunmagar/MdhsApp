/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mdhsapp;

/**
 *
 * @author rajup
 */
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    // Attributes
    private int orderId;
    private int customerId;
    private List<Product> productList;
    private Date orderDate;
    private Date deliveryDate;
    private double totalPrice;
    private String status; // e.g., "Pending", "Completed", "Cancelled"

    // Constructor
    public Order(int orderId, int customerId, List<Product> productList, Date orderDate, Date deliveryDate, double totalPrice, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productList = productList;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.totalPrice = totalPrice;
        this.status = status;
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

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
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
