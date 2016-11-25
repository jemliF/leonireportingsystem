package com.leoni.mfa.reporting.view;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
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
import jfxtras.scene.control.LocalDateTextField;
import jfxtras.scene.control.LocalTimePicker;
import org.apache.log4j.Logger;
import org.controlsfx.control.SegmentedButton;

/**
 * FXML Controller class
 *
 * @author kaab1010
 */
public class ETEController implements Initializable {

    private ToggleButton tglBtnInr;
    private ToggleButton tglBtnMra;
    private SegmentedButton sgmBtnsSegments;
    private MSSQLConnection connection;
    private static final Logger logger = org.apache.log4j.LogManager.getRootLogger();
    private String selectedSegment = "MRA";
    private ObservableList<TableColumn> columns = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> initialData = FXCollections.observableArrayList();
    private ObservableList<Object> row;
    private ObservableList<Object> totalRow;
    private ObservableList<PieChart.Data> pieChartData;
    private Stage window;
    private Parent root;

    @FXML
    private TableView tableView;
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
    private AnchorPane barChartContainer;
    @FXML
    private Label pieChartContainerTitle;
    @FXML
    private Label barChartContainerTitle;
    @FXML
    private ComboBox<String> shifts;
    @FXML
    private Button btnWorkflow;
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
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                startDate.setDateTimeFormatter(dateFormatter);
                endDate.setDateTimeFormatter(dateFormatter);
                startDate.setLocalDate(LocalDate.now());
                endDate.setLocalDate(LocalDate.now());
                barChart.setLegendVisible(false);
                pieChart.setLabelsVisible(false);
                pieChart.setLegendVisible(true);
                fillSegments();
                fillShifts();

                tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        ObservableList<ObservableList<String>> selectedData = FXCollections.observableArrayList();
                        ObservableList<Object> selectedLine = FXCollections.observableArrayList();
                        selectedLine = (ObservableList<Object>) tableView.getSelectionModel().getSelectedItems().get(0);
                        System.out.println("selected line: " + selectedLine);
                        pieChartData = FXCollections.observableArrayList();
                        pieChartContainerTitle.setText(selectedLine.get(0).toString());
                        for (int c = 1; c < columns.size() - 1; c++) {
                            pieChartData.add(new PieChart.Data(columns.get(c).getText(), Double.parseDouble("" + selectedLine.get(c))));
                        }
                        pieChart.setData(pieChartData);
                        pieChart.setLegendVisible(true);
                    }
                });

                tableView.sortPolicyProperty().set((Callback<TableView<ObservableList<ObservableList>>, Boolean>) (TableView<ObservableList<ObservableList>> param) -> {//tableview sorting policy
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
                );

                //adding caption on hover 
                final Label caption = new Label("");
                caption.setTextFill(Color.DARKORANGE);
                caption.setStyle("-fx-font: 24 arial;");
                final ObservableList<Node> children = ((AnchorPane) pieChart.getScene().getRoot()).getChildren();
                children.add(caption);
                for (final PieChart.Data data : pieChart.getData()) {
                    data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                            new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent e) {
                                    System.out.println("mouse entered pie chart");
                                    caption.setTranslateX(e.getSceneX());
                                    caption.setTranslateY(e.getSceneY());
                                    caption.setText(String.valueOf(data.getPieValue()) + "%");
                                    caption.setVisible(true);
                                }
                            });
                }
            }
        });
    }

    public void showReportETE() {
        System.out.println("ETE Report");
        columns = FXCollections.observableArrayList();
        data = FXCollections.observableArrayList();
        tableView.getColumns().setAll(columns);
        tableView.setItems(data);
        barChart.getData().setAll();
        pieChart.getData().setAll();
        //starting date
        String startDateTimeParam = startDate.getLocalDate().toString() + " " + startTime.getLocalTime().getHour() + ":" + startTime.getLocalTime().getMinute();
        //complete date
        String endDateTimeParam = endDate.getLocalDate().toString() + " " + endTime.getLocalTime().getHour() + ":" + endTime.getLocalTime().getMinute();
        List<String> lines = lines(selectedSegment);

        TableColumn colLine = new TableColumn("Line");
        colLine.setPrefWidth(60);
        colLine.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(0).toString());
                    }
                });
        columns.add(colLine);

        ObservableList<String> etes = getETEs(startDateTimeParam, endDateTimeParam);
        System.out.println("etes: " + etes + "\n");
        System.out.println("get ete size: " + etes.size());
        for (int i = 0; i < etes.size(); i++) {
            int j = i + 1;
            System.out.println("j= " + j);
            TableColumn col = new TableColumn(etes.get(i));
            col.setPrefWidth((tableView.getWidth() - colLine.getWidth() - 80) / etes.size());
            col.setCellValueFactory(
                    new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                        @Override
                        public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                            System.out.println("param.getValue size: " + param.getValue().size());
                            ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(j).toString()));
                            return obsInt;
                        }
                    });
            columns.add(col);
        }
        TableColumn colTotalETE = new TableColumn("Total ETE");
        colTotalETE.setPrefWidth(80);
        colTotalETE.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                    @Override
                    public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                        ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(Integer.parseInt(param.getValue().get(etes.size() + 1).toString()));
                        return obsInt;
                    }
                });
        columns.add(colTotalETE);

        if (columns.size() > 0) {
            System.out.println("columns number: " + columns.size());
            tableView.getColumns().addAll(columns);
        }

        int[] totalData = new int[etes.size()];
        //XYChart.Series[] barSeries = new XYChart.Series[etes.size()];
        /*for (XYChart.Series barSerie : barSeries) {
        
         }*/

        for (int l = 0; l < lines.size(); l++) {
            String brw = lines.get(l);
            row = FXCollections.observableArrayList();
            row.add(brw);
            System.out.print(brw + " ");
            Map<String, Integer> statistics = getETEstatisticsByLine(brw, startDateTimeParam, endDateTimeParam);
            if (l == 0) {
                pieChartData = FXCollections.observableArrayList();
            }
            //pieChart.setData(pieChartData);
            int totalETE = 0;
            for (int i = 1; i < columns.size() - 1; i++) {
                if (statistics.get(columns.get(i).getText()) != null) {
                    row.add(statistics.get(columns.get(i).getText()));
                    totalETE += statistics.get(columns.get(i).getText());
                    if (l == 0) {
                        pieChartData.add(new PieChart.Data(columns.get(i).getText(), Double.parseDouble("" + statistics.get(columns.get(i).getText()))));
                        System.out.println("pie chart col: " + i + " : " + etes.get(i - 1) + ", " + Double.parseDouble("" + statistics.get(columns.get(i).getText())));
                    }
                    totalData[i - 1] += statistics.get(columns.get(i).getText());
                    System.out.print(statistics.get(columns.get(i).getText()) + " ");
                } else {
                    row.add(0);
                    if (l == 0) {
                        pieChartData.add(new PieChart.Data(etes.get(i - 1), Double.parseDouble("" + 0)));
                    }
                }
            }
            row.add(totalETE);
            System.out.println("row N° " + l + " : " + row + "\n");
            data.add(row);
        }
        System.out.println("pie chart data: \n" + data);
        pieChart.setData(pieChartData);
        totalRow = FXCollections.observableArrayList();
        totalRow.add("Total");
        XYChart.Series barSeries = new XYChart.Series();
        int total = 0;
        for (int k = 0; k < totalData.length; k++) {
            totalRow.add(totalData[k]);
            total += totalData[k];
            barSeries.getData().add(new XYChart.Data<String, Integer>(etes.get(k), totalData[k]));
        }
        totalRow.add(total);
        barChart.getData().add(barSeries);
        if (data.size() > 0) {
            try {
                data.add(totalRow);
                tableView.setItems(data);
                System.out.println("\ndata: " + data);
                initialData = FXCollections.observableArrayList(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void onSegmentSelected() {
        //initializes the tableview data and columns
        columns = FXCollections.observableArrayList();
        data = FXCollections.observableArrayList();
        tableView.getColumns().setAll(columns);
        tableView.setItems(data);
        barChart.getData().setAll();
        pieChart.getData().setAll();

        if (tglBtnInr.selectedProperty().get()) {
            selectedSegment = "INR";
        } else if (tglBtnMra.selectedProperty().get()) {
            selectedSegment = "MRA";
        }
    }

    public void onShiftSelected() {
        //initializes the tableview data and columns
        columns = FXCollections.observableArrayList();
        data = FXCollections.observableArrayList();
        tableView.getColumns().setAll(columns);
        tableView.setItems(data);
        barChart.getData().setAll();
        pieChart.getData().setAll();
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
            connection.disconnect();
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        connection.disconnect();
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

    public void showWorkflowView() {
        try {
            System.out.println("Show Workflow view");
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

    public void showOverviewView() {
        try {
            System.out.println("Show Workflow view");
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

    public ObservableList<String> getETEs(String startDateTime, String completeDateTime) {
        int routeStep = 62;
        for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
            if (process.getSegment().equalsIgnoreCase(selectedSegment) && process.getOperation().equalsIgnoreCase("ETE")) {
                routeStep = Integer.parseInt(process.getRouteStep().replaceAll("[\\D]", ""));
                break;
            }
        }

        String query = "SELECT distinct[Workplace]"
                + "FROM [REP_MFA].[LEP_HIS].[WorkTimes]"
                + "WHERE [RouteStep]='" + routeStep + "' "
                + "and [CreateTime] BETWEEN '" + startDateTime + "' and '" + completeDateTime + "'";
        System.out.println("query: " + query);
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            ObservableList<String> etes = FXCollections.observableArrayList();
            while (resultSet.next()) {
                etes.add(resultSet.getString(1));
            }
            connection.disconnect();
            return etes;
        } catch (Exception e) {
            connection.disconnect();
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        connection.disconnect();
        return null;
    }

    public Map<String, Integer> getETEstatisticsByLine(String line, String startDateTime, String completeDateTime) {
        int routeStep = 62;
        for (com.leoni.mfa.reporting.config.Process process : ConfigReader.getProcesses()) {
            if (process.getSegment().equalsIgnoreCase(selectedSegment) && process.getOperation().equalsIgnoreCase("ETE")) {
                routeStep = Integer.parseInt(process.getRouteStep().replaceAll("[\\D]", ""));
                break;
            }
        }
        String query = "SELECT [REP_MFA].[dbo].[Sublot].[Startworkplace],[REP_MFA].[LEP_HIS].[WorkTimes].[Workplace], count(distinct ([REP_MFA].[LEP_HIS].[WorkTimes].[KSK])) "
                + " FROM [REP_MFA].[LEP_HIS].[WorkTimes] "
                + " INNER JOIN [REP_MFA].[dbo].[Sublot] ON [REP_MFA].[dbo].[Sublot].[LotID] = [REP_MFA].[LEP_HIS].[WorkTimes].[KSK] "
                + " WHERE [REP_MFA].[LEP_HIS].[WorkTimes].[DispositionCode]='OK' and [RouteStep]='" + routeStep + "' "
                + " and [REP_MFA].[dbo].[Sublot].[Startworkplace] like '" + line + "'"
                + " and ([REP_MFA].[LEP_HIS].[WorkTimes].[Operation]='ETE' or [REP_MFA].[LEP_HIS].[WorkTimes].[Operation]='BRW') "
                + " and [REP_MFA].[LEP_HIS].[WorkTimes].[CreateTime] between '" + startDateTime + "' "
                + " and '" + completeDateTime + "' "
                + " GROUP BY [Workplace] ,[REP_MFA].[dbo].[Sublot].[Startworkplace] "
                + " ORDER BY [REP_MFA].[dbo].[Sublot].[Startworkplace] asc, [REP_MFA].[LEP_HIS].[WorkTimes].[Workplace] asc";
        System.out.println("query: " + query);
        Map<String, Integer> eteStatistics = new HashMap<String, Integer>();
        try {
            connection = new MSSQLConnection();
            ResultSet resultSet = connection.selectQuery(query);
            int totalETE = 0, i = 0;
            while (resultSet.next()) {
                eteStatistics.put(resultSet.getString(2), resultSet.getInt(3));
                if (i >= 1) {
                    totalETE += resultSet.getInt(3);
                }
                i++;
            }
            //eteStatistics.put("Total ETE", totalETE);
            return eteStatistics;
        } catch (Exception sQLException) {
            connection.disconnect();
            sQLException.printStackTrace();
        }
        connection.disconnect();
        return null;

    }

}
