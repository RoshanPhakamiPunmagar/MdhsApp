/*
Author: Roshan Phakami Punmagar
StudentId: 12201590
FileName: Product.java
Date: 07/06/2024
Purpose: This class represents a product in a delivery system with details about the product ID, name, quantity, unit, price, and ingredients.
*/

package mdhsapp;

import java.io.Serializable;


 //Product class represents a product in the delivery system.
 //It encapsulates details about a product such as ID, name, quantity, unit of measurement, price, and ingredients.
 
public class Product implements Serializable {
    // Instance Variables
    private int id;
    private String name;
    private int quantity;
    private String unit;
    private double price;
    private String ingredients;

   
     // Constructor to initialize a new Product with specified details. 
    public Product(String name, int quantity, String unit, double price, String ingredients) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.ingredients = ingredients;
    }

    // Getter and setter methods for the product attributes

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    
      //Returns a string representation of the Product object.
    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", name='" + name + '\'' + ", quantity=" + quantity + ", unit='" + unit + '\'' + ", price=" + price + ", ingredients='" + ingredients + '\'' + '}';
    }
}
