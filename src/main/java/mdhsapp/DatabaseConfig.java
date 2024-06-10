/*
Author: Roshan Phakami Punmagar
StudentId- 12201590
FileName-DatabaseConfig.java
Date-07/06/2024
Purpose: This class provides a configuration for connecting to the database.
*/

package mdhsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


  //The DatabaseConfig class provides a method to establish a connection to the MySQL database using JDBC.
 
public class DatabaseConfig {
    // JDBC URL for the MySQL database
    private static final String URL = "jdbc:mysql://localhost:3306/mdhs?";
    
    // Database user
    private static final String USER = "root";
    
    // Database user's password
    private static final String PASSWORD = "root";


      //Establishes a connection to the MySQL database.
    public static Connection getConnection() throws SQLException {
        // Return a connection to the MySQL database
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
