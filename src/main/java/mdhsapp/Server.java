/*
Author: Roshan Phakami Punmagar
StudentId: 12201590
FileName: Server.java
Date: 07/06/2024
Purpose: This server class handles the application's interaction logic, including user registration, login, placing orders, and managing products and delivery schedules.
*/


package mdhsapp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.crypto.*;

public class Server {

    // Port number on which the server will listen
    private static final int PORT = 7777;
    // Public key for encryption
    private static PublicKey publicKey;
    // Private key for decryption
    private static PrivateKey privateKey;
    // Connection to the database
    private static Connection dbConnection;

    public static void main(String[] args) {
        // Generate public and private keys for secure communication
        generateKeys();
        // Set up connection to the database
        establishDatabaseConnection();

        try ( ServerSocket serverSocket = new ServerSocket(PORT)) { // Create a server socket to listen for client connections
            System.out.println("Server is listening on port " + PORT);

            // Infinite loop to accept client connections
            while (true) {
                // Accept incoming client connection
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Start a new thread to handle the client connection
                new ConnectionHandler(socket, privateKey, dbConnection, publicKey).start();
            }
        } catch (IOException e) {
            // Handle exceptions related to I/O operations
            e.printStackTrace();
        }
    }
        
    // Generate RSA public and private keys for secure communication
    private static void generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            // Initialize with a key size of 2048 bits
            keyGen.initialize(2048);
            // Generate the key pair
            KeyPair pair = keyGen.generateKeyPair();
            // Extract the public key
            publicKey = pair.getPublic();
            // Extract the private key
            privateKey = pair.getPrivate();
            System.out.println("Public Key: " + publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

   
    // Establish a connection to the database
    private static void establishDatabaseConnection() {
        try {
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mdhs?", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions related to database connection
        }
    }

    static class ConnectionHandler extends Thread {

        // Socket for communication with the client
        private final Socket socket;
         // Private key for decrypting data from the client
        private final PrivateKey privateKey;
        // Connection to the database
        private final Connection dbConnection;
         // Public key for encrypting data sent to the client
        private final PublicKey publicKey;

            // Constructor to initialize the ConnectionHandler with necessary parameters
        public ConnectionHandler(Socket socket, PrivateKey privateKey, Connection dbConnection, PublicKey publicKey) {
            this.socket = socket;
            this.privateKey = privateKey;
            this.dbConnection = dbConnection;
            this.publicKey = publicKey;
        }

        @Override
        public void run() {
            try {
                // Setting up input and output streams for communication with the client
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Send the server's public key to the client
                sendPublicKey(out);

                // Loop to handle multiple requests from the client
                while (true) {
                    // Read the request from the client
                    String request = in.readUTF();

                      // Process the request based on its type
                    switch (request) {
                        case "REGISTER":
                            handleRegister(in, out);
                            break;
                        case "LOGIN":
                            handleLogin(in, out);
                            break;
                        case "PLACE_ORDER":
                            handlePlaceOrder(in, out);
                            break;
                        case "VIEW_DELIVERY_SCHEDULE":
                            handleViewDeliverySchedule(in, out);
                            break;
                        case "DISPLAY_PRODUCTS":
                            handleDisplayProducts(out);
                            break;
                        case "ADD_PRODUCT":
                            handleAddProduct(in, out);
                            break;
                        case "CREATE_DELIVERY_SCHEDULE":
                            handleCreateDeliverySchedule(in, out);
                            break;
                        case "VIEW_CUSTOMERS":
                            handleViewCustomers(in, out);
                            break;
                        case "VIEW_ORDERS":
                            handleViewOrders(out);
                            break;
                        case "UPDATE_PRODUCT":
                            handleUpdateProduct(in, out);
                            break;
                        case "REMOVE_PRODUCT":
                            handleRemoveProduct(in, out);
                            break;
                        default:
                              // Send a response indicating an invalid request
                            out.writeUTF("INVALID_REQUEST");
                    }
                }
            } catch (IOException | SQLException | GeneralSecurityException e) {
                e.printStackTrace();// Handle exceptions related to I/O, SQL, and cryptographic operations
            } finally {
                // Clean up resources such as socket and database connection
                try {
                    socket.close();
                    dbConnection.close();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();// Handle exceptions during cleanup
                }
            }
        }

       // Method to send the server's public key to the client
    private void sendPublicKey(ObjectOutputStream out) throws IOException {
        byte[] publicKeyBytes = publicKey.getEncoded(); // Get the byte representation of the public key
        out.writeInt(publicKeyBytes.length); // Send the length of the public key
        out.write(publicKeyBytes); // Send the public key bytes
        out.flush(); // Ensure data is sent immediately
    }

        private void handleRegister(ObjectInputStream in, ObjectOutputStream out) throws IOException, SQLException, GeneralSecurityException {
    try {
        // Read user details from the input stream
        String fullName = in.readUTF();  // Read the user's full name
        String phone = in.readUTF();     // Read the user's phone number
        String email = in.readUTF();     // Read the user's email
        byte[] encryptedPassword = new byte[in.readInt()]; // Read the encrypted password
        in.readFully(encryptedPassword); // Read the complete encrypted password
        String address = in.readUTF();   // Read the user's address

        // Decrypt the password using RSA with the server's private key
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        String password = new String(cipher.doFinal(encryptedPassword)); // Decrypt the password

        // Debug output to confirm data reception
        System.out.println("Registering user:");
        System.out.println("Full Name: " + fullName);
        System.out.println("Phone: " + phone);
        System.out.println("Email: " + email);
        System.out.println("Address: " + address);
        System.out.println("Password (hashed): " + password); // Consider hashing the password before storing it

        // Store user details in the database
        String query = "INSERT INTO customers (full_name, phone, email, password, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setString(1, fullName);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.setString(4, password); // Consider hashing the password before storing it
            statement.setString(5, address);
            int rowsInserted = statement.executeUpdate(); // Execute the query

            // Check if the insertion was successful
            if (rowsInserted <= 0) {
                out.writeUTF("FAILURE"); // Inform client of failure
                System.out.println("User registration failed.");
            } else {
                out.writeUTF("SUCCESS"); // Inform client of success
                System.out.println("User registered successfully.");
            }
        } catch (SQLException e) {
            out.writeUTF("FAILURE"); // Inform client of failure due to SQL error
            e.printStackTrace();
        }
    } catch (IOException | GeneralSecurityException e) {
        out.writeUTF("FAILURE"); // Inform client of failure due to exception
        e.printStackTrace();
    }
}

     private void handleLogin(ObjectInputStream in, ObjectOutputStream out) throws IOException, SQLException, GeneralSecurityException {
    try {
        // Read the email address and encrypted password from the client
        String email = in.readUTF();
        byte[] encryptedPassword = new byte[in.readInt()];
        in.readFully(encryptedPassword);

        // Decrypt the password using RSA with the server's private key
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        String password = new String(cipher.doFinal(encryptedPassword));

        // Debug output to confirm receipt of login details
        System.out.println("Received login request for email: " + email);

        // Query the database to find the user's stored password
        String query = "SELECT password FROM customers WHERE email = ?";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            // Check if the user exists in the database
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                // Compare the decrypted password with the stored password
                if (storedPassword.equals(password)) {
                    out.writeUTF("SUCCESS"); // Inform the client of successful login
                    System.out.println("Login successful for email: " + email);
                } else {
                    out.writeUTF("FAILURE"); // Inform the client of failed login due to incorrect password
                    System.out.println("Login failed for email: " + email);
                }
            } else {
                out.writeUTF("FAILURE"); // Inform the client if no user was found
                System.out.println("No user found with email: " + email);
            }
            out.flush();
        } catch (SQLException e) {
            out.writeUTF("FAILURE"); // Handle SQL errors
            e.printStackTrace();
        }
    } catch (IOException | GeneralSecurityException e) {
        out.writeUTF("FAILURE"); // Handle errors related to input/output or security
        e.printStackTrace();
        out.flush();
    }
}

      private void handlePlaceOrder(ObjectInputStream in, ObjectOutputStream out) throws IOException, SQLException {
    // Read the order details from the client
    int customerId = in.readInt();    // Customer ID
    double totalPrice = in.readDouble(); // Total price of the order
    String pName = in.readUTF();      // Name of the product
    String status = in.readUTF();     // Status of the order

    // SQL query to insert the new order into the database
    String query = "INSERT INTO orders (customer_id, product_name, status) VALUES (?, ?, ?)";
    try (PreparedStatement statement = dbConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        // Set the parameters for the prepared statement
        statement.setInt(1, customerId);
        statement.setString(2, pName);
        statement.setString(3, status);

        // Execute the update and retrieve the generated keys
        statement.executeUpdate();

        // Retrieve the generated order ID
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int orderId = generatedKeys.getInt(1);
            // Inform the client of the successful order placement and send the order ID
            out.writeUTF("SUCCESS");
            out.writeInt(orderId);
        } else {
            // Inform the client if the order placement failed
            out.writeUTF("FAILURE");
        }
    } catch (SQLException e) {
        // Handle SQL exceptions and inform the client of the failure
        out.writeUTF("FAILURE");
        e.printStackTrace();
    }
    out.flush(); // Ensure all data is sent to the client
}
        
// Inside your Server class
      private void handleViewDeliverySchedule(ObjectInputStream in, ObjectOutputStream out) {
    try {
        // Receive the schedule ID from the client
        int scheduleId = in.readInt();

        // SQL query to fetch delivery schedules based on the schedule ID
        String query = "SELECT * FROM delivery_schedules WHERE schedule_id = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            // Set the schedule ID parameter for the prepared statement
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                // No schedule found for the given schedule ID
                out.writeUTF("NO_SCHEDULE");
            } else {
                // Schedule found, send a success message to the client
                out.writeUTF("SUCCESS");
                // Send the schedule details to the client
                do {
                    out.writeInt(rs.getInt("postcode")); // Send the postcode
                    out.writeUTF(rs.getString("delivery_day")); // Send the delivery day
                    out.writeDouble(rs.getDouble("delivery_cost")); // Send the delivery cost
                } while (rs.next()); // Continue sending details if there are more rows
            }
            // Indicate no more data
            out.writeBoolean(false);
        } catch (SQLException e) {
            // Handle SQL exceptions and send an error message to the client
            out.writeUTF("ERROR");
            e.printStackTrace();
        }

        // Ensure all data is sent to the client
        out.flush();
    } catch (IOException e) {
        // Handle IO exceptions and log the error
        e.printStackTrace();
    }
}

 private void handleDisplayProducts(ObjectOutputStream out) {
    try {
        // SQL query to retrieve all products
        String query = "SELECT * FROM products";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Retrieve product details from the result set
                int productId = rs.getInt("product_id");
                String productName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String unit = rs.getString("unit");
                double productPrice = rs.getDouble("price");
                String productDescription = rs.getString("ingredients");

                // Check if any string exceeds the maximum length for UTF-8 encoding
                if (productName.length() > Short.MAX_VALUE || unit.length() > Short.MAX_VALUE || productDescription.length() > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("One or more strings exceed the maximum UTF length.");
                }

                // Send product information to the client
                out.writeUTF("PRODUCT");
                out.writeInt(productId);
                out.writeUTF(productName);
                out.writeInt(quantity);
                out.writeUTF(unit);
                out.writeDouble(productPrice);
                out.writeUTF(productDescription);
                out.flush(); // Ensure data is sent immediately
            }

            // Indicate the end of product list
            out.writeUTF("END_OF_PRODUCTS");
            out.flush();
        }
    } catch (SQLException | IOException e) {
        e.printStackTrace();
        // Inform the client about the failure
        try {
            out.writeUTF("FAILURE");
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

    private void handleAddProduct(ObjectInputStream in, ObjectOutputStream out) throws IOException, SQLException {
    try {
        // Read product details from the input stream sent by the client
        String productName = in.readUTF();
        int quantity = in.readInt(); // Read the quantity of the product
        String unit = in.readUTF();
        double price = in.readDouble();
        String ingredients = in.readUTF();

        // Input validation to ensure all necessary fields are provided and valid
        if (productName == null || productName.isEmpty() || unit == null || unit.isEmpty() || price <= 0 || quantity < 0 || ingredients == null || ingredients.isEmpty()) {
            out.writeUTF("FAILURE"); // Send failure response for invalid input
            return;
        }

        // Prepare the SQL query to insert the new product into the database
        String query = "INSERT INTO products (name, quantity, unit, price, ingredients) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            // Set the parameters for the SQL query
            statement.setString(1, productName);
            statement.setInt(2, quantity);
            statement.setString(3, unit);
            statement.setDouble(4, price);
            statement.setString(5, ingredients);

            // Execute the update to insert the new product
            statement.executeUpdate();
            out.writeUTF("SUCCESS"); // Send success response to the client
        } catch (SQLException e) {
            out.writeUTF("FAILURE"); // Send failure response if an SQL exception occurs
            e.printStackTrace();
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE"); // Handle IO exceptions
        e.printStackTrace();
    }
}

       private void handleCreateDeliverySchedule(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    try {
        // Read the necessary data from the input stream sent by the client
        int postcode = in.readInt();
        String deliveryDate = in.readUTF();
        double deliveryCost = in.readDouble();

        // Prepare the SQL query to insert the new delivery schedule
        String query = "INSERT INTO delivery_schedules (delivery_day, delivery_cost, postcode) VALUES (?, ?, ?)";

        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            // Set the parameters for the SQL query
            statement.setString(1, deliveryDate);
            statement.setDouble(2, deliveryCost);
            statement.setInt(3, postcode);

            // Execute the update and check if it was successful
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                out.writeUTF("SUCCESS"); // Send success response to the client
            } else {
                out.writeUTF("FAILURE"); // Send failure response if no rows were affected
            }
        } catch (SQLException e) {
            out.writeUTF("FAILURE"); // Send failure response in case of SQL exceptions
            e.printStackTrace();
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE"); // Handle IO exceptions
        e.printStackTrace();
    }
}

   private void handleViewCustomers(ObjectInputStream in, ObjectOutputStream out) {
    try {
        // SQL query to fetch all customer records from the database
        String query = "SELECT * FROM customers";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through the result set and send each customer record to the client
            while (rs.next()) {
                out.writeUTF("CUSTOMER"); // Indicate that a customer record is being sent
                out.writeInt(rs.getInt("customer_id")); // Send the customer ID
                out.writeUTF(rs.getString("full_name")); // Send the customer's full name
                out.writeUTF(rs.getString("phone")); // Send the customer's phone number
                out.writeUTF(rs.getString("email")); // Send the customer's email address
                out.writeUTF(rs.getString("address")); // Send the customer's address
            }

            // Signal the end of the customer list to the client
            out.writeUTF("END_OF_CUSTOMERS");
            out.flush();
        }
    } catch (SQLException | IOException e) {
        e.printStackTrace();
        try {
            // Send a failure response to the client in case of errors
            out.writeUTF("FAILURE");
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

private void handleViewOrders(ObjectOutputStream out) {
    try {
        // SQL query to fetch all orders from the database
        String query = "SELECT * FROM orders";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through the result set and send each order record to the client
            while (rs.next()) {
                out.writeUTF("ORDER"); // Indicate that an order record is being sent
                out.writeInt(rs.getInt("order_id")); // Send the order ID
                out.writeInt(rs.getInt("customer_id")); // Send the customer ID associated with the order
                out.writeUTF(rs.getString("product_name")); // Send the name of the product ordered
                out.writeUTF(rs.getString("status")); // Send the status of the order
            }

            // Signal the end of the order list to the client
            out.writeUTF("END_OF_ORDERS");
            out.flush();
        }
    } catch (SQLException | IOException e) {
        e.printStackTrace();
        try {
            // Send a failure response to the client in case of errors
            out.writeUTF("FAILURE");
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

      private void handleUpdateProduct(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    try {
        // Read product details sent by the client
        int productId = in.readInt(); // Read the ID of the product to be updated
        String productName = in.readUTF(); // Read the new name for the product
        double price = in.readDouble(); // Read the new price for the product
        String description = in.readUTF(); // Read the new description (ingredients) for the product

        // SQL query to update the product details
        String query = "UPDATE products SET name = ?, price = ?, ingredients = ? WHERE product_id = ?";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            // Set the parameters for the prepared statement
            statement.setString(1, productName); // Set the new product name
            statement.setDouble(2, price); // Set the new product price
            statement.setString(3, description); // Set the new product description
            statement.setInt(4, productId); // Set the product ID for the record to update

            // Execute the update query
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                out.writeUTF("SUCCESS"); // Indicate that the product was successfully updated
            } else {
                out.writeUTF("FAILURE: Product not found or no changes made."); // Indicate failure
            }
            out.flush(); // Ensure the data is sent immediately
        } catch (SQLException e) {
            out.writeUTF("FAILURE: Database error occurred."); // Communicate database error to the client
            out.flush();
            e.printStackTrace(); // Log the error for debugging
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE: Error occurred while processing the update."); // Communicate error during processing to the client
        out.flush();
        e.printStackTrace(); // Log the error for debugging
    }
}

    private void handleRemoveProduct(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    try {
        // Read the product ID from the client's input stream
        int productId = in.readInt();

        // Optional: Validate the product ID (based on your application's logic)
        if (productId <= 0) {
            out.writeUTF("FAILURE: Invalid Product ID"); // Inform the client of invalid product ID
            return;
        }

        // SQL query to delete the product from the database
        String query = "DELETE FROM products WHERE product_id = ?";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setInt(1, productId); // Set the product ID parameter for the query
            int rowsAffected = statement.executeUpdate(); // Execute the delete operation

            // Check if the delete operation was successful
            if (rowsAffected > 0) {
                out.writeUTF("SUCCESS"); // Inform the client of successful deletion
            } else {
                out.writeUTF("FAILURE: Product not found"); // Inform the client if no product was found
            }
        } catch (SQLException e) {
            out.writeUTF("FAILURE: Database error"); // Inform the client of a database error
            e.printStackTrace(); // Log the exception for debugging
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE: Input error"); // Inform the client of an input error
        e.printStackTrace(); // Log the exception for debugging
    }
}
    }
}
