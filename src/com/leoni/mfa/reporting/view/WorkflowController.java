package com.leoni.mfa.reporting.view;

import Test.Test;
import com.leoni.mfa.reporting.config.ConfigReader;
import com.leoni.mfa.reporting.db.MSSQLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.animation.TranslateTransitionBuilder;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import jfxtras.scene.control.LocalDateTextField;
import jfxtras.scene.control.LocalTimePicker;
import org.apache.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.SegmentedButton;
import org.joda.time.DateTime;
import org.joda.time.*;
import org.joda.time.DurationFieldType;

/**
 * FXML Controller class
 *
 * @author bewa1022
 */
public class WorkflowController implements Initializable {

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
    private ObservableList<Object> goalRow;
    private ObservableList<Object> percentageRow;
    private ObservableList<PieChart.Data> pieChartData;
    private Stage window;
    private Parent root;
    private XYChart.Series lineSeries;
    private Label caption;
    private ObservableList<String> processesList;
    private int shift1StartHour, shift1StartMinute, shift1EndHour, shift1EndMinute,
            shift2StartHour, shift2StartMinute, shift2EndHour, shift2EndMinute,
            shift3StartHour, shift3StartMinute, shift3EndHour, shift3EndMinute;
    @FXML
    private TableView tableView;
    @FXML
    private LineChart lineChart;
    @FXML
    private BarChart barChart;
    @FXML
    private PieChart pieChart;
    @FXML
    AnchorPane mainAnchorPane;
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
    private VBox mainContainer;
    @FXML
    private VBox reportContainer;
    @FXML
    private HBox middleChartsContainer;
    @FXML
    private AnchorPane reworkContainer;
    @FXML
    private AnchorPane barChartContainer;
    @FXML
    private Label pieChartContainerTitle;
    @FXML
    private Label lineChartContainerTitle;
    @FXML
    private ComboBox<String> shifts;
    @FXML
    private Button btnETE;
    @FXML
    private Button btnOverview;

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
                reportContainer.getChildren().remove(reworkContainer);
                barChart.setLegendVisible(false);
                pieChart.setLabelsVisible(false);

                fillProcessesComboBox();
                fillSegments();
                fillShifts();
                //processes.getCheckModel().selectIndices(0, 1, 2, 3, 4, 8);

                ObservableList<Node> children = FXCollections.observableArrayList();
                //children = ((AnchorPane) pieChart.getScene().getRoot()).getChildren();
                children = mainAnchorPane.getChildren();
                System.out.println("anchor children: " + children.size());
                caption = new Label("");
                caption.setTextFill(Color.DARKORANGE);
                caption.setStyle("-fx-font: 24 arial;");
                children.add(caption);
                System.out.println("anchor children: " + children.size());
                for (final PieChart.Data data : pieChartData) {
                    System.out.println("pie data");
                    data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                            new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent e) {
                                    System.out.println("MOUSE_ENTERED");
                                    caption.setTranslateX(e.getSceneX());
                                    caption.setTranslateY(e.getSceneY());
                                    caption.setText(String.valueOf(data.getPieValue()));
                                    caption.setVisible(true);
                                }
                            });
                    //data.getNode().setOnMousePressed(new AnimatedChartsController.MousePressedAnimation(data));
                }

                for (PieChart.Data d : pieChartData) {
                    d.getNode().setOnMouseEntered(new MouseHoverAnimation(d, pieChart));
                    d.getNode().setOnMouseExited(new MouseExitAnimation());
                }
                pieChart.setClockwise(false);

                tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {         //tableview selection listener

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        lineChartContainerTitle.setText("View by line: ");
                        lineChart.getData().setAll();
                        ObservableList<ObservableList<String>> selectedData = FXCollections.observableArrayList();
                        ObservableList<Object> selectedLine = FXCollections.observableArrayList();
                        for (int i = 0; i < tableView.getSelectionModel().getSelectedItems().size(); i++) {
                            selectedLine = (ObservableList<Object>) tableView.getSelectionModel().getSelectedItems().get(i);
                            if (selectedLine.get(0).equals("Total")) {
                                lineChartContainerTitle.setText(lineChartContainerTitle.getText() + selectedLine.get(0));
                            } else {
                                if (i == tableView.getSelectionModel().getSelectedItems().size() - 1) {
                                    lineChartContainerTitle.setText(lineChartContainerTitle.getText() + selectedLine.get(0));
                                } else {
                                    lineChartContainerTitle.setText(lineChartContainerTitle.getText() + selectedLine.get(0) + " | ");
                                }
                            }
                            System.out.println("selected line: " + selectedLine);
                            if (((String) selectedLine.get(0)).contains("Total")) {
                                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                            } else {
                                tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                            }
                            XYChart.Series lineSeries = new XYChart.Series();
                            lineSeries.setName((String) selectedLine.get(0));
                            //ObservableList<String> selectedProcesses = FXCollections.observableArrayList();
                            //selectedProcesses = processes.getCheckModel().getSelectedItems();
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
                //tableview sorting listener
                tableView.getSortOrder().addListener((ListChangeListener.Change c) -> {
                    System.out.println("initial data: " + initialData);
                    System.out.println("current data: " + data);
                    System.out.println("sorted column: " + ((TableColumn) tableView.getSortOrder().get(0)).getText());
                    if (!((TableColumn) tableView.getSortOrder().get(0)).getText().equalsIgnoreCase("Line")) {
                        pieChartContainerTitle.setText(((TableColumn) tableView.getSortOrder().get(0)).getText());
                        int selectedColPosition = tableView.getColumns().indexOf(tableView.getSortOrder().get(0));
                        System.out.println("selectedColPosition: " + selectedColPosition);
                        pieChartData = FXCollections.observableArrayList();
                        System.out.println("selecpieChartDatated processes: " + selectedProcesses);
                        for (int i = 0; i < initialData.size() - 2; i++) {
                            pieChartData.add(new PieChart.Data((String) initialData.get(i).get(0), Double.parseDouble("" + initialData.get(i).get(selectedColPosition))));
                        }
                    } else {
                        pieChartData = FXCollections.observableArrayList();
                    }
                    pieChart.setData(pieChartData);
                }
                );
                tableView.sortPolicyProperty().set((Callback<TableView<ObservableList<ObservableList>>, Boolean>) (TableView<ObservableList<ObservableList>> param) -> {//tableview sorting policy
                    Comparator<ObservableList> comparator = new Comparator<ObservableList>() {
                        @Override
                        public int compare(ObservableList r1, ObservableList r2) {
                            if (r1 == totalRow || r1 == goalRow) {
                                return 1;
                            } else if (r2 == totalRow || r2 == goalRow) {
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
                );
            }
        });

    }

    public void fillProcessesComboBox() {

        processesList = FXCollections.observableArrayList();
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
        lineChart.setData(FXCollections.observableArrayList());
        lineChart.setLegendVisible(false);
        barChart.getData().setAll();
        pieChart.getData().setAll();
        pieChartContainerTitle.setText("View by process");

        lineChartContainerTitle.setText("View by line");
        int rewIndex = 0;
        //starting date
        String startDateTimeParam = startDate.getLocalDate().toString() + " " + startTime.getLocalTime().getHour() + ":" + startTime.getLocalTime().getMinute();
        //complete date
        String endDateTimeParam = endDate.getLocalDate().toString() + " " + endTime.getLocalTime().getHour() + ":" + endTime.getLocalTime().getMinute();
        List<String> lines = lines(selectedSegment);

        selectedProcesses = FXCollections.observableArrayList();//selected processes for reporting
        selectedProcesses = processes.getCheckModel().getSelectedItems();
        if (selectedProcesses.size() == 0) {
            return;
        }
        TableColumn colLine = null;
        if (selectedProcesses.size() > 0) {
            colLine = new TableColumn("Line");
            colLine.setPrefWidth(70);
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
                reportContainer.getChildren().add(reworkContainer);
                rewIndex = i;
                for (int l = 0; l < lines.size(); l++) {
                    String brw = lines.get(l);
                }

            } else {
                reportContainer.getChildren().remove(reworkContainer);
            }
            //everything including REW
            int j = i + 1;
            selectedRouteSteps[i] = Integer.parseInt(selectedProcesses.get(i).replaceAll("[\\D]", ""));
            System.out.println("goalByDateandRouteStep: " + goalByDateandRouteStep("" + selectedRouteSteps[i], startDateTimeParam, endDateTimeParam));

            TableColumn col = new TableColumn(selectedProcesses.get(i));
            col.setPrefWidth((tableView.getWidth() - colLine.getWidth()) / selectedProcesses.size());
            col.setCellValueFactory(
                    new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                        public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                            System.out.println("param size: " + param.getValue().size());
                            //return new SimpleStringProperty(param.getValue().get(j).toString());
                            ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(j).toString()));
                            return obsInt;
                        }
                    });
            columns.add(col);
        }
        if (columns.size() > 0) {
            tableView.getColumns().addAll(columns);
        }
        XYChart.Series barSeries = new XYChart.Series();
        pieChartData = FXCollections.observableArrayList();
        int[] totalData = new int[selectedProcesses.size()];
        for (int l = 0; l < lines.size(); l++) {                    //loop on the lines: filling tableview data
            String brw = lines.get(l);
            row = FXCollections.observableArrayList();
            row.add(brw);
            lineSeries = new XYChart.Series();

            lineSeries.setName(brw);
            barSeries.setName(brw);
            for (int i = 0; i < selectedRouteSteps.length; i++) {
                if (!selectedProcesses.get(i).contains("REW")) {//Not a REW process
                    lineSeries.getData().add(new XYChart.Data<String, Integer>("" + selectedRouteSteps[i], kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i],
                            brw, startDateTimeParam, endDateTimeParam)));//adding data to line chart series
                    //adding data to tableview 
                    row.add(kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i], brw, startDateTimeParam, endDateTimeParam));
                    totalData[i] += kskQteByRoutestepAndStartworkplace(selectedRouteSteps[i], brw, startDateTimeParam, endDateTimeParam);
                } else {//REW process
                    barSeries.getData().add(new XYChart.Data<String, Integer>(brw, kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam)));//adding data to bar chart series
                    //adding data to tableview 
                    row.add(kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam));
                    totalData[i] += kskREWQteByStartworkplace(brw, startDateTimeParam, endDateTimeParam);
                }
                if (i == 0) {
                    pieChartData.add(new PieChart.Data(lines.get(l), Double.parseDouble("" + row.get(1))));
                }
            }
            data.add(row);
            lineChart.getData().add(lineSeries);
            lineChart.setLegendVisible(true);
        }
        totalRow = FXCollections.observableArrayList();
        totalRow.add("Total");
        for (int k = 0; k < totalData.length; k++) {
            totalRow.add(totalData[k]);
        }
        goalRow = FXCollections.observableArrayList();
        percentageRow = FXCollections.observableArrayList();
        goalRow.add("Objectif");
        percentageRow.add("%");
        for (int b = 0; b < selectedRouteSteps.length; b++) {
            goalRow.add(goalByDateandRouteStep("" + selectedRouteSteps[b], startDateTimeParam, endDateTimeParam));
            try {
                double percent = totalData[b] / goalByDateandRouteStep("" + selectedRouteSteps[b], startDateTimeParam, endDateTimeParam);

                percentageRow.add(Integer.parseInt("" + ((totalData[b] / goalByDateandRouteStep("" + selectedRouteSteps[b], startDateTimeParam, endDateTimeParam)) * 100)));
                //System.out.println("percent: " + totalData[b] + " / " + goalByDateandRouteStep("" + selectedRouteSteps[b], startDateTimeParam, endDateTimeParam));
                System.out.println("percent: " + percent);
            } catch (ArithmeticException e) {
                percentageRow.add(0);
                e.printStackTrace();
            }
        }

        barChart.getData().add(barSeries);
        //barChart.getData().removeAll();
        //barChart.getData().setAll(barSeries);

        pieChart.setData(pieChartData);
        if (data.size() > 0) {
            try {
                data.add(totalRow);
                data.add(goalRow);
                //data.add(percentageRow);
                tableView.setItems(data);
                initialData = FXCollections.observableArrayList(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public int kskREWQteByStartworkplace(String startWorkplace, String startDateTime, String completeDateTime) {
        /*org.joda.time.LocalDate localStartDate = new org.joda.time.LocalDate(startDateTime);
         org.joda.time.LocalDate localCompleteDate = new org.joda.time.LocalDate(completeDateTime);*/

        String query = "SELECT total.sw,sum (kskcount) "
                + "FROM (SELECT [REP_MFA].[dbo].[Sublot].[Startworkplace] as sw,count(distinct ([REP_MFA].[LEP_HIS].[WorkTimes].KSK)) as kskcount FROM [REP_MFA].[LEP_HIS].[WorkTimes] "
                + "INNER JOIN [REP_MFA].[dbo].[Sublot] ON [REP_MFA].[dbo].[Sublot].[LotID] = [REP_MFA].[LEP_HIS].[WorkTimes].[KSK] "
                + "WHERE [REP_MFA].[LEP_HIS].[WorkTimes].CreateTime BETWEEN '" + startDateTime + "' and '" + completeDateTime + "' "
                + "and [REP_MFA].[LEP_HIS].[WorkTimes].[DispositionCode]='REWORK' "
                + "and [REP_MFA].[dbo].[Sublot].[Startworkplace]='" + startWorkplace + "' "
                + "GROUP BY  [REP_MFA].[dbo].[Sublot].[Startworkplace],[REP_MFA].[LEP_HIS].[WorkTimes].[Workplace]) as total GROUP BY total.sw";
        System.out.println("rew query: " + query);
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(2);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getMessage());
            e.printStackTrace();
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
        System.out.println("query: " + query);
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            resultSet.next();
            int qte = resultSet.getInt(2);
            connection.disconnect();
            return qte;
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        connection.disconnect();
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
        pieChartData = FXCollections.observableArrayList();
        pieChart.setData(pieChartData);
        pieChartContainerTitle.setText("View by process");
        lineSeries = new XYChart.Series();
        lineChart.setData(FXCollections.observableArrayList());
        tableView.getColumns().setAll(columns);
        tableView.setItems(data);
        reportContainer.getChildren().remove(reworkContainer);
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
                    shift1StartHour = startHour;

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
            connection.disconnect();
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        connection.disconnect();
    }

    public void onSegmentSelected() {
        //initializes the tableview data and columns
        columns = FXCollections.observableArrayList();
        data = FXCollections.observableArrayList();
        pieChartData = FXCollections.observableArrayList();
        pieChart.setData(pieChartData);
        pieChartContainerTitle.setText("View by process");
        lineSeries = new XYChart.Series();
        lineChart.setData(FXCollections.observableArrayList());
        tableView.getColumns().setAll(columns);
        reportContainer.getChildren().remove(reworkContainer);
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

    public ObservableList<Object> getDataByLine(ObservableList<ObservableList<Object>> items, String line) {
        for (ObservableList<Object> row : items) {
            if (((String) row.get(0)).contains(line)) {
                return row;
            }
        }
        return null;
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

    public void showOverviewView() {
        try {
            window = (Stage) btnOverview.getScene().getWindow();
            root = FXMLLoader.load(getClass().getResource("Overview.fxml"));
            btnOverview.getScene().setRoot(root);

            Scene scene = new Scene(root);
            window.hide();
            window.setScene(scene);
            window.show();
            window.setFullScreen(true);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(WorkflowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int goalByDateandRouteStep(String routeStep, String startDateTimeStr, String completeDateTimeStr) {
        int qte = 0;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date startDateTime = format.parse(startDateTimeStr);
            System.out.println(startDateTime);
            Date endDateTime = format.parse(completeDateTimeStr);
            System.out.println(endDateTime);

            org.joda.time.LocalDate localStartDateTime = new org.joda.time.LocalDate(startDateTime);
            org.joda.time.LocalDate localEndDateTime = new org.joda.time.LocalDate(endDateTime);

            List<LocalDate> dates = new ArrayList<LocalDate>();
            int days = Days.daysBetween(localStartDateTime, localEndDateTime).getDays();
            for (int i = 0; i <= days; i++) {
                org.joda.time.LocalDate d = localStartDateTime.withFieldAdded(DurationFieldType.days(), i);
                System.out.println("date " + i + " " + d);
                if (i == 0) {//first day in the interval
                    if (shift1EndHour < startDateTime.getHours() && shift1EndMinute < startDateTime.getMinutes()) {//shift 1

                    }
                    if (shift2EndHour < startDateTime.getHours() && shift2EndMinute < startDateTime.getMinutes()) {//shift 1

                    }
                    if (shift3EndHour < startDateTime.getHours() && shift3EndMinute < startDateTime.getMinutes()) {//shift 1

                    }
                } else if (i == days) {//last day in the interval
                    if (shift1EndHour < startDateTime.getHours() && shift1EndMinute < startDateTime.getMinutes()) {//shift 1

                    }
                    if (shift2EndHour < startDateTime.getHours() && shift2EndMinute < startDateTime.getMinutes()) {//shift 1

                    }
                    if (shift3EndHour < startDateTime.getHours() && shift3EndMinute < startDateTime.getMinutes()) {//shift 1

                    }
                } else {
                    System.out.println("day: " + i + " : shift 1");
                    System.out.println("day: " + i + " : shift 2");
                }
                //dates.add(d);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        org.joda.time.format.DateTimeFormatter dateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        org.joda.time.format.DateTimeFormatter dateFormatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");

        DateTime startDateTime = dateTimeFormatter.parseDateTime(startDateTimeStr);
        System.out.println("startDate: " + startDateTime);
        DateTime completeDateTime = dateTimeFormatter.parseDateTime(completeDateTimeStr);
        System.out.println("complete date: " + completeDateTime);
        if (startDateTime.toLocalDate().isEqual(completeDateTime.toLocalDate())) {
            //Date date = dateFormatter.parseLocalDate(routeStep)
            int selectedShiftNumber = Integer.parseInt(shifts.getSelectionModel().getSelectedItem().replaceAll("[\\D]", ""));
            String query = "SELECT distinct [REP_MFA].[LEP_HIS].[WorkTimes].[RouteStep], "
                    + " [REP_MFA].[LEP_HIS].[Productivity].[Machine], "
                    + " substring([REP_MFA].[LEP_HIS].[Productivity].[Machine],1,3), "
                    + " [REP_MFA].[LEP_HIS].[Productivity].[Shift],[REP_MFA].[LEP_HIS].[Productivity].[KSKCount_SP]   "
                    + " FROM [REP_MFA].[LEP_HIS].[Productivity]   "
                    + " INNER JOIN [REP_MFA].[LEP_HIS].[WorkTimes] "
                    + " ON [REP_MFA].[LEP_HIS].[WorkTimes].[Workplace] = [REP_MFA].[LEP_HIS].[Productivity].[Machine]   "
                    + " where [REP_MFA].[LEP_HIS].[Productivity].[KSKCount_SP] != '0'   "
                    + " and [REP_MFA].[LEP_HIS].[Productivity].[Shift] = '" + selectedShiftNumber
                    + "' and [REP_MFA].[LEP_HIS].[Productivity].[ShiftDate] = '" + startDateTime.toLocalDate()
                    + "' and [REP_MFA].[LEP_HIS].[WorkTimes].[RouteStep] ='" + routeStep + "'";

            System.out.println("query: " + query);
            try {
                connection = new MSSQLConnection();
                ResultSet resultSet = connection.selectQuery(query);
                resultSet.next();
                qte = Integer.parseInt(resultSet.getString(5));
                System.out.println("goal quantity: " + qte);
                connection.disconnect();
                return qte;
            } catch (Exception e) {
                connection.disconnect();
                e.printStackTrace();
            }
        } else {

        }

        List<org.joda.time.LocalDate> dates = new ArrayList<org.joda.time.LocalDate>();
        int days = Days.daysBetween(startDateTime, completeDateTime).getDays();
        System.out.println(days);
        for (int i = 0; i < days; i++) {
            org.joda.time.LocalDate d = startDateTime.toLocalDate().withFieldAdded(DurationFieldType.days(), i);
            dates.add(d);
        }

        /*List<Date> dates = new ArrayList<Date>();
         Calendar cal = Calendar.getInstance();
         cal.setTime(startDateTime.toDate());
         while (startDateTime.isBefore(completeDateTime)) {
         cal.add(Calendar.DATE, 1);
         dates.add(cal.getTime());
         startDateTime = startDateTime.plusDays(1);
         }*/
        for (org.joda.time.LocalDate date : dates) {
            System.out.println(date);
        }
        connection.disconnect();
        return 0;
    }

    static class MouseHoverAnimation implements EventHandler<MouseEvent> {

        static final Duration ANIMATION_DURATION = new Duration(500);
        static final double ANIMATION_DISTANCE = 0.15;
        private double cos;
        private double sin;
        private PieChart chart;

        public MouseHoverAnimation(PieChart.Data d, PieChart chart) {
            this.chart = chart;
            double start = 0;
            double angle = calcAngle(d);
            for (PieChart.Data tmp : chart.getData()) {
                if (tmp == d) {
                    break;
                }
                start += calcAngle(tmp);
            }

            cos = Math.cos(Math.toRadians(0 - start - angle / 2));
            sin = Math.sin(Math.toRadians(0 - start - angle / 2));
        }

        @Override
        public void handle(MouseEvent arg0) {
            Node n = (Node) arg0.getSource();

            double minX = Double.MAX_VALUE;
            double maxX = Double.MAX_VALUE * -1;

            for (PieChart.Data d : chart.getData()) {
                minX = Math.min(minX, d.getNode().getBoundsInParent().getMinX());
                maxX = Math.max(maxX, d.getNode().getBoundsInParent().getMaxX());
            }

            double radius = maxX - minX;
            TranslateTransitionBuilder.create().toX((radius * ANIMATION_DISTANCE) * cos).toY((radius * ANIMATION_DISTANCE) * sin).duration(ANIMATION_DURATION).node(n).build().play();
        }

        private static double calcAngle(PieChart.Data d) {
            double total = 0;
            for (PieChart.Data tmp : d.getChart().getData()) {
                total += tmp.getPieValue();
            }

            return 360 * (d.getPieValue() / total);
        }
    }

    static class MouseExitAnimation implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {
            TranslateTransitionBuilder.create().toX(0).toY(0).duration(new Duration(500)).node((Node) event.getSource()).build().play();
        }
    }
}
