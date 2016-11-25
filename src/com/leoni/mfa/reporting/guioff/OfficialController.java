/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leoni.mfa.reporting.guioff;

import com.leoni.mfa.reporting.db.MSSQLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author fathi jemli
 */
public class OfficialController implements Initializable {

    private static Logger logger = org.apache.log4j.LogManager.getRootLogger();
    ObservableList<ObservableList> data;
    Connection connection;

    @FXML
    private TableView tableView;

    @FXML
    private LineChart lineChart;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadData();
        System.out.println("table view: " + tableView.getItems());

    }

    public void loadData() {
        try {
            connection = new MSSQLConnection().connecter();
            data = FXCollections.observableArrayList();
            String query = "SELECT [Line],[KSKCountMax] FROM [REP_MFA].[LEP_SYS].[KSKStartCountMax]";

            PreparedStatement prepareStatement = connection.prepareStatement(query);
            ResultSet resultSet = prepareStatement.executeQuery();

            //adding columns to tableview
            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(resultSet.getMetaData().getColumnName(i + 1));

                col.setCellValueFactory(
                        new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {

                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {

                                return new SimpleStringProperty(param.getValue().get(j).toString());

                            }

                        });

                tableView.getColumns().addAll(col);

            }

            //adding data rows
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    row.add(resultSet.getObject(i) + "");
                }
                //System.out.println("row:   " + row);
                data.add(row);
            }
            System.out.println("data:   " + data);
            //tableView.setItems(data);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
