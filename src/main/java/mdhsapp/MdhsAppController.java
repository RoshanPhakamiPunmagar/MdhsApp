/*
Author: Roshan Phakami Punmagar
StudentId: 12201590
FileName: MdhsAppController.java
Date: 07/06/2024
Purpose: This controller class handles the application's interaction logic, including user registration, login, placing orders, and managing products and delivery schedules.
*/

package mdhsapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UTFDataFormatException;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javax.crypto.Cipher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


 // MdhsAppController handles the application's logic for user interaction, such as registration, login, order placement, and management of products and delivery schedules.
 
public class MdhsAppController implements Initializable {
    
    // FXML fields for user interface components
    @FXML private TextField registerFullName;
    @FXML private TextField registerPhone;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private TextField registerAddress;
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private TextField orderCustomerId;
    @FXML private TextField orderTotalPrice;   
    @FXML private TextField addProductName;
    @FXML private TextField addProductPrice;
    @FXML private TextField addProductDescription;
    @FXML private TextField addProductQuantity;
    @FXML private TextField addProductUnit;
    @FXML private TextField  createDeliveryPostcode;
    @FXML private TextField  createDeliveryDay;
    @FXML private TextField  createDeliveryCost;
    @FXML private TextField updateProductId;
    @FXML private TextField updateProductName;
    @FXML private TextField updateProductPrice;
    @FXML private TextField updateProductDescription;
    @FXML private TextField removeProductId;
    @FXML private TextField status;
    @FXML private TextField pName;
    @FXML private TextArea ordersTextArea;
    @FXML private TextArea txtDisplayCustomer;
    @FXML private TextArea txtViewSchedule;
    @FXML private TextArea txtDisplayProducts;
    @FXML private TextField deliveryScheduleId;
    
    // Network-related fields
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PublicKey serverPublicKey;
   
  
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Establish a connection to the server when the application initializes
        connectToServer();
        
    }
    
 

    private void connectToServer() {
        try {
            // Create a socket connection to the server running on localhost at port 7777
            socket = new Socket("localhost", 7777);
            // Initialize ObjectOutputStream to send objects to the server
            out = new ObjectOutputStream(socket.getOutputStream());
            // Initialize ObjectInputStream to read objects sent by the server
            in = new ObjectInputStream(socket.getInputStream());
            // Retrieve the server's public key after establishing the connection
            retrieveServerPublicKey();
        } catch (IOException e) {
            // Show an error alert if the connection fails
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to connect to the server.");
            e.printStackTrace();
        }
    }

    private void retrieveServerPublicKey() throws IOException {
        try {
             // Read the length of the public key byte array sent by the server
            int length = in.readInt();
            
            // Allocate a byte array to store the public key bytes
            byte[] publicKeyBytes = new byte[length];
             // Read the public key bytes from the input stream
            in.readFully(publicKeyBytes);
            // Generate the public key from the bytes using RSA algorithm
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            serverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (GeneralSecurityException e) {
             // Handle any security-related exceptions during the key generation
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
             // Encrypt the password before sending it over the network
            byte[] encryptedPassword = encryptPassword(registerPassword.getText());
             // Send a command to the server indicating this is a registration request
            out.writeUTF("REGISTER");
            
            // Send user registration details to the server
            out.writeUTF(registerFullName.getText());
            out.writeUTF(registerPhone.getText());
            out.writeUTF(registerEmail.getText());
            out.writeInt(encryptedPassword.length);
            out.write(encryptedPassword);
            out.writeUTF(registerAddress.getText());
            out.flush();
            
             // Read the server's response
            String response = in.readUTF();
            if ("SUCCESS".equals(response)) {
                 // Show success message if the registration was successful
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You have registered successfully.");
            } else {
                // Show error message if the registration failed
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Failed to register.");
            }
        } catch (IOException | GeneralSecurityException e) {
             // Show error message if an exception occurs
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during registration.");
            e.printStackTrace();
            
        }
        
    }
  

    private byte[] encryptPassword(String password) throws GeneralSecurityException {
    // Create a Cipher instance for RSA encryption
    Cipher cipher = Cipher.getInstance("RSA");
    
    // Initialize the cipher in encryption mode with the server's public key
    cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
    
    // Encrypt the password and return the encrypted byte array
    return cipher.doFinal(password.getBytes());
}


@FXML
private void handleLogin(ActionEvent event) {
    new Thread(() -> {
        try {
            // Encrypt the user's login password
            byte[] encryptedPassword = encryptPassword(loginPassword.getText());
            
            // Send a LOGIN command to the server
            out.writeUTF("LOGIN");
            // Send the user's email address
            out.writeUTF(loginEmail.getText());
            // Send the length of the encrypted password
            out.writeInt(encryptedPassword.length);
            // Send the encrypted password itself
            out.write(encryptedPassword);
            // Ensure all data is sent to the server
            out.flush();

            // Read the server's response
            String response = in.readUTF();
            // Update the UI based on the server's response
            if ("SUCCESS".equals(response)) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Login successful."));
            } else {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials."));
            }
        } catch (IOException | GeneralSecurityException e) {
            // Handle any exceptions that occur during login
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during login.");
                e.printStackTrace();
            });
        }
    }).start();
}


    @FXML
private void handlePlaceOrder(ActionEvent event) {
    try {
          // Validate that required fields are not empty
        if (orderCustomerId.getText().isEmpty() || orderTotalPrice.getText().isEmpty()||  pName.getText().isEmpty() ||status.getText().isEmpty() ) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Customer ID and Total Price cannot be empty.");
            return;
        }
        
        // Variables to hold parsed input values
        int customerId;
        double totalPrice;
        String productName;
        String statusId;

        // Parse and validate numeric inputs
        try {
            customerId = Integer.parseInt(orderCustomerId.getText());
            totalPrice = Double.parseDouble(orderTotalPrice.getText());
            productName = pName.getText();
            statusId = status.getText();
            
        } catch (NumberFormatException e) {
            // Show an error alert if the input is not a valid number
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid numeric input for Customer ID or Total Price.");
            return;
        }

        // Send order details to the server
        out.writeUTF("PLACE_ORDER");
        out.writeInt(customerId);
        out.writeDouble(totalPrice);
        out.writeUTF(productName);
        out.writeUTF(statusId);
        out.flush();

        // Read server response
        String response = in.readUTF();
        if ("SUCCESS".equals(response)){
             // If successful, read the order ID returned by the server and show a success alert
            int orderId = in.readInt();
            showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Order Placed Successfully. Order ID: " + orderId);
        } else {
             // Show an error alert if the order placement faile
            showAlert(Alert.AlertType.ERROR, "Order Failed", "Order Placement Failed.");
        }
    } catch (IOException e) {
        // Handle any I/O exceptions that occur during the process
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during order placement.");
    }
}


private void showAlert(Alert.AlertType alertType, String message) {
    // Ensure that the UI update occurs on the JavaFX Application Thread
    Platform.runLater(() -> {
        // Create an alert dialog with the specified type (e.g., ERROR, INFORMATION)
        Alert alert = new Alert(alertType);
        
        // Set the content text of the alert dialog to the provided message
        alert.setContentText(message);
        
        // Display the alert dialog and wait for the user to acknowledge it
        alert.showAndWait();
    });
}


@FXML
private void handleDisplayProducts(ActionEvent event) {
    new Thread(() -> {
        try {
            // Send a request to the server to retrieve product information
            out.writeUTF("DISPLAY_PRODUCTS");
            out.flush();

            StringBuilder productData = new StringBuilder();
            String response;
            // Read product data from the server until the end signal is received
            while (!(response = in.readUTF()).equals("END_OF_PRODUCTS")) {
                if ("PRODUCT".equals(response)) {
                    // Extract product details from the server response
                    int productId = in.readInt();
                    String productName = in.readUTF();
                    int quantity = in.readInt();
                    String unit = in.readUTF();
                    double productPrice = in.readDouble();
                    String productDescription = in.readUTF();

                    // Append product details to the StringBuilder
                    productData.append("Product ID: ").append(productId).append("\n");
                    productData.append("Name: ").append(productName).append("\n");
                    productData.append("Quantity: ").append(quantity).append("\n");
                    productData.append("Unit: ").append(unit).append("\n");
                    productData.append("Price: ").append(String.format("%.2f", productPrice)).append("\n");
                    productData.append("Description: ").append(productDescription).append("\n\n");
                }
            }

            // Update the TextArea with the collected product data
            String finalProductData = productData.toString();
            Platform.runLater(() -> txtDisplayProducts.setText(finalProductData));

        } catch (UTFDataFormatException e) {
            // Handle the exception when strings exceed the maximum length
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "A string exceeded the maximum allowable length."));
            e.printStackTrace();
        } catch (IOException e) {
            // Handle IO exception
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while retrieving product information."));
            e.printStackTrace();
        }
    }).start();
}






@FXML
private void handleAddProduct(ActionEvent event) {
    try {
        // Retrieve input values from the user interface
        String name = addProductName.getText();
        int quantity = Integer.parseInt(addProductQuantity.getText()); // Correct field for quantity
        String unit = addProductUnit.getText();
        double price = Double.parseDouble(addProductPrice.getText());
        String ingredients = addProductDescription.getText();

        // Send the command to the server indicating that a product addition request is being made
        out.writeUTF("ADD_PRODUCT");

        // Send product details to the server
        out.writeUTF(name);            // Product name
        out.writeInt(quantity);        // Quantity
        out.writeUTF(unit);            // Unit of measurement
        out.writeDouble(price);        // Price
        out.writeUTF(ingredients);     // Ingredients or description

        // Ensure all data has been sent to the server
        out.flush();

        // Await server's response to confirm the operation
        String response = in.readUTF();
        if ("SUCCESS".equals(response)) {
            // Display success message to the user
            showAlert(Alert.AlertType.INFORMATION, "Product Added Successfully");
        } else {
            // Display error message if the product could not be added
            showAlert(Alert.AlertType.ERROR, "Failed to Add Product");
        }
    } catch (IOException e) {
        // Handle network or input/output errors
        showAlert(Alert.AlertType.ERROR, "Network error: Could not add product. Please try again.");
        e.printStackTrace();
    } catch (NumberFormatException e) {
        // Handle incorrect format inputs (e.g., non-numeric input for numeric fields)
        showAlert(Alert.AlertType.ERROR, "Invalid input format. Please check the fields and try again.");
        e.printStackTrace();
    }
}
@FXML
private void handleCreateDeliverySchedule(ActionEvent event) {
    try {
        int postcode = Integer.parseInt(createDeliveryPostcode.getText());
        String deliveryDay = createDeliveryDay.getText();       
        double deliveryCost = Double.parseDouble(createDeliveryCost.getText()); // Assuming this is your TextField

        // Assuming 'out' is your ObjectOutputStream
        out.writeUTF("CREATE_DELIVERY_SCHEDULE");
         out.writeInt(postcode);
        out.writeUTF(deliveryDay);
        
       // Sending the delivery cost as a double
        out.writeDouble(deliveryCost);  
        out.flush();

        String response = in.readUTF();
        if ("SUCCESS".equals(response)) {
            showAlert(Alert.AlertType.INFORMATION, "Delivery Schedule Created Successfully");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed to Create Delivery Schedule");
        }
    } catch (NumberFormatException e) {
        showAlert(Alert.AlertType.ERROR, "Please enter valid values for postcode and delivery cost.");
        e.printStackTrace();
    } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "An error occurred while communicating with the server.");
        e.printStackTrace();
    }
}



@FXML
private void handleViewDeliverySchedule(ActionEvent event) {
    new Thread(() -> {
        try {
            // Send a request to the server to retrieve delivery schedules for the given schedule ID
            int scheduleId = Integer.parseInt(deliveryScheduleId.getText()); 
            // Send request type
            out.writeUTF("VIEW_DELIVERY_SCHEDULE"); 
             // Send schedule ID
            out.writeInt(scheduleId);
            out.flush();

            // Receive response from the server
            String response = in.readUTF();
            if ("SUCCESS".equals(response)) {
                // Read delivery schedules from the server and populate the TextArea
                StringBuilder scheduleText = new StringBuilder();
                boolean moreDataAvailable = true;
                while (moreDataAvailable) {
                    int postcode = in.readInt();
                    String deliveryDay = in.readUTF();
                    double deliveryCost = in.readDouble();
                    scheduleText.append("Postcode: ").append(postcode).append("\n");
                    scheduleText.append("Delivery Day: ").append(deliveryDay).append("\n");
                    scheduleText.append("Delivery Cost: ").append(deliveryCost).append("\n\n");
                    moreDataAvailable = in.readBoolean(); // Check if more data is available
                }
                Platform.runLater(() -> txtViewSchedule.setText(scheduleText.toString()));
            } else if ("NO_SCHEDULE".equals(response)) {
                Platform.runLater(() -> txtViewSchedule.setText("No delivery schedules found for this schedule ID."));
            } else {
                Platform.runLater(() -> txtViewSchedule.setText("Error occurred while retrieving delivery schedules from the server."));
            }
        } catch (NumberFormatException e) {
            Platform.runLater(() -> txtViewSchedule.setText("Invalid schedule ID. Please enter a valid number."));
        } catch (IOException e) {
            Platform.runLater(() -> txtViewSchedule.setText("Error occurred while communicating with the server."));
            e.printStackTrace();
        }
    }).start();
}


@FXML
private void handleViewCustomers(ActionEvent event) {
    new Thread(() -> {
        try {
            // Send request to the server to view customers
            out.writeUTF("VIEW_CUSTOMERS");
            out.flush();

            // Clear the TextArea before displaying new data
            Platform.runLater(() -> txtDisplayCustomer.clear());

            // Receive and display customer information
            String response;
            while (!(response = in.readUTF()).equals("END_OF_CUSTOMERS")) {
                if ("CUSTOMER".equals(response)) {
                    int customerId = in.readInt();
                    String fullName = in.readUTF();
                    String phone = in.readUTF();
                    String email = in.readUTF();
                    String address = in.readUTF();

                    // Append customer details to the TextArea
                    String customerInfo = String.format("Customer ID: %d\nFull Name: %s\nPhone: %s\nEmail: %s\nAddress: %s\n\n",
                            customerId, fullName, phone, email, address);

                    // Update the TextArea on the JavaFX Application Thread
                    Platform.runLater(() -> txtDisplayCustomer.appendText(customerInfo));
                }
            }
        } catch (IOException e) {
            // Handle IO exception
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while retrieving customer information."));
        }
    }).start();
}




@FXML
private void handleViewOrders(ActionEvent event) {
    new Thread(() -> {
        try {
            // Send request to the server to view orders
            out.writeUTF("VIEW_ORDERS");
            out.flush();

            StringBuilder orders = new StringBuilder();
            String response;
           
            // Receive and display order information
            while (!(response = in.readUTF()).equals("END_OF_ORDERS")) {
                if ("ORDER".equals(response)) {
                    int orderId = in.readInt();
                    int customerId = in.readInt();
                    String productName = in.readUTF();
                    String stats = in.readUTF();
                    
 
                    // Append order details to the StringBuilder
                    orders.append("Order ID: ").append(orderId)
                         .append(", Customer ID: ").append(customerId)
 
                          .append(", Product Name: ").append(productName)
                          .append(", Status: ").append(stats)
                          .append("\n");
                }
            }

            // Update the TextArea with the collected order data
            String finalOrdersData = orders.toString();
            ordersTextArea.setText(finalOrdersData);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while retrieving orders."));
        }
    }).start();
}





@FXML
private void handleUpdateProduct() {
    String productIdText = updateProductId.getText();
    String productName = updateProductName.getText();
    String productPriceText = updateProductPrice.getText();
    String productDescription = updateProductDescription.getText();

    try {
        // Input validation
        if (productIdText.isEmpty() || productName.isEmpty() || productPriceText.isEmpty() || productDescription.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "All fields must be filled out.");
            return;
        }

        int productId = Integer.parseInt(productIdText);
        double productPrice = Double.parseDouble(productPriceText);

        // Sending data to the server
        out.writeUTF("UPDATE_PRODUCT");
        out.writeInt(productId);
        out.writeUTF(productName);
        out.writeDouble(productPrice);
        out.writeUTF(productDescription);
        out.flush();

        // Receiving response from the server
        String response = in.readUTF();
        if ("SUCCESS".equals(response)) {
            showAlert(Alert.AlertType.INFORMATION, "Product updated successfully.");
        } else {
            // Display detailed error message from the server
            showAlert(Alert.AlertType.ERROR, response); 
        }
    } catch (NumberFormatException e) {
        showAlert(Alert.AlertType.ERROR, "Invalid input: Please enter valid numbers for ID and price.");
        e.printStackTrace();
    } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "Network error: Unable to communicate with the server.");
        e.printStackTrace();
    }
}

@FXML
    private void handleRemoveProduct(ActionEvent event) {
        try {
            // Validate input
            String productIdText = removeProductId.getText();
            if (productIdText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Product ID must not be empty.");
                return;
            }

            int productId = Integer.parseInt(productIdText);

            // Send remove product request to server
            out.writeUTF("REMOVE_PRODUCT");
            out.writeInt(productId);
            out.flush();

            // Read response from server
            String response = in.readUTF();
            if ("SUCCESS".equals(response)) {
                showAlert(Alert.AlertType.INFORMATION, "Product Removed Successfully");
            } else {
                // Show detailed error message from the server
                showAlert(Alert.AlertType.ERROR, response); 
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Product ID: Please enter a valid number.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Network error: Unable to communicate with the server.");
            e.printStackTrace();
        }
    }
    
       private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


