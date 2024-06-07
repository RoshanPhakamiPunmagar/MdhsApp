/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mdhsapp;

import java.io.Serializable;

public class Customer implements Serializable {
    private String fullName;
    private String phone;
    private String email;
    private String password;
    private String address;

    public Customer(String name, String phone, String email, String password, String address) {
        this.fullName = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.address = address;
    }
    
    

    public String getfullName() {
        return fullName;
    }

    public void setfullName(String name) {
        this.fullName = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" + "name=" + fullName + ", phone=" + phone + ", email=" + email + ", password=" + password + ", address=" + address + '}';
    }

    
    
}
