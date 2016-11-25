/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leoni.mfa.reporting.view;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 *
 * @author fathi jemli
 */
public class Leoni_repoting extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Overview.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Leoni Reporting System");
        //stage.setFullScreen(true);   
        stage.setMaximized(true);
        stage.setHeight(1050);
        stage.show();
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (t.isControlDown() && t.getCode() == KeyCode.F) {
                    stage.setFullScreen(true);
                }
            }
        });
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
    }

}
