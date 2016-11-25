package com.leoni.mfa.reporting.view;

import com.leoni.mfa.reporting.config.ConfigReader;
import com.leoni.mfa.reporting.db.MSSQLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.SegmentedButton;

/**
 * FXML Controller class
 *
 * @author bewa1022
 */
public class OverviewController implements Initializable {

    private CheckComboBox<String> processes;
    private ToggleButton tglBtnInr;
    private ToggleButton tglBtnMra;
    private SegmentedButton sgmBtnsSegments;
    private MSSQLConnection connection;
    private static final Logger logger = org.apache.log4j.LogManager.getRootLogger();
    private String selectedSegment = "MRA";

    private ObservableList<TableColumn> tableViewDynColumns = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> tableViewDynData = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> tableViewDynInitialData = FXCollections.observableArrayList();
    private ObservableList<Object> tableViewDynRow;
    private ObservableList<Object> tableViewDynTotalRow;
    private ObservableList<String> selectedProcesses;
    private ObservableList<Object> row;
    private ObservableList<Object> totalRow;

    private ObservableList<TableColumn> tableViewStatColumns = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> tableViewStatData = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> tableViewStatInitialData = FXCollections.observableArrayList();
    private ObservableList<Object> tableViewStatRow;
    private ObservableList<Object> tableViewStatTotalRow;

    private ObservableList<TableColumn> tableViewPerHourColumns = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> tableViewPerHourData = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> tableViewPerHourInitialData = FXCollections.observableArrayList();
    private ObservableList<Object> tableViewPerHourRow;
    private ObservableList<Object> tableViewPerHourRowTotal;
    private ObservableList<Object> tableViewPerHourTotalRow;
    private XYChart.Series[] perHourbarSeries;
    private ObservableList<com.leoni.mfa.reporting.config.Process> perHourProcesses;
    private ObservableList<com.leoni.mfa.reporting.config.Process> perHourTotalProcesses;

    private ObservableList<PieChart.Data> pieChartData;
    private Stage window;
    private Parent root;
    private ObservableList<String> routeStepsForCalculation = FXCollections.observableArrayList();
    private ObservableList<String> processesForCalculation = FXCollections.observableArrayList();

    private ObservableList<String> inrRouteStepsForCalculation = FXCollections.observableArrayList();
    private ObservableList<String> mraRouteStepsForCalculation = FXCollections.observableArrayList();
    private int startHour, startMinute, endHour, endMinute;
    private ObservableList<ObservableList<Object>> tableViewPerHourRowTotals;
    private XYChart.Series[] stackedStaticBarChartSeries;

    @FXML
    private TableView tableViewDyn;
    @FXML
    private TableView tableViewStat;
    @FXML
    private TableView tableViewPerHour;
    @FXML
    private BarChart barChartDyn;
    @FXML
    private BarChart barChartPerHour;

    @FXML
    private PieChart pieChartDyn;
    @FXML
    private StackedBarChart stackedBarChartStat;

    /*@FXML
     private LocalDateTextField startDate;
     @FXML
     private LocalDateTextField endDate;
    
     @FXML
     private LocalTimePicker startTime;
     @FXML
     private LocalTimePicker endTime;*/
    @FXML
    private HBox segmentsHBox;
    @FXML
    private HBox shiftsHBox;
    @FXML
    private HBox processesHBox;

    @FXML
    Button btnShowReportWorkflow;
    @FXML
    private VBox mainContainer;
    @FXML
    private VBox reportContainer;
    @FXML
    private HBox middleChartsContainer;
    @FXML
    private AnchorPane barChartContainer;
    @FXML
    private Label pieChartContainerTitle;
    @FXML
    private Label barChartContainerTitle;
    @FXML
    private ComboBox<String> shifts;
    @FXML
    private CheckBox checkreste;
    @FXML
    private CheckBox checkresteMontage;
    @FXML
    private CheckBox checkKSKToday;
    @FXML
    private Button btnETE;
    @FXML
    private Button btnWorkflow;
    @FXML
    private VBox staticContainer;
    @FXML
    private VBox dynamicContainer;
    @FXML
    private VBox perHourContainer;
    @FXML
    private ImageView imgLoader;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                imgLoader.setVisible(false);
                fillProcessesComboBox();
                fillSegments();
                fillShifts();

                checkreste.selectedProperty().addListener(new ChangeListener<Boolean>() {

                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (checkreste.isSelected()) {
                            dynamicContainer.setDisable(false);

                        } else {
                            dynamicContainer.setDisable(true);
                        }
                    }
                });
                checkresteMontage.selectedProperty().addListener(new ChangeListener<Boolean>() {

                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (checkresteMontage.isSelected()) {
                            staticContainer.setDisable(false);

                        } else {
                            staticContainer.setDisable(true);
                        }
                    }
                });
                checkKSKToday.selectedProperty().addListener(new ChangeListener<Boolean>() {

                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (checkKSKToday.isSelected()) {
                            perHourContainer.setDisable(false);

                        } else {
                            perHourContainer.setDisable(true);
                        }
                    }
                });

                tableViewDyn.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
                    if (process.getSegment().equalsIgnoreCase(selectedSegment) && getRouteStepsForCalculation().contains(Integer.parseInt(process.getRouteStep()))) {
                        routeStepsForCalculation.add("" + Integer.parseInt(process.getRouteStep()));
                        processesForCalculation.add(process.getOperation() + " (" + Integer.parseInt(process.getRouteStep()) + ") ");
                    }
                }

                tableViewDyn.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        barChartDyn.getData().setAll();
                        ObservableList<Object> selectedLine = FXCollections.observableArrayList();
                        selectedLine = (ObservableList<Object>) tableViewDyn.getSelectionModel().getSelectedItems().get(0);
                        XYChart.Series barSeries = new XYChart.Series();
                        for (int i = 0; i < selectedProcesses.size(); i++) {
                            barSeries.setName(selectedProcesses.get(i));
                            barSeries.getData().add(new XYChart.Data<String, Integer>(selectedProcesses.get(i), Integer.parseInt("" + selectedLine.get(i + 1))));
                        }
                        barChartDyn.getData().add(barSeries);
                    }
                });

                tableViewDyn.getSortOrder().addListener(new ListChangeListener() {

                    @Override
                    public void onChanged(ListChangeListener.Change c) {
                        System.out.println("sorted column: " + ((TableColumn) tableViewDyn.getSortOrder().get(0)).getText());
                        if (!((TableColumn) tableViewDyn.getSortOrder().get(0)).getText().equalsIgnoreCase("Line")) {
                            pieChartData = FXCollections.observableArrayList();
                            int selectedColPosition = tableViewDyn.getColumns().indexOf(tableViewDyn.getSortOrder().get(0));
                            for (int i = 0; i < tableViewDynInitialData.size() - 1; i++) {
                                pieChartData.add(new PieChart.Data((String) tableViewDynInitialData.get(i).get(0), Double.parseDouble("" + tableViewDynInitialData.get(i).get(selectedColPosition))));
                            }
                        } /*else {
                         pieChartData = FXCollections.observableArrayList();
                         }*/

                        pieChartDyn.setData(pieChartData);
                    }
                });

                tableViewDyn.sortPolicyProperty().set((Callback<TableView<ObservableList<ObservableList>>, Boolean>) (TableView<ObservableList<ObservableList>> param) -> {//tableview sorting policy
                    Comparator<ObservableList> comparator = new Comparator<ObservableList>() {
                        @Override
                        public int compare(ObservableList r1, ObservableList r2) {
                            if (r1 == tableViewDynTotalRow) {
                                return 1;
                            } else if (r2 == tableViewDynTotalRow) {
                                return -1;
                            } else if (param.getComparator() == null) {
                                return 0;
                            } else {
                                return param.getComparator()
                                        .compare(r1, r2);
                            }
                        }
                    };
                    FXCollections.sort(tableViewDyn.getItems(), comparator);
                    return true;
                });

                tableViewPerHour.sortPolicyProperty().set((Callback<TableView<ObservableList<ObservableList>>, Boolean>) (TableView<ObservableList<ObservableList>> param) -> {//tableview sorting policy
                    Comparator<ObservableList> comparator = new Comparator<ObservableList>() {
                        @Override
                        public int compare(ObservableList r1, ObservableList r2) {
                            if (tableViewPerHourRowTotals.contains(r1)) {
                                System.out.println("r1: " + r1);
                                return 1;
                            } else if (tableViewPerHourRowTotals.contains(r2)) {
                                System.out.println("r2: " + r2);
                                return -1;
                            } else if (param.getComparator() == null) {
                                return 0;
                            } else {
                                return param.getComparator()
                                        .compare(r1, r2);
                            }
                        }
                    };
                    FXCollections.sort(tableViewPerHour.getItems(), comparator);
                    return true;
                });
                tableViewPerHour.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        barChartPerHour.getData().setAll();
                        ObservableList<Object> selectedLine = FXCollections.observableArrayList();
                        selectedLine = (ObservableList<Object>) tableViewPerHour.getSelectionModel().getSelectedItems().get(0);
                        XYChart.Series barSeries = new XYChart.Series();
                        for (int i = 2; i < tableViewPerHourColumns.size() - 1; i++) {
                            barSeries.setName(selectedLine.get(0) + " - " + selectedLine.get(1));
                            barSeries.getData().add(new XYChart.Data<String, Integer>(tableViewPerHourColumns.get(i).getText(), Integer.parseInt("" + selectedLine.get(i))));
                        }
                        barChartPerHour.getData().add(barSeries);
                    }
                });

                tableViewPerHour.getSortOrder().addListener(new ListChangeListener() {

                    @Override
                    public void onChanged(ListChangeListener.Change c) {
                        if (((TableColumn) tableViewPerHour.getSortOrder().get(0)).getText().equalsIgnoreCase("Total")) {
                            barChartPerHour.getData().setAll();
                            perHourbarSeries = new XYChart.Series[perHourProcesses.size()];

                            for (int g = 0; g < perHourProcesses.size(); g++) {
                                perHourbarSeries[g] = new XYChart.Series();
                                perHourbarSeries[g].setName(perHourProcesses.get(g).getOperation());
                            }
                            for (int i = 0; i < lines(selectedSegment).size(); i++) {
                                String brw = lines(selectedSegment).get(i);
                                System.out.println("brw:  " + brw);
                                for (int j = 0; j < perHourProcesses.size(); j++) {
                                    perHourbarSeries[j].getData().add(new XYChart.Data<String, Integer>(brw, Integer.parseInt("" + tableViewPerHourInitialData.get((3 * i) + j).get(10))));
                                }
                            }
                            for (int g = 0; g < perHourbarSeries.length; g++) {
                                barChartPerHour.getData().add(perHourbarSeries[g]);
                                System.out.println("perHourbarSeries[g]: " + perHourbarSeries[g]);
                            }
                        }
                    }
                });
            }
        });
    }

    public void showReportOverview() {

        if (checkreste.isSelected()) {
            showDynamicPart();
        }
        if (checkresteMontage.isSelected()) {
            showStaticPart();
        }
        if (checkKSKToday.isSelected()) {
            showPerHourPart();
        }
        imgLoader.setVisible(false);
        imgLoader.getScene().setCursor(Cursor.DEFAULT);
    }

    public void btnShowReportMousePressed() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                imgLoader.setVisible(true);
                imgLoader.getScene().setCursor(Cursor.WAIT);
            }

        });
    }

    public void showPerHourPart() {
        tableViewPerHourColumns = FXCollections.observableArrayList();
        tableViewPerHourData = FXCollections.observableArrayList();
        tableViewPerHour.getColumns().setAll(tableViewPerHourColumns);
        tableViewPerHour.setItems(tableViewPerHourData);
        barChartPerHour.getData().setAll();

        List<String> lines = lines(selectedSegment);

        TableColumn colLine = new TableColumn("Line");
        colLine.setPrefWidth(80);
        colLine.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(0).toString());
                    }
                });
        tableViewPerHourColumns.add(colLine);

        TableColumn colOperation = new TableColumn("Operation");
        colOperation.setPrefWidth(100);
        colOperation.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(1).toString());
                    }
                });
        tableViewPerHourColumns.add(colOperation);

        for (int i = 1; i <= 8; i++) {
            int j = i + 1;
            TableColumn col = new TableColumn("H" + (i));
            col.setPrefWidth((tableViewPerHour.getWidth() - 260) / 8);
            col.setCellValueFactory(
                    new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                        public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                            ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(j).toString()));
                            return obsInt;
                        }
                    });
            tableViewPerHourColumns.add(col);
        }
        TableColumn colTotal = new TableColumn("Total");
        colTotal.setPrefWidth(90);
        colTotal.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                    public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {

                        ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(10).toString()));
                        return obsInt;
                    }
                });
        tableViewPerHourColumns.add(colTotal);

        if (tableViewPerHourColumns.size() > 0) {
            tableViewPerHour.getColumns().addAll(tableViewPerHourColumns);
        }

        perHourProcesses = FXCollections.observableArrayList();
        perHourTotalProcesses = FXCollections.observableArrayList();
        for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
            if (process.isISH() && process.getSegment().equalsIgnoreCase(selectedSegment)) {
                perHourProcesses.add(process);
            }
            if (process.isITSH() && process.getSegment().equalsIgnoreCase(selectedSegment)) {
                perHourTotalProcesses.add(process);
            }
        }

        perHourbarSeries = new XYChart.Series[perHourProcesses.size()];
        for (int g = 0; g < perHourProcesses.size(); g++) {
            perHourbarSeries[g] = new XYChart.Series();
            perHourbarSeries[g].setName(perHourProcesses.get(g).getOperation());
        }

        for (int l = 0; l < lines.size(); l++) {
            String brw = lines.get(l);
            int totalLine = 0;

            for (int p = 0; p < perHourProcesses.size(); p++) {
                Date startDate = new Date();
                startDate.setHours(startHour);
                startDate.setMinutes(startMinute);
                String startDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(startDate);
                Date endDate = new Date();
                endDate.setHours(startDate.getHours() + 1);
                endDate.setMinutes(startMinute);
                String endDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(endDate);

                totalLine = 0;
                tableViewPerHourRow = FXCollections.observableArrayList();
                tableViewPerHourRow.add(brw);
                tableViewPerHourRow.add(perHourProcesses.get(p).getOperation() + " (" + perHourProcesses.get(p).getRouteStep() + ")");
                for (int h = 0; h < 8; h++) {
                    if (perHourProcesses.get(p).getOperation().contains("REW")) {
                        tableViewPerHourRow.add(kskREWQteByStartworkplace(brw, startDateStr, endDateStr));
                        totalLine += kskREWQteByStartworkplace(brw, startDateStr, endDateStr);
                    } else {
                        tableViewPerHourRow.add(kskQteByRoutestepAndStartworkplace(Integer.parseInt(perHourProcesses.get(p).getRouteStep()), brw, startDateStr, endDateStr));
                        totalLine += kskQteByRoutestepAndStartworkplace(Integer.parseInt(perHourProcesses.get(p).getRouteStep()), brw, startDateStr, endDateStr);
                    }
                    startDate.setHours(startDate.getHours() + 1);
                    startDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(startDate);
                    endDate.setHours(endDate.getHours() + 1);
                    endDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(endDate);
                    if (h == 6) {
                        endDate.setHours(endHour);
                        endDate.setMinutes(endMinute);
                        endDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(endDate);
                    }
                }
                tableViewPerHourRow.add(totalLine);
                perHourbarSeries[p].getData().add(new XYChart.Data<String, Integer>(brw, totalLine));
                tableViewPerHourData.add(tableViewPerHourRow);
            }
        }
        tableViewPerHourRowTotals = FXCollections.observableArrayList();//total rows

        for (int n = 0; n < perHourTotalProcesses.size(); n++) {
            tableViewPerHourRow = FXCollections.observableArrayList();//total rows
            tableViewPerHourRowTotal = FXCollections.observableArrayList();
            int totalLine = 0;
            ObservableList<Object> tableViewPerHourRowTotal = FXCollections.observableArrayList();
            tableViewPerHourRowTotal.add("Total");
            tableViewPerHourRowTotal.add(perHourTotalProcesses.get(n).getOperation() + " (" + perHourTotalProcesses.get(n).getRouteStep() + ")");

            Date startDate = new Date();
            startDate.setHours(startHour);
            startDate.setMinutes(startMinute);
            String startDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(startDate);

            Date endDate = new Date();
            endDate.setHours(startDate.getHours() + 1);
            endDate.setMinutes(startMinute);
            String endDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(endDate);

            for (int h = 0; h < 8; h++) {
                if (perHourTotalProcesses.get(n).getOperation().contains("REW")) {
                    tableViewPerHourRowTotal.add(kskREWQte(startDateStr, endDateStr));
                    totalLine += kskREWQte(startDateStr, endDateStr);
                } else {
                    tableViewPerHourRowTotal.add(kskQteByRoutestep(Integer.parseInt(perHourTotalProcesses.get(n).getRouteStep()), startDateStr, endDateStr));
                    totalLine += kskQteByRoutestep(Integer.parseInt(perHourTotalProcesses.get(n).getRouteStep()), startDateStr, endDateStr);
                }
                startDate.setHours(startDate.getHours() + 1);
                startDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(startDate);
                endDate.setHours(endDate.getHours() + 1);
                endDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(endDate);
                if (h == 6) {
                    endDate.setHours(endHour);
                    endDate.setMinutes(endMinute);
                    endDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(endDate);
                }
            }
            tableViewPerHourRowTotal.add(totalLine);
            tableViewPerHourRowTotals.add(tableViewPerHourRowTotal);
            tableViewPerHourData.add(tableViewPerHourRowTotal);
        }

        //System.out.println("table per hour data: " + tableViewPerHourData);
        if (tableViewPerHourData.size() > 0) {
            try {
                for (int g = 0; g < perHourbarSeries.length; g++) {
                    barChartPerHour.getData().add(perHourbarSeries[g]);
                }
                /*barChartPerHour.getData().setAll();
                 for (int g = 0; g < perHourbarSeries.length; g++) {
                 barChartPerHour.getData().add(perHourbarSeries[g]);
                 }*/
                tableViewPerHour.setItems(tableViewPerHourData);
                tableViewPerHourInitialData = FXCollections.observableArrayList(tableViewPerHourData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public int kskREWQte(String startDateTime, String completeDateTime) {
        String route;
        if (selectedSegment.equalsIgnoreCase("MRA")) {
            route = "LEP_ROUTE02";
        } else {
            route = "LEP_ROUTE01";
        }
        String query = "  SELECT sum (kskcount) "
                + " FROM (Select count(distinct ([REP_MFA].[LEP_HIS].[WorkTimes].KSK)) as kskcount "
                + " FROM [REP_MFA].[LEP_HIS].[WorkTimes] "
                + " WHERE [REP_MFA].[LEP_HIS].[WorkTimes].CreateTime BETWEEN '" + startDateTime + "' and '" + completeDateTime
                + "' and [REP_MFA].[LEP_HIS].[WorkTimes].[DispositionCode]='REWORK' "
                + " and [REP_MFA].[LEP_HIS].[WorkTimes].[Route]='" + route
                + "' GROUP BY [REP_MFA].[LEP_HIS].[WorkTimes].[Workplace]) as total";
        //System.out.println("query REW :" + query);
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(1);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getMessage());
            //e.printStackTrace();
        }
        connection.disconnect();
        return 0;
    }

    public int kskREWQteByStartworkplace(String startWorkplace, String startDateTime, String completeDateTime) {
        String query = "SELECT total.sw,sum (kskcount) "
                + " FROM (SELECT [REP_MFA].[dbo].[Sublot].[Startworkplace] as sw,count(distinct ([REP_MFA].[LEP_HIS].[WorkTimes].KSK)) as kskcount FROM [REP_MFA].[LEP_HIS].[WorkTimes] "
                + " INNER JOIN [REP_MFA].[dbo].[Sublot] ON [REP_MFA].[dbo].[Sublot].[LotID] = [REP_MFA].[LEP_HIS].[WorkTimes].[KSK] "
                + " WHERE [REP_MFA].[LEP_HIS].[WorkTimes].CreateTime BETWEEN '" + startDateTime + "' and '" + completeDateTime + "' "
                + " and [REP_MFA].[LEP_HIS].[WorkTimes].[DispositionCode]='REWORK' "
                + " and [REP_MFA].[dbo].[Sublot].[Startworkplace]='" + startWorkplace + "' "
                + " GROUP BY  [REP_MFA].[dbo].[Sublot].[Startworkplace],[REP_MFA].[LEP_HIS].[WorkTimes].[Workplace]) as total GROUP BY total.sw";
        //System.out.println("query REW by BRW:" + query);
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(2);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            //e.printStackTrace();
        }
        connection.disconnect();
        return 0;
    }

    public int kskQteByRoutestep(int routeStep, String startDateTime, String completeDateTime) {
        String query = "SELECT count(distinct ([REP_MFA].[LEP_HIS].[WorkTimes].KSK))  "
                + "FROM [REP_MFA].[LEP_HIS].[WorkTimes]"
                + " INNER JOIN [REP_MFA].[dbo].[Sublot]"
                + "  ON [REP_MFA].[dbo].[Sublot].[LotID] = [REP_MFA].[LEP_HIS].[WorkTimes].[KSK]"
                + "  WHERE [REP_MFA].[LEP_HIS].[WorkTimes].CreateTime BETWEEN '" + startDateTime
                + "'  and '" + completeDateTime
                + "' and [REP_MFA].[LEP_HIS].[WorkTimes].[RouteStep]='" + routeStep
                + "'  and [REP_MFA].[LEP_HIS].[WorkTimes].[DispositionCode]='OK' ";
        //System.out.println("query: " + query);
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(1);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            //e.printStackTrace();
        }
        connection.disconnect();
        return 0;
    }

    public int kskQteByRoutestepAndStartworkplace(int routeStep, String startWorkplace, String startDateTime, String completeDateTime) {//number of KSKs between tow dates in a specific line and a specific route step

        String query = "SELECT [REP_MFA].[dbo].[Sublot].[Startworkplace],count(distinct ([REP_MFA].[LEP_HIS].[WorkTimes].KSK))  "
                + "FROM [REP_MFA].[LEP_HIS].[WorkTimes] "
                + "INNER JOIN [REP_MFA].[dbo].[Sublot] ON [REP_MFA].[dbo].[Sublot].[LotID] = [REP_MFA].[LEP_HIS].[WorkTimes].[KSK] "
                + "WHERE [REP_MFA].[LEP_HIS].[WorkTimes].CreateTime BETWEEN '" + startDateTime + "' and '" + completeDateTime + "'"
                + " and [REP_MFA].[LEP_HIS].[WorkTimes].[RouteStep]='" + routeStep
                + "' and [REP_MFA].[LEP_HIS].[WorkTimes].[DispositionCode]='OK' "
                + " and [REP_MFA].[dbo].[Sublot].[Startworkplace]='" + startWorkplace
                + "' GROUP BY [REP_MFA].[dbo].[Sublot].[Startworkplace]";
        //System.out.println("query: " + query);
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(2);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            //e.printStackTrace();
        }
        connection.disconnect();
        return 0;

    }

    public void showDynamicPart() {
        tableViewDynColumns = FXCollections.observableArrayList();
        tableViewDynData = FXCollections.observableArrayList();
        tableViewDyn.getColumns().setAll(tableViewDynColumns);
        tableViewDyn.setItems(tableViewDynData);
        pieChartDyn.getData().setAll();
        barChartDyn.getData().setAll();

        selectedProcesses = processes.getCheckModel().getSelectedItems();
        if (selectedProcesses.size() == 0) {
            return;
        }
        List<String> lines = lines(selectedSegment);
        TableColumn colLine = null;
        colLine = new TableColumn("Line");
        colLine.setPrefWidth(80);
        colLine.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(0).toString());
                    }
                });
        tableViewDynColumns.add(colLine);
        int[] selectedRouteSteps = new int[selectedProcesses.size()];

        for (int i = 0; i < selectedProcesses.size(); i++) {                        //loop on the selected processes: filling tableview columns
            int j = i + 1;
            selectedRouteSteps[i] = Integer.parseInt(selectedProcesses.get(i).replaceAll("[\\D]", ""));

            TableColumn col = new TableColumn(selectedProcesses.get(i));
            col.setPrefWidth((tableViewDyn.getWidth() - 160) / selectedProcesses.size());
            col.setCellValueFactory(
                    new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                        public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                            ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(j).toString()));
                            return obsInt;
                        }
                    });
            tableViewDynColumns.add(col);
        }
        TableColumn colTotal = new TableColumn("Total");
        colTotal.setPrefWidth(80);
        colTotal.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                    @Override
                    public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                        ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(selectedProcesses.size() + 1).toString()));
                        return obsInt;
                    }
                });
        tableViewDynColumns.add(colTotal);

        if (tableViewDynColumns.size() > 0) {
            tableViewDyn.getColumns().addAll(tableViewDynColumns);
        }

        int total = 0, totalLine = 0;
        int[] totalData = new int[selectedProcesses.size()];
        pieChartData = FXCollections.observableArrayList();
        for (int l = 0; l < lines.size(); l++) {
            tableViewDynRow = FXCollections.observableArrayList();
            total = 0;
            String brw = lines.get(l);
            tableViewDynRow.add(brw);
            for (int b = 0; b < selectedRouteSteps.length; b++) {
                tableViewDynRow.add(getRestByLineAndRouteStep(brw, "" + selectedRouteSteps[b]));
                total += getRestByLineAndRouteStep(brw, "" + selectedRouteSteps[b]);
                totalData[b] += getRestByLineAndRouteStep(brw, "" + selectedRouteSteps[b]);
            }
            pieChartData.add(new PieChart.Data(brw, total));
            totalLine += total;
            //totalData[totalData.length - 1] += total;
            tableViewDynRow.add(total);
            tableViewDynData.add(tableViewDynRow);
        }
        pieChartDyn.setData(pieChartData);
        tableViewDynTotalRow = FXCollections.observableArrayList();
        tableViewDynTotalRow.add("Total");
        XYChart.Series barSeries = new XYChart.Series();
        for (int d = 0; d < totalData.length; d++) {
            tableViewDynTotalRow.add(totalData[d]);
            barSeries.setName(selectedProcesses.get(d));
            barSeries.getData().add(new XYChart.Data<String, Integer>(selectedProcesses.get(d), totalData[d]));
        }
        barChartDyn.getData().add(barSeries);
        tableViewDynTotalRow.add(totalLine);
        tableViewDynData.add(tableViewDynTotalRow);
        System.out.println("table dyn data: " + tableViewDynData);
        if (tableViewDynData.size() > 0) {
            try {
                //tableViewDynData.add(totalRow);
                tableViewDyn.setItems(tableViewDynData);
                tableViewDynInitialData = FXCollections.observableArrayList(tableViewDynData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public int getRestByLineAndRouteStep(String line, String routeStep) {
        String query = "SELECT dbo.Sublot.startworkplace"
                + " ,count (distinct  dbo.Sublot.LotID)"
                + " FROM"
                + " dbo.Sublot"
                + " INNER JOIN COM_SAP.ZCREA ON dbo.Sublot.LotID = COM_SAP.ZCREA.KSK"
                + " WHERE" + " dbo.Sublot.WorkState <> 'CLOSED' "
                + " and startworkplace = '" + line
                + "' and routestepname ='" + routeStep
                + "' group by dbo.Sublot.startworkplace";
        connection = new MSSQLConnection();
        System.out.println(query);
        try {
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            return resultSet.getInt(2);
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            //e.printStackTrace();
        }
        return 0;
    }

    public void fillShifts() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        System.out.println(hour + " : " + minute);
        int currentShiftIndex = 0;

        ObservableList<String> shifts = FXCollections.observableArrayList();
        for (com.leoni.mfa.reporting.config.WorkShift workShift : ConfigReader.getWorkShifts()) {
            if (workShift.isEnabled()) {
                shifts.add("Shift " + workShift.getNumber());
                System.out.println("shift " + workShift.getNumber());
                try {
                    connection = new MSSQLConnection();
                    ResultSet resultSet = connection.selectQuery("SELECT STARTHOUR, STARTMINUTE, ENDHOUR, ENDMINUTE FROM [REP_MFA].[LEP_BAS].[SHIFTMODEL]"
                            + " WHERE SHIFT=" + workShift.getNumber());
                    resultSet.next();
                    int startHours = resultSet.getInt(1);
                    int startMinutes = resultSet.getInt(2);
                    int endHours = resultSet.getInt(3);
                    int endMinutes = resultSet.getInt(4);
                    System.out.println("start date:" + workShift.getNumber() + startHours + " : " + startMinutes);
                    System.out.println("end date:" + workShift.getNumber() + endHours + " : " + endMinutes);

                    if (hour > startHours && hour < endHours) {
                        //this.shifts.getSelectionModel().select(workShift.getNumber() - 1);
                        currentShiftIndex = workShift.getNumber();
                        System.out.println("shift " + currentShiftIndex);
                        startHour = startMinutes;
                        startMinute = startMinutes;
                        endHour = endHours;
                        endMinute = endMinutes;
                        System.out.println("start date:" + startHour + " : " + startMinute);
                        System.out.println("end date:" + endHour + " : " + endMinute);
                    } else if (hour == startHours) {
                        if (minute >= startMinutes) {
                            currentShiftIndex = workShift.getNumber();
                            System.out.println("shift " + currentShiftIndex);
                            startHour = startMinutes;
                            startMinute = startMinutes;
                            endHour = endHours;
                            endMinute = endMinutes;
                            System.out.println("start date:" + startHour + " : " + startMinute);
                            System.out.println("end date:" + endHour + " : " + endMinute);
                        }
                    } else if (hour == endHours) {
                        if (minute <= endMinutes) {
                            currentShiftIndex = workShift.getNumber();
                            System.out.println("shift " + currentShiftIndex);
                            startHour = startMinutes;
                            startMinute = startMinutes;
                            endHour = endHours;
                            endMinute = endMinutes;
                            System.out.println("start date:" + startHour + " : " + startMinute);
                            System.out.println("end date:" + endHour + " : " + endMinute);
                        }
                    }
                } catch (Exception e) {
                    connection.disconnect();
                    logger.error(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        this.shifts.setItems(shifts);
        this.shifts.getSelectionModel().select(currentShiftIndex - 1);
        connection.disconnect();
    }

    public void fillSegments() {
        tglBtnInr = new ToggleButton("INR");
        tglBtnMra = new ToggleButton("MRA");
        sgmBtnsSegments = new SegmentedButton();
        sgmBtnsSegments.getButtons().addAll(tglBtnMra, tglBtnInr);

        segmentsHBox.getChildren().add(sgmBtnsSegments);
        tglBtnMra.selectedProperty().set(true);
        tglBtnInr.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                onSegmentSelected();
            }
        });
        tglBtnMra.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                onSegmentSelected();
            }
        });
    }

    public void onSegmentSelected() {
        //initializes the tableview data and columns
        tableViewStatColumns = FXCollections.observableArrayList();
        tableViewStatData = FXCollections.observableArrayList();
        ObservableList<String> processes = FXCollections.observableArrayList();
        this.processes.getCheckModel().clearSelection();
        this.processes.getItems().clear();
        if (tglBtnInr.selectedProperty().get()) {
            selectedSegment = "INR";
            for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
                if (process.getSegment().equalsIgnoreCase("INR")) {
                    processes.add(process.getOperation() + " (" + process.getRouteStep() + ")");
                }
            }
        } else if (tglBtnMra.selectedProperty().get()) {
            selectedSegment = "MRA";
            for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
                if (process.getSegment().equalsIgnoreCase("MRA")) {
                    processes.add(process.getOperation() + " (" + process.getRouteStep() + ")");
                }
            }
        }
        routeStepsForCalculation = FXCollections.observableArrayList();
        processesForCalculation = FXCollections.observableArrayList();
        for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
            if (process.getSegment().equalsIgnoreCase(selectedSegment) && getRouteStepsForCalculation().contains(Integer.parseInt(process.getRouteStep()))) {
                //routeStepsForCalculation.add(process.getOperation() + " (" + Integer.parseInt(process.getRouteStep()) + ") ");
                routeStepsForCalculation.add("" + Integer.parseInt(process.getRouteStep()));
                processesForCalculation.add(process.getOperation() + " (" + Integer.parseInt(process.getRouteStep()) + ") ");
            }
        }
        this.processes.getItems().addAll(processes);
        this.processes.getCheckModel().selectAll();
    }

    public void onShiftSelected() {
        //initializes the tableview data and columns
        tableViewStatColumns = FXCollections.observableArrayList();
        tableViewStatData = FXCollections.observableArrayList();
        int selectedShiftIndex = shifts.getSelectionModel().getSelectedIndex();
        connection = new MSSQLConnection();
        try {
            switch (selectedShiftIndex) {
                case 0: {
                    ResultSet resultSet = connection.selectQuery("SELECT STARTHOUR, STARTMINUTE, ENDHOUR, ENDMINUTE FROM [REP_MFA].[LEP_BAS].[SHIFTMODEL] WHERE SHIFT=1");
                    resultSet.next();
                    startHour = resultSet.getInt(1);
                    startMinute = resultSet.getInt(2);
                    endHour = resultSet.getInt(3);
                    endMinute = resultSet.getInt(4);
                    break;
                }
                case 1: {
                    ResultSet resultSet = connection.selectQuery("SELECT STARTHOUR, STARTMINUTE, ENDHOUR, ENDMINUTE FROM [REP_MFA].[LEP_BAS].[SHIFTMODEL] WHERE SHIFT=2");
                    resultSet.next();
                    startHour = resultSet.getInt(1);
                    startMinute = resultSet.getInt(2);
                    endHour = resultSet.getInt(3);
                    endMinute = resultSet.getInt(4);
                    break;
                }
                case 2: {
                    ResultSet resultSet = connection.selectQuery("SELECT STARTHOUR, STARTMINUTE, ENDHOUR, ENDMINUTE FROM [REP_MFA].[LEP_BAS].[SHIFTMODEL] WHERE SHIFT=3");
                    resultSet.next();
                    startHour = resultSet.getInt(1);
                    startMinute = resultSet.getInt(2);
                    endHour = resultSet.getInt(3);
                    endMinute = resultSet.getInt(4);
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        connection.disconnect();
    }

    public List<String> lines(String segment) {
        List<String> lines = new ArrayList<String>();
        connection = new MSSQLConnection();
        connection.connecter();
        try {
            switch (segment) {
                case "MRA": {
                    ResultSet resultSet = connection.selectQuery("SELECT distinct[Line] FROM [REP_MFA].[LEP_SYS].[KSKStartCountMax] WHERE [Route]='LEP_ROUTE02'");
                    while (resultSet.next()) {
                        lines.add(resultSet.getString(1));
                    }
                    break;
                }
                case "INR": {
                    ResultSet resultSet = new MSSQLConnection().selectQuery("SELECT distinct[Line] FROM [REP_MFA].[LEP_SYS].[KSKStartCountMax] WHERE [Route]='LEP_ROUTE01'");
                    while (resultSet.next()) {
                        lines.add(resultSet.getString(1));
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            //e.printStackTrace();
        }
        connection.disconnect();
        return lines;
    }

    public void showWorkflowView() {
        try {
            window = (Stage) btnWorkflow.getScene().getWindow();
            root = FXMLLoader.load(getClass().getResource("Workflow.fxml"));
            btnWorkflow.getScene().setRoot(root);

            Scene scene = new Scene(root);
            window.hide();
            window.setScene(scene);
            window.show();
            window.setFullScreen(true);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(WorkflowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showETEView() {
        try {
            window = (Stage) btnETE.getScene().getWindow();
            root = FXMLLoader.load(getClass().getResource("ETE.fxml"));
            btnETE.getScene().setRoot(root);

            Scene scene = new Scene(root);
            window.hide();
            window.setScene(scene);
            window.show();
            window.setFullScreen(true);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(WorkflowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void fillProcessesComboBox() {

        ObservableList<String> processesList = FXCollections.observableArrayList();
        for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
            if (process.getSegment().equalsIgnoreCase("MRA")) {
                processesList.add(process.getOperation() + " (" + process.getRouteStep() + ")");
            }
        }
        Platform.runLater(() -> {
            processes = new CheckComboBox<>(processesList);
            processes.setPrefWidth(110);
            processes.setMaxWidth(110);
            processesHBox.getChildren().add(processes);
            this.processes.getCheckModel().selectAll();
        });
        
    }

    public void showStaticPart() {
        tableViewStatColumns = FXCollections.observableArrayList();
        tableViewStatData = FXCollections.observableArrayList();

        stackedBarChartStat.getData().setAll(FXCollections.observableArrayList());

        TableColumn colLine = null;
        colLine = new TableColumn("Line");
        colLine.setPrefWidth(80);
        colLine.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(0).toString());
                    }
                });
        tableViewStatColumns.add(colLine);

        for (int c = 0; c < routeStepsForCalculation.size(); c++) {
            int d = c + 1;
            TableColumn col = null;
            col = new TableColumn("" + processesForCalculation.get(c));
            col.setPrefWidth((tableViewStat.getWidth() - 220) / routeStepsForCalculation.size());
            col.setCellValueFactory(
                    new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                        public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                            System.out.println("param size: " + param.getValue().size());
                            ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(d).toString()));
                            return obsInt;
                        }
                    });
            tableViewStatColumns.add(col);
        }

        TableColumn colTotal = new TableColumn("Total");
        colTotal.setPrefWidth(70);
        colTotal.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                    public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                        System.out.println("param size: " + param.getValue().size());
                        ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(routeStepsForCalculation.size() + 1).toString()));
                        return obsInt;
                    }
                });
        TableColumn colMax = new TableColumn("Max");
        colMax.setPrefWidth(70);
        colMax.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                    public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                        System.out.println("param size: " + param.getValue().size());
                        ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(routeStepsForCalculation.size() + 2).toString()));
                        return obsInt;
                    }
                });

        stackedStaticBarChartSeries = new XYChart.Series[processesForCalculation.size() + 1];
        for (int g = 0; g <= processesForCalculation.size(); g++) {
            stackedStaticBarChartSeries[g] = new XYChart.Series();
            if (g == processesForCalculation.size()) {
                stackedStaticBarChartSeries[g].setName("Reste");
            } else {
                stackedStaticBarChartSeries[g].setName(processesForCalculation.get(g));
            }
        }

        tableViewStatColumns.add(colTotal);
        tableViewStatColumns.add(colMax);
        tableViewStat.getColumns().setAll(tableViewStatColumns);

        List<String> lines = lines(selectedSegment);
        tableViewStatData = FXCollections.observableArrayList();
        for (int l = 0; l < lines.size(); l++) {
            String brw = lines.get(l);
            tableViewStatRow = FXCollections.observableArrayList();
            tableViewStatRow.add(brw);
            int total = 0;
            for (int c = 0; c < routeStepsForCalculation.size(); c++) {
                tableViewStatRow.add(getRestBYLineAndProcess(brw, "" + routeStepsForCalculation.get(c)));
                total += getRestBYLineAndProcess(brw, "" + routeStepsForCalculation.get(c));
                stackedStaticBarChartSeries[c].getData().add(new XYChart.Data(brw, getRestBYLineAndProcess(brw, "" + routeStepsForCalculation.get(c))));
            }
            tableViewStatRow.add(total);
            tableViewStatRow.add(getMaxBufferByLineAndProcess(brw));
            stackedStaticBarChartSeries[stackedStaticBarChartSeries.length - 1].getData().add(new XYChart.Data(brw, (getMaxBufferByLineAndProcess(brw) - total)));

            tableViewStatData.add(tableViewStatRow);
            System.out.println("row " + l + " : " + tableViewStatRow);
        }

        for (XYChart.Series series : stackedStaticBarChartSeries) {
            stackedBarChartStat.getData().add(series);
        }
        /*stackedBarChartStat.getData().setAll(FXCollections.observableArrayList());
         for (XYChart.Series series : stackedStaticBarChartSeries) {
         stackedBarChartStat.getData().add(series);
         }*/

        System.out.println("tableviewstat data:" + tableViewStatData);
        tableViewStat.setItems(tableViewStatData);

    }

    public int getRestBYLineAndProcess(String line, String process) {
        String query = "SELECT dbo.Sublot.startworkplace"
                + ", count (distinct  dbo.Sublot.LotID)"
                + " FROM"
                + " dbo.Sublot"
                + " INNER JOIN COM_SAP.ZCREA ON dbo.Sublot.LotID = COM_SAP.ZCREA.KSK"
                + " WHERE"
                + " dbo.Sublot.WorkState <> 'CLOSED' "
                + " and startworkplace = '" + line
                + "' and routestepname ='" + process
                + "' group by dbo.Sublot.startworkplace";
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(2);
            return qte;

        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            //e.printStackTrace();
        }
        connection.disconnect();
        return 0;
    }

    public int getMaxBufferByLineAndProcess(String line) {
        String query = "SELECT [Line], [RouteStepForCalculation], [KSKCountMax] "
                + "FROM [REP_MFA].[LEP_SYS].[KSKStartCountMax] "
                + "where [Line]='" + line + "'";
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(3);
            return qte;

        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            //e.printStackTrace();
        }
        connection.disconnect();
        return 0;

    }

    public ObservableList<Integer> getRouteStepsForCalculation() {
        ObservableList<Integer> routeStepsForCalc = FXCollections.observableArrayList();
        String query = "SELECT distinct [RouteStepForCalculation] FROM [REP_MFA].[LEP_SYS].[KSKStartCountMax]";
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            while (resultSet.next()) {
                routeStepsForCalc.add(Integer.parseInt(resultSet.getString(1)));
            }
        } catch (Exception e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return routeStepsForCalc;

    }
}
