package com.leoni.mfa.reporting.training;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransitionBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author fathi jemli
 */
public class AnimatedChartsController implements Initializable {

    @FXML
    private PieChart pieChart;
    @FXML
    AnchorPane mainAnchorPane;
    private Label caption;

    private ObservableList<PieChart.Data> pieChartData;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Austria", 10),
                new PieChart.Data("Swiss", 20),
                new PieChart.Data("Germany", 30),
                new PieChart.Data("France", 40),
                new PieChart.Data("Spain", 50),
                new PieChart.Data("Italy", 60)
        );
        pieChart.setData(pieChartData);

        ObservableList<Node> children = FXCollections.observableArrayList();
        //children = ((AnchorPane) pieChart.getScene().getRoot()).getChildren();
        children = mainAnchorPane.getChildren();
        caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");
        children.add(caption);
        for (final PieChart.Data data : pieChart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                            System.out.println("mouse pressed");
                            caption.setTranslateX(e.getSceneX());
                            caption.setTranslateY(e.getSceneY());
                            caption.setText(String.valueOf(data.getPieValue()));
                            caption.setVisible(true);
                        }
                    });
            data.getNode().setOnMousePressed(new AnimatedChartsController.MousePressedAnimation(data));
        }

        //slices transition animation
        for (PieChart.Data d : pieChartData) {
            d.getNode().setOnMouseEntered(new PieChartSample.MouseHoverAnimation(d, pieChart));
            d.getNode().setOnMouseExited(new PieChartSample.MouseExitAnimation());
        }
        pieChart.setClockwise(false);
    }

    class MousePressedAnimation implements EventHandler<MouseEvent> {

        private PieChart.Data data;

        public MousePressedAnimation(PieChart.Data data) {
            this.data = data;
        }

        @Override
        public void handle(MouseEvent e) {
            //System.out.println("mouse pressed");
            caption.setTranslateX(e.getSceneX());
            caption.setTranslateY(e.getSceneY());
            caption.setText(String.valueOf(data.getPieValue()) );
            caption.setVisible(true);
        }
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
