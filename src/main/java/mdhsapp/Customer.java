/*
Author: Roshan Phakami Punmagar
StudentId- 12201590
FileName-Customer.java
Date-07/06/2024
Purpose: This class represents a Customer with their details and serves as a model for customer information.
*/
package mdhsapp;

import java.io.Serializable;


  //The Customer class represents a customer with their details.

 
public class Customer implements Serializable {
    private String fullName;
    private String phone;
    private String email;
    private String password;
    private String address;

    //Constructor to initialize a new Customer with given details.
    public Customer(String name, String phone, String email, String password, String address) {
        this.fullName = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.address = address;
    }

    
     // Gets the full name of the customer.      
    public String getFullName() {
        return fullName;
    }

  
     //Sets the full name of the customer.  
    public void setFullName(String name) {
        this.fullName = name;
    }

 
     //Gets the phone number of the customer.
    public String getPhone() {
        return phone;
    }

     // Sets the phone number of the customer. 
    public void setPhone(String phone) {
        this.phone = phone;
    }

    
     // Gets the email address of the customer.   
    public String getEmail() {
        return email;
    }

    
    // Sets the email address of the customer. 
    public void setEmail(String email) {
        this.email = email;
    }

    
     // Gets the password of the customer.
    public String getPassword() {
        return password;
    }

    //Sets the password of the customer.
    public void setPassword(String password) {
        this.password = password;
    }
  
     // Gets the address of the customer.
     
    public String getAddress() {
        return address;
    }

    
     //Sets the address of the customer.
    public void setAddress(String address) {
        this.address = address;
    }

    
     // Returns a string representation of the customer.
    @Override
    public String toString() {
        return "Customer{" + "name=" + fullName + ", phone=" + phone + ", email=" + email + ", password=" + password + ", address=" + address + '}';
    }
}
