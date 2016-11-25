package com.leoni.mfa.reporting.guioff;

import Test.Test;
import com.leoni.mfa.reporting.config.ConfigReader;
import com.leoni.mfa.reporting.db.MSSQLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import jfxtras.scene.control.LocalDateTextField;
import jfxtras.scene.control.LocalTimePicker;
import org.apache.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.SegmentedButton;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author bewa1022
 */
public class TabbedFunctionController implements Initializable {

    private CheckComboBox<String> processes;
    private ToggleButton tglBtnInr;
    private ToggleButton tglBtnMra;
    private SegmentedButton sgmBtnsSegments;
    private MSSQLConnection connection;
    private static Logger logger = org.apache.log4j.LogManager.getRootLogger();
    private String selectedSegment = "MRA";
    private ObservableList<TableColumn> columns = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> initialData = FXCollections.observableArrayList();
    private TableColumn<Test, Boolean> checkCol = new TableColumn<Test, Boolean>();
    private ObservableList<String> selectedProcesses;
    private ObservableList<Object> row;
    private ObservableList<Object> totalRow;

    @FXML
    private TableView tableView;
    @FXML
    private LineChart lineChart;
    @FXML
    private BarChart barChart;
    @FXML
    private PieChart pieChart;

    @FXML
    private LocalDateTextField startDate;
    @FXML
    private LocalDateTextField endDate;

    @FXML
    private LocalTimePicker startTime;
    @FXML
    private LocalTimePicker endTime;

    @FXML
    private HBox processesHBox;
    @FXML
    private HBox segmentsHBox;
    @FXML
    private HBox shiftsHBox;

    @FXML
    Button btnShowReportWorkflow;
    @FXML
    private VBox chartsContainer;

    @FXML
    private ComboBox<String> shifts;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                startDate.setDateTimeFormatter(dateFormatter);
                endDate.setDateTimeFormatter(dateFormatter);
                startDate.setLocalDate(LocalDate.now());
                endDate.setLocalDate(LocalDate.now());
                chartsContainer.getChildren().remove(barChart);
                chartsContainer.getChildren().remove(pieChart);
                barChart.setLegendVisible(false);

                fillProcessesComboBox();
                fillSegments();
                fillShifts();

                //addCheckColumn();
                tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {         //tableview selection listener

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        lineChart.getData().setAll();
                        ObservableList<ObservableList<String>> selectedData = FXCollections.observableArrayList();
                        ObservableList<Object> selectedLine = FXCollections.observableArrayList();
                        for (int i = 0; i < tableView.getSelectionModel().getSelectedItems().size(); i++) {
                            selectedLine = (ObservableList<Object>) tableView.getSelectionModel().getSelectedItems().get(i);
                            System.out.println("selected line: " + selectedLine);
                            if (((String) selectedLine.get(0)).contains("Total")) {
                                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                            } else {
                                tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                            }
                            XYChart.Series lineSeries = new XYChart.Series();
                            lineSeries.setName((String) selectedLine.get(0));
                            ObservableList<String> selectedProcesses = FXCollections.observableArrayList();
                            selectedProcesses = processes.getCheckModel().getSelectedItems();
                            for (int j = 0; j < selectedProcesses.size(); j++) {
                                if (!selectedProcesses.get(j).contains("REW")) {
                                    lineSeries.getData().add(new XYChart.Data<String, Integer>(selectedProcesses.get(j).replaceAll("[\\D]", ""),
                                            Integer.parseInt("" + selectedLine.get(j + 1))));
                                }
                            }
                            lineChart.getData().add(lineSeries);
                        }
                    }
                });
                tableView.getSortOrder().addListener(new ListChangeListener() {             //tableview sorting listener

                    @Override
                    public void onChanged(ListChangeListener.Change c) {
                        System.out.println("initial data: " + initialData);
                        System.out.println("current data: " + data);
                        System.out.println("sorted column: " + ((TableColumn) tableView.getSortOrder().get(0)).getText());
                        //for(int k=0;k<tableView.getColumns().size())
                        int selectedColPosition = tableView.getColumns().indexOf(tableView.getSortOrder().get(0));
                        System.out.println("selectedColPosition: " + selectedColPosition);
                        ObservableList<PieChart.Data> pieChartData
                                = FXCollections.observableArrayList();
                        System.out.println("selected processes: " + selectedProcesses);
                        for (int i = 0; i < initialData.size() - 1; i++) {
                            //System.out.println("BRW: " + (String) initialData.get(i).get(0));
                            //System.out.println("value: " + (Double) initialData.get(i).get(selectedColPosition));
                            pieChartData.add(new PieChart.Data((String) initialData.get(i).get(0), Double.parseDouble("" + initialData.get(i).get(selectedColPosition))));
                        }
                        pieChart.setData(pieChartData);
                        chartsContainer.getChildren().add(pieChart);
                    }
                });
                tableView.sortPolicyProperty().set(new Callback<TableView<ObservableList<ObservableList>>, Boolean>() {         //tableview sorting policy

                    @Override
                    public Boolean call(TableView<ObservableList<ObservableList>> param) {
                        Comparator<ObservableList> comparator = new Comparator<ObservableList>() {
                            @Override
                            public int compare(ObservableList r1, ObservableList r2) {
                                if (r1 == totalRow) {
                                    return 1;
                                } else if (r2 == totalRow) {
                                    return -1;
                                } else if (param.getComparator() == null) {
                                    return 0;
                                } else {
                                    return param.getComparator()
                                            .compare(r1, r2);
                                }
                            }
                        };
                        FXCollections.sort(tableView.getItems(), comparator);
                        return true;
                    }
                });
            }
        });

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
        });
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

    public void fillShifts() {
        ObservableList<String> shifts = FXCollections.observableArrayList();
        for (com.leoni.mfa.reporting.config.WorkShift workShift : ConfigReader.getWorkShifts()) {
            if (workShift.isEnabled()) {
                shifts.add("Shift " + workShift.getNumber());
            }
        }
        this.shifts.setItems(shifts);
        this.shifts.getSelectionModel().select(0);
    }

    public void showReportWorkflow() {
        //initializes the tableview data and columns, line chart
        columns = FXCollections.observableArrayList();
        data = FXCollections.observableArrayList();
        tableView.getColumns().setAll(columns);
        tableView.setItems(data);
        lineChart.getData().setAll();
        barChart.getData().setAll();
        chartsContainer.getChildren().remove(pieChart);

        int rewIndex = 0;
        //starting date
        String startDateTimeParam = startDate.getLocalDate().toString() + " " + startTime.getLocalTime().getHour() + ":" + startTime.getLocalTime().getMinute();
        //complete date
        String endDateTimeParam = endDate.getLocalDate().toString() + " " + endTime.getLocalTime().getHour() + ":" + endTime.getLocalTime().getMinute();
        List<String> lines = lines(selectedSegment);

        selectedProcesses = FXCollections.observableArrayList();//selected processes for reporting
        selectedProcesses = processes.getCheckModel().getSelectedItems();
        TableColumn colLine = null;
        if (selectedProcesses.size() > 0) {
            colLine = new TableColumn("Line");
            colLine.setPrefWidth(50);
            colLine.setCellValueFactory(
                    new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                            return new SimpleStringProperty(param.getValue().get(0).toString());
                        }
                    });
            columns.add(colLine);
        }
        int[] selectedRouteSteps = new int[selectedProcesses.size()];

        for (int i = 0; i < selectedProcesses.size(); i++) {                        //loop on the selected processes: filling tableview columns
            if (selectedProcesses.get(i).contains("REW")) {                         //when REW is selected for the report
                chartsContainer.getChildren().add(barChart);
                rewIndex = i;
                for (int l = 0; l < lines.size(); l++) {
                    String brw = lines.get(l);
                }

            } else {
                chartsContainer.getChildren().remove(barChart);
            }
            //everything including REW
            int j = i + 1;
            selectedRouteSteps[i] = Integer.parseInt(selectedProcesses.get(i).replaceAll("[\\D]", ""));

            TableColumn col = new TableColumn(selectedProcesses.get(i));
            col.setPrefWidth((tableView.getWidth() - colLine.getWidth()) / selectedProcesses.size());
            col.setCellValueFactory(
                    new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                            return new SimpleStringProperty(param.getValue().get(j).toString());
                        }
                    });
            columns.add(col);
        }
        if (columns.size() > 0) {
            tableView.getColumns().addAll(columns);
        }
        XYChart.Series barSeries = new XYChart.Series();

        int[] totalData = new int[selectedProcesses.size()];
        for (int l = 0; l < lines.size(); l++) {                    //loop on the lines: filling tableview data
            String brw = lines.get(l);
            row = FXCollections.observableArrayList();
            row.add(brw);
            XYChart.Series lineSeries = new XYChart.Series();

            lineSeries.setName(brw);
            barSeries.setName(brw);
            for (int i = 0; i < selectedRouteSteps.length; i++) {

                if (!selectedProcesses.get(i).contains("REW")) {//Not a REW process
                    lineSeries.getData().add(new XYChart.Data<String, Integer>("" + selectedRouteSteps[i], kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i],
                            brw, startDateTimeParam, endDateTimeParam)));//adding data to line chart series
                    //adding data to tableview 
                    if (kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i], brw, startDateTimeParam, endDateTimeParam) >= 10) {
                        row.add(kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i], brw, startDateTimeParam, endDateTimeParam));
                    } else {
                        row.add("0" + kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i], brw, startDateTimeParam, endDateTimeParam));
                    }
                    //row.add(kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i], brw, startDateTimeParam, endDateTimeParam));//adding data to tableview 
                    totalData[i] += kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i], brw, startDateTimeParam, endDateTimeParam);
                } else {//REW process
                    barSeries.getData().add(new XYChart.Data<String, Integer>(brw, kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam)));//adding data to bar chart series

                    if (kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam) >= 10) {//adding data to tableview 
                        row.add(kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam));
                    } else {
                        row.add("0" + kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam));
                    }
                    totalData[i] += kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam);
                }
            }
            data.add(row);
            lineChart.getData().add(lineSeries);
        }
        totalRow = FXCollections.observableArrayList();
        totalRow.add("Total");
        for (int k = 0; k < totalData.length; k++) {
            totalRow.add(totalData[k]);
        }
        barChart.getData().add(barSeries);
        if (data.size() > 0) {
            try {
                data.add(totalRow);
                tableView.setItems(data);
                initialData = FXCollections.observableArrayList(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public int kskREWQteByStartworkplace(String startWorkplace, String startDateTime, String completeDateTime) {
        String query = "SELECT total.sw,sum (kskcount) "
                + "FROM (SELECT [REP_MFA].[dbo].[Sublot].[Startworkplace] as sw,count(distinct ([REP_MFA].[LEP_HIS].[WorkTimes].KSK)) as kskcount FROM [REP_MFA].[LEP_HIS].[WorkTimes] "
                + "INNER JOIN [REP_MFA].[dbo].[Sublot] ON [REP_MFA].[dbo].[Sublot].[LotID] = [REP_MFA].[LEP_HIS].[WorkTimes].[KSK] "
                + "WHERE [REP_MFA].[LEP_HIS].[WorkTimes].CreateTime BETWEEN '" + startDateTime + "' and '" + completeDateTime + "' "
                + "and [REP_MFA].[LEP_HIS].[WorkTimes].[DispositionCode]='REWORK' "
                + "and [REP_MFA].[dbo].[Sublot].[Startworkplace]='" + startWorkplace + "' "
                + "GROUP BY  [REP_MFA].[dbo].[Sublot].[Startworkplace],[REP_MFA].[LEP_HIS].[WorkTimes].[Workplace]) as total GROUP BY total.sw";
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(2);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
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
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(2);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return 0;

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
            e.printStackTrace();
        }
        connection.disconnect();
        return lines;
    }

    public void onShiftSelected() {
        //initializes the tableview data and columns
        columns = FXCollections.observableArrayList();
        data = FXCollections.observableArrayList();
        tableView.getColumns().setAll(columns);
        tableView.setItems(data);
        chartsContainer.getChildren().remove(barChart);
        int selectedShiftIndex = shifts.getSelectionModel().getSelectedIndex();
        int startHour, startMinute, endHour, endMinute;
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
                    startTime.setLocalTime(LocalTime.of(startHour, startMinute));
                    endTime.setLocalTime(LocalTime.of(endHour, endMinute));
                    startDate.setLocalDate(new Date(System.currentTimeMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    endDate.setLocalDate(new Date(System.currentTimeMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    break;
                }
                case 1: {
                    ResultSet resultSet = connection.selectQuery("SELECT STARTHOUR, STARTMINUTE, ENDHOUR, ENDMINUTE FROM [REP_MFA].[LEP_BAS].[SHIFTMODEL] WHERE SHIFT=2");
                    resultSet.next();
                    startHour = resultSet.getInt(1);
                    startMinute = resultSet.getInt(2);
                    endHour = resultSet.getInt(3);
                    endMinute = resultSet.getInt(4);
                    startTime.setLocalTime(LocalTime.of(startHour, startMinute));
                    endTime.setLocalTime(LocalTime.of(endHour, endMinute));
                    startDate.setLocalDate(new Date(System.currentTimeMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    endDate.setLocalDate(new Date(System.currentTimeMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    break;
                }
                case 2: {
                    ResultSet resultSet = connection.selectQuery("SELECT STARTHOUR, STARTMINUTE, ENDHOUR, ENDMINUTE FROM [REP_MFA].[LEP_BAS].[SHIFTMODEL] WHERE SHIFT=3");
                    resultSet.next();
                    startHour = resultSet.getInt(1);
                    startMinute = resultSet.getInt(2);
                    endHour = resultSet.getInt(3);
                    endMinute = resultSet.getInt(4);
                    startTime.setLocalTime(LocalTime.of(startHour, startMinute));
                    endTime.setLocalTime(LocalTime.of(endHour, endMinute));
                    startDate.setLocalDate(new Date(System.currentTimeMillis() - 1000L * 60L * 60L * 24L).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        connection.disconnect();
    }

    public void onSegmentSelected() {
        //initializes the tableview data and columns
        columns = FXCollections.observableArrayList();
        data = FXCollections.observableArrayList();
        tableView.getColumns().setAll(columns);
        chartsContainer.getChildren().remove(barChart);
        tableView.setItems(data);
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
        this.processes.getItems().addAll(processes);
    }

    public void addCheckColumn() {
        checkCol.setPrefWidth(25);
        checkCol.setCellValueFactory(cellData -> {
            //return new ReadOnlyBooleanWrapper(true);
            return new SimpleBooleanProperty(true);
        });
        checkCol.setCellFactory(CheckBoxTableCell.<Test>forTableColumn(checkCol));
        tableView.getColumns().add(checkCol);
    }

    public ObservableList<Object> getDataByLine(ObservableList<ObservableList<Object>> items, String line) {
        for (ObservableList<Object> row : items) {
            if (((String) row.get(0)).contains(line)) {
                return row;
            }
        }
        return null;
    }
}
