/*
Author: Roshan Phakami Punmagar
StudentId- 12201590
FileName-App.java
Date-07/06/2024
Purpose: This class serves as the entry point for the JavaFX application.
*/

package mdhsapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
// Static variable to hold the primary scene
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Load the initial FXML file and set it as the scene for the stage
        scene = new Scene(loadFXML("FXML"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
         // Load the new FXML file and set it as the root of the current scene
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
          // Create an FXMLLoader to load the specified FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
          // Launch the JavaFX application
        launch();
    }

}