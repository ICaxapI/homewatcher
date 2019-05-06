package ru.exsoft.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;

public class Gui extends Application {

    public static void startGui(String... args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("HomeWatcher");
        primaryStage.setResizable(false);
        FXMLLoader loader = new FXMLLoader();
        AnchorPane root = loader.load(new FileInputStream("general.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest((we) -> System.out.println("CLOSE")); //TODO add code to close application
    }
}
