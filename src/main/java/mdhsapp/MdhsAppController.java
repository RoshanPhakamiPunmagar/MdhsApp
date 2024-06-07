package mdhsapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
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


public class MdhsAppController implements Initializable {
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
    
    
    
    //initalizings
    private TextField deliveryCustomerId;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PublicKey serverPublicKey;
    @FXML
    private TextArea ordersTextArea;
    @FXML
    private TextArea txtDisplayCustomer;
    @FXML
    private TextArea txtViewSchedule;
    @FXML
    private TextArea txtDisplayProducts;
    @FXML
    private TextField deliveryScheduleId;
  
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connectToServer();
      
        
    }
    
 

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 7777);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            retrieveServerPublicKey();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to connect to the server.");
            e.printStackTrace();
        }
    }

    private void retrieveServerPublicKey() throws IOException {
        try {
            int length = in.readInt();
            byte[] publicKeyBytes = new byte[length];
            in.readFully(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            serverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            byte[] encryptedPassword = encryptPassword(registerPassword.getText());
            out.writeUTF("REGISTER");
            out.writeUTF(registerFullName.getText());
            out.writeUTF(registerPhone.getText());
            out.writeUTF(registerEmail.getText());
            out.writeInt(encryptedPassword.length);
            out.write(encryptedPassword);
            out.writeUTF(registerAddress.getText());
            out.flush();

            String response = in.readUTF();
            if ("SUCCESS".equals(response)) {
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You have registered successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Failed to register.");
            }
        } catch (IOException | GeneralSecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during registration.");
            e.printStackTrace();
            
        }
        
    }
  

    private byte[] encryptPassword(String password) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        return cipher.doFinal(password.getBytes());
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            byte[] encryptedPassword = encryptPassword(loginPassword.getText());
            out.writeUTF("LOGIN");
            out.writeUTF(loginEmail.getText());
            out.writeInt(encryptedPassword.length);
            out.write(encryptedPassword);
            out.flush();

            String response = in.readUTF();
            if ("SUCCESS".equals(response)) {
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Login successful.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials.");
            }
        } catch (IOException | GeneralSecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during login.");
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
    

    @FXML
private void handlePlaceOrder(ActionEvent event) {
    try {
        // Validate inputs
        if (orderCustomerId.getText().isEmpty() || orderTotalPrice.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Customer ID and Total Price cannot be empty.");
            return;
        }

        int customerId;
        double totalPrice;

        // Parse and validate numeric inputs
        try {
            customerId = Integer.parseInt(orderCustomerId.getText());
            totalPrice = Double.parseDouble(orderTotalPrice.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid numeric input for Customer ID or Total Price.");
            return;
        }

        // Send order details to the server
        out.writeUTF("PLACE_ORDER");
        out.writeInt(customerId);
        out.writeDouble(totalPrice);
        out.flush();

        // Read server response
        String response = in.readUTF();
        if ("SUCCESS".equals(response)) {
            int orderId = in.readInt();
            showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Order Placed Successfully. Order ID: " + orderId);
        } else {
            showAlert(Alert.AlertType.ERROR, "Order Failed", "Order Placement Failed.");
        }
    } catch (IOException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during order placement.");
    }
}




private void showAlert(Alert.AlertType alertType, String message) {
    Platform.runLater(() -> {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
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
            response = in.readUTF();
            // Read product data from the server until the end signal is received
            while (!response.equals("END_OF_PRODUCTS")) {
                if ((response).equals("PRODUCTS")) {
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
            txtDisplayProducts.setText(finalProductData);

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
        String deliveryDay = createDeliveryDay.getText();
        int postcode = Integer.parseInt(createDeliveryPostcode.getText());
        double deliveryCost = Double.parseDouble(createDeliveryCost.getText()); // Assuming this is your TextField

        // Assuming 'out' is your ObjectOutputStream
        out.writeUTF("CREATE_DELIVERY_SCHEDULE");
        out.writeUTF(deliveryDay);
        out.writeInt(postcode);
        out.writeDouble(deliveryCost);  // Sending the delivery cost as a double
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
    try {
        // Send a request to the server to retrieve delivery schedules for the given schedule ID
        int scheduleId = Integer.parseInt(deliveryScheduleId.getText()); // Assuming you have a TextField named deliveryScheduleId
        out.writeUTF("VIEW_DELIVERY_SCHEDULE"); // Send request type
        out.writeInt(scheduleId); // Send schedule ID
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
            txtViewSchedule.setText(scheduleText.toString());
        } else if ("NO_SCHEDULE".equals(response)) {
            txtViewSchedule.setText("No delivery schedules found for this schedule ID.");
        } else {
            txtViewSchedule.setText("Error occurred while retrieving delivery schedules from the server.");
        }
    } catch (NumberFormatException e) {
        txtViewSchedule.setText("Invalid schedule ID. Please enter a valid number.");
    } catch (IOException e) {
        txtViewSchedule.setText("Error occurred while communicating with the server.");
        e.printStackTrace();
    }
}


@FXML
private void handleViewCustomers(ActionEvent event) {
    try {
        out.writeUTF("VIEW_CUSTOMERS");
        //out.flush();

        String response;
        response = in.readUTF();
        txtDisplayCustomer.clear();  // Clear the TextArea before displaying new data
        while (!response.equals("END_OF_CUSTOMERS")) {
            if ("CUSTOMER".equals(response)) {
                int customerId = in.readInt();
                String fullName = in.readUTF();
                String phone = in.readUTF();
                String email = in.readUTF();
                String address = in.readUTF();

                // Append customer details to the TextArea
                String customerInfo = String.format("Customer ID: %d\nFull Name: %s\nPhone: %s\nEmail: %s\nAddress: %s\n\n",
                    customerId, fullName, phone, email, address);
                txtDisplayCustomer.appendText(customerInfo);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while retrieving customer information."));
    }
}


@FXML
private void handleViewOrders() {
    new Thread(() -> {
        try {
            out.writeUTF("VIEW_ORDERS");
           // out.flush();

            String response;
            response = in.readUTF();
            StringBuilder orders = new StringBuilder();
            while (!response.equals("END_OF_ORDERS")) {
                if ("ORDER".equals(response = in.readUTF())) {
                    int orderId = in.readInt();
                    int customerId = in.readInt();
                    double totalPrice = in.readDouble();
                    String orderDate = in.readUTF();
                    orders.append("Order ID: ").append(orderId)
                          .append(", Customer ID: ").append(customerId)
                          .append(", Total Price: ").append(totalPrice)
                          .append(", Order Date: ").append(orderDate)
                          .append("\n");
                }
            }
            String ordersText = orders.toString();

            // Ensure UI updates happen on the JavaFX application thread
            ordersTextArea.setText(ordersText);
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
            showAlert(Alert.AlertType.ERROR, response); // Display detailed error message from the server
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
                showAlert(Alert.AlertType.ERROR, response); // Show detailed error message from the server
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Product ID: Please enter a valid number.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Network error: Unable to communicate with the server.");
            e.printStackTrace();
        }
    }
}


