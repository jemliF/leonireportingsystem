/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author bewa1022
 */
public class TTEs implements Initializable {

    @FXML
    private TableView<Test> tableReport;

    @FXML
    private TableColumn<Test, String> name;

    @FXML
    private TableColumn<Test, Boolean> checkbox;

    @FXML
    public void getValues() {
    //the method will get what check boxes are checked (this part is the problem)
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        ObservableList<Test> data = FXCollections.observableArrayList();
        data.add(new Test("Test 1", true));
        data.add(new Test("Test 2", false));

        tableReport.setItems(data);

        name.setCellValueFactory(new PropertyValueFactory<Test, String>("name"));
        checkbox.setCellValueFactory(new PropertyValueFactory<Test, Boolean>("checked"));

        checkbox.setCellFactory(new Callback<TableColumn<Test, Boolean>, TableCell<Test, Boolean>>() {

            public TableCell<Test, Boolean> call(TableColumn<Test, Boolean> p) {
                return new CheckBoxTableCell<Test, Boolean>();
            }
        });

    }

//CheckBoxTableCell for creating a CheckBox in a table cell
    public static class CheckBoxTableCell<S, T> extends TableCell<S, T> {

        private final CheckBox checkBox;
        private ObservableValue<T> ov;

        public CheckBoxTableCell() {
            this.checkBox = new CheckBox();
            this.checkBox.setAlignment(Pos.CENTER);

            setAlignment(Pos.CENTER);
            setGraphic(checkBox);
        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(checkBox);
                if (ov instanceof BooleanProperty) {
                    checkBox.selectedProperty().unbindBidirectional((BooleanProperty) ov);
                }
                ov = getTableColumn().getCellObservableValue(getIndex());
                if (ov instanceof BooleanProperty) {
                    checkBox.selectedProperty().bindBidirectional((BooleanProperty) ov);
                }
            }
        }
    }
    public static void main(String[] args) {
        
    }
}
