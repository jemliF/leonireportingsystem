/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leoni.mfa.reporting.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author fathi jemli
 */
public class Leoni_repoting1 extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Workflow.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Leoni Reporting System");
         stage.setFullScreen(true);   
        stage.setHeight(1050);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
    }

}
