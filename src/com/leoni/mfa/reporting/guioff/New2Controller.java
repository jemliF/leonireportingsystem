/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leoni.mfa.reporting.guioff;

import com.leoni.mfa.reporting.db.MySQLConnexion;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author fathi jemli
 */
public class New2Controller implements Initializable {

    @FXML
    private TableView TableView;
    private ObservableList<ObservableList> data;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TableView = new TableView();
        loadData();
    }

    public void loadData() {
        ResultSet resultSet = new MySQLConnexion().selectQuery("SELECT * FROM PRODUIT");
        //let's add resultset columns to the tableview
        /*for (int i = 0; i < resultSet.getMetaData().getColumnCount()) {
            
         }*/
    }

}
