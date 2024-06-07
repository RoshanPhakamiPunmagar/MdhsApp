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
    private static final int PORT = 7777;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static Connection dbConnection;

 public static void main(String[] args) {
    generateKeys();
    establishDatabaseConnection();
    
    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
        System.out.println("Server is listening on port " + PORT);
        
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected");
            
            // Start a new thread to handle the client connection
            new ConnectionHandler(socket, privateKey, dbConnection, publicKey).start();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    private static void generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            publicKey = pair.getPublic();
            privateKey = pair.getPrivate();
            System.out.println("Public Key: " + publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void establishDatabaseConnection() {
        try {
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mdhs?", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static class ConnectionHandler extends Thread {
        private Socket socket;
        private PrivateKey privateKey;
        private Connection dbConnection;
        private PublicKey publicKey;

        public ConnectionHandler(Socket socket, PrivateKey privateKey, Connection dbConnection, PublicKey publicKey) {
            this.socket = socket;
            this.privateKey = privateKey;
            this.dbConnection = dbConnection;
            this.publicKey = publicKey;
        }

        @Override
        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Send the server's public key to the client
                sendPublicKey(out);

                while (true) {
                    String request = in.readUTF();

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
                            handleViewOrders(in, out);
                            break;
                        case "UPDATE_PRODUCT":
                            handleUpdateProduct(in, out);
                            break;
                        case "REMOVE_PRODUCT":
                            handleRemoveProduct(in, out);
                            break;
                        default:
                            out.writeUTF("INVALID_REQUEST");
                    }
                }
            } catch (IOException | SQLException | GeneralSecurityException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    dbConnection.close();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendPublicKey(ObjectOutputStream out) throws IOException {
            byte[] publicKeyBytes = publicKey.getEncoded();
            out.writeInt(publicKeyBytes.length);
            out.write(publicKeyBytes);
            out.flush();
        }

     private void handleRegister(ObjectInputStream in, ObjectOutputStream out) throws IOException, SQLException, GeneralSecurityException {
    try {
        String fullName = in.readUTF();
        String phone = in.readUTF();
        String email = in.readUTF();
        byte[] encryptedPassword = new byte[in.readInt()];
        in.readFully(encryptedPassword);
        String address = in.readUTF();

        // Decrypt password
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        String password = new String(cipher.doFinal(encryptedPassword));
        
        // Debug output
        System.out.println("Registering user:");
        System.out.println("Full Name: " + fullName);
        System.out.println("Phone: " + phone);
        System.out.println("Email: " + email);
        System.out.println("Address: " + address);
        System.out.println("Password (hashed): " + password);

        // Store user details in the database
        String query = "INSERT INTO customers (full_name, phone, email, password, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setString(1, fullName);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.setString(4, password);
            statement.setString(5, address);
            int rowsInserted = statement.executeUpdate();

            // Check if insert was successful
            if (rowsInserted > 0) {
                out.writeUTF("SUCCESS");
                System.out.println("User registered successfully.");
            } else {
                out.writeUTF("FAILURE");
                System.out.println("User registration failed.");
            }
        } catch (SQLException e) {
            out.writeUTF("FAILURE");
            e.printStackTrace();
        }
    } catch (IOException | GeneralSecurityException e) {
        out.writeUTF("FAILURE");
        e.printStackTrace();
    }
}

   private void handleLogin(ObjectInputStream in, ObjectOutputStream out) throws IOException, SQLException, GeneralSecurityException {
    try {
        String email = in.readUTF();
        byte[] encryptedPassword = new byte[in.readInt()];
        in.readFully(encryptedPassword);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        String password = new String(cipher.doFinal(encryptedPassword));

        System.out.println("Received login request for email: " + email);

        String query = "SELECT password FROM customers WHERE email = ?";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                if (storedPassword.equals(password)) {
                    out.writeUTF("SUCCESS");
                    System.out.println("Login successful for email: " + email);
                } else {
                    out.writeUTF("FAILURE");
                    System.out.println("Login failed for email: " + email);
                }
            } else {
                out.writeUTF("FAILURE");
                System.out.println("No user found with email: " + email);
            }
            out.flush();
        } catch (SQLException e) {
            out.writeUTF("FAILURE");
            e.printStackTrace();
        }
    } catch (IOException | GeneralSecurityException e) {
        out.writeUTF("FAILURE");
        e.printStackTrace();
        out.flush();
    }
}

private void handlePlaceOrder(ObjectInputStream in, ObjectOutputStream out) throws IOException, SQLException {
    int customerId = in.readInt();
    double totalPrice = in.readDouble();

    String query = "INSERT INTO orders (customer_id, total_price) VALUES (?, ?)";
    try (PreparedStatement statement = dbConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        statement.setInt(1, customerId);
        statement.setDouble(2, totalPrice);
        statement.executeUpdate();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int orderId = generatedKeys.getInt(1);
            out.writeUTF("SUCCESS");
            out.writeInt(orderId);
        } else {
            out.writeUTF("FAILURE");
        }
    } catch (SQLException e) {
        out.writeUTF("FAILURE");
        e.printStackTrace();
    }
    out.flush();
}


// Inside your Server class

private void handleViewDeliverySchedule(ObjectInputStream in, ObjectOutputStream out) {
    try {
        // Receive schedule ID from the client
        int scheduleId = in.readInt();

        // Query the database for delivery schedules with the given schedule ID
        String query = "SELECT * FROM delivery_schedules WHERE schedule_id = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                // If no schedules are found, send a message to the client indicating so
                out.writeUTF("NO_SCHEDULE");
            } else {
                // If schedules are found, send a success message to the client
                out.writeUTF("SUCCESS");
                do {
                    // Send schedule details to the client
                    out.writeInt(rs.getInt("postcode"));
                    out.writeUTF(rs.getString("delivery_day"));
                    out.writeDouble(rs.getDouble("delivery_cost"));
                } while (rs.next());
            }
            // Indicate no more data
            out.writeBoolean(false);
        } catch (SQLException e) {
            // Send an error message to the client
            out.writeUTF("ERROR");
            e.printStackTrace();
        }

        // Flush the output stream to ensure the data is sent immediately
        out.flush();
    } catch (IOException e) {
        // If an error occurs during communication with the client, print the stack trace
        e.printStackTrace();
    }
}


private void handleDisplayProducts(ObjectOutputStream out) {
    try {
        String query = "SELECT * FROM products";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String unit = rs.getString("unit");
                Double productPrice = rs.getDouble("price");
                String productDescription = rs.getString("ingredients");
               
                out.writeUTF("PRODUCT");
                out.writeInt(productId);
                out.writeUTF(productName);
                out.writeInt(quantity);
                out.writeUTF(unit);
                out.writeDouble(productPrice);
                out.writeUTF(productDescription);
                
            }

            out.writeUTF("END_OF_PRODUCTS");
            
        }
    } catch (SQLException | IOException e) {
        e.printStackTrace();
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
        // Read product details from input stream
        String productName = in.readUTF();
        int quantity = in.readInt(); // Assuming quantity is sent as an integer
        String unit = in.readUTF();
        double price = in.readDouble();
        String ingredients = in.readUTF();

        // Input validation
        if (productName == null || productName.isEmpty() || unit == null || unit.isEmpty() || price <= 0 || quantity < 0 || ingredients == null || ingredients.isEmpty()) {
            out.writeUTF("FAILURE");
            return;
        }

        // Prepare SQL query to insert the product
        String query = "INSERT INTO products (name, quantity, unit, price, ingredients) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setString(1, productName);
            statement.setInt(2, quantity);
            statement.setString(3, unit);
            statement.setDouble(4, price);
            statement.setString(5, ingredients);

            // Execute the update
            statement.executeUpdate();
            out.writeUTF("SUCCESS");
        } catch (SQLException e) {
            out.writeUTF("FAILURE");
            e.printStackTrace();
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE");
        e.printStackTrace();
    }
}


private void handleCreateDeliverySchedule(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    try {
        // Read the necessary data from the input stream
        String deliveryDate = in.readUTF();
        int postcode = in.readInt();
        double deliveryCost = in.readDouble(); 

        String query = "INSERT INTO delivery_schedules (delivery_day, delivery_cost, postcode) VALUES (?, ?, ?)";

        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            // Set parameters for the query
            statement.setString(1, deliveryDate);
            statement.setDouble(2, deliveryCost);
            statement.setInt(3, postcode);

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                // Retrieve the auto-generated key if schedule_id is auto-generated
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long scheduleId = generatedKeys.getLong(1);
                        // You can use scheduleId if needed
                    }
                }
                out.writeUTF("SUCCESS");
            } else {
                out.writeUTF("FAILURE");
            }
        } catch (SQLException e) {
            out.writeUTF("FAILURE");
            e.printStackTrace();
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE");
        e.printStackTrace();
    }
}


   private void handleViewCustomers(ObjectInputStream in, ObjectOutputStream out) {
    String query = "SELECT * FROM customers";
    try (PreparedStatement statement = dbConnection.prepareStatement(query);
         ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {
            out.writeUTF("CUSTOMER");
            out.writeInt(resultSet.getInt("customer_id"));
            out.writeUTF(resultSet.getString("full_name"));
            out.writeUTF(resultSet.getString("phone"));
            out.writeUTF(resultSet.getString("email"));
            out.writeUTF(resultSet.getString("address"));
        }
        out.writeUTF("END_OF_CUSTOMERS");
    } catch (SQLException e) {
        try {
            out.writeUTF("FAILURE");
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
}




      private void handleViewOrders(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    String query = "SELECT * FROM orders";
    
    try (PreparedStatement statement = dbConnection.prepareStatement(query);
         ResultSet resultSet = statement.executeQuery()) {
         
        while (resultSet.next()) {
            out.writeUTF("ORDER");
            out.writeInt(resultSet.getInt("order_id"));
            out.writeInt(resultSet.getInt("customer_id"));
            out.writeDouble(resultSet.getDouble("total_price"));
            out.writeUTF(resultSet.getString("order_date"));
        }
        
        out.writeUTF("END_OF_ORDERS");
    } catch (SQLException e) {
        try {
            out.writeUTF("FAILURE");
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        e.printStackTrace();
    }
}



        private void handleUpdateProduct(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    try {
        // Read product details from the client
        int productId = in.readInt();
        String productName = in.readUTF();
        double price = in.readDouble();
        String description = in.readUTF();

        // SQL query to update the product
        String query = "UPDATE products SET name = ?, price = ?, ingredients = ? WHERE product_id = ?";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            // Set parameters for the prepared statement
            statement.setString(1, productName);
            statement.setDouble(2, price);
            statement.setString(3, description);
            statement.setInt(4, productId);

            // Execute the update query
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                out.writeUTF("SUCCESS");
            } else {
                out.writeUTF("FAILURE: Product not found or no changes made.");
            }
            out.flush();
        } catch (SQLException e) {
            out.writeUTF("FAILURE: Database error occurred.");
            out.flush();
            e.printStackTrace(); // Log error for debugging
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE: Error occurred while processing the update.");
        out.flush();
        e.printStackTrace(); // Log error for debugging
    }
}

    

     private void handleRemoveProduct(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    try {
        // Read the product ID from the client
        int productId = in.readInt();

        // Validate the product ID (optional, based on your application's logic)
        if (productId <= 0) {
            out.writeUTF("FAILURE: Invalid Product ID");
            return;
        }

        // SQL query to delete the product
        String query = "DELETE FROM products WHERE product_id = ?";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setInt(1, productId);
            int rowsAffected = statement.executeUpdate();

            // Check if the delete operation was successful
            if (rowsAffected > 0) {
                out.writeUTF("SUCCESS");
            } else {
                out.writeUTF("FAILURE: Product not found");
            }
        } catch (SQLException e) {
            out.writeUTF("FAILURE: Database error");
            e.printStackTrace(); // Log error for debugging
        }
    } catch (IOException e) {
        out.writeUTF("FAILURE: Input error");
        e.printStackTrace(); // Log error for debugging
    }
}

    }
}
