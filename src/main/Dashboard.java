package main;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Dashboard {

    public static GridPane getView() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(20);
        gridPane.setHgap(20);

        Label titleLabel = new Label("Dashboard Overview");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #343a40;");
        GridPane.setConstraints(titleLabel, 0, 0, 2, 1);

        HBox topRow = new HBox(20);
        VBox greetingBox = greetingSection();
        ImageView imageView = createImageView(greetingBox); // Pass the greetingBox to the method
        topRow.getChildren().addAll(greetingBox, imageView);
        GridPane.setConstraints(topRow, 0, 1, 2, 1);

        HBox bottomRow = new HBox(20);
        bottomRow.getChildren().addAll(createCalendarSection(), analysisChartSection());
        GridPane.setConstraints(bottomRow, 0, 2, 2, 1);

        gridPane.getChildren().addAll(titleLabel, topRow, bottomRow);
        return gridPane;
    }

    private static VBox greetingSection() {
        VBox greetingBox = new VBox();
        greetingBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20px; -fx-spacing: 10px; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        greetingBox.setMinSize(200, 200); // Set fixed size or use preferred size

        Label greetingLabel = new Label("Hi! Admin");
        greetingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        Label timeLabel = new Label();
        timeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #495057;");

        updateCurrentTime(timeLabel);

        greetingBox.getChildren().addAll(greetingLabel, timeLabel);
        return greetingBox;
    }

    private static void updateCurrentTime(Label timeLabel) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            timeLabel.setText("Current Time: " + now.format(formatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private static ImageView createImageView(VBox greetingBox) {
        Image image = new Image("file:src/main/adminadsh.jpg");
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        // Bind the size of the imageView to the size of the greetingBox
        imageView.fitWidthProperty().bind(greetingBox.widthProperty());
        imageView.fitHeightProperty().bind(greetingBox.heightProperty());

        return imageView;
    }

    private static VBox createCalendarSection() {
        VBox calendar = new VBox();
        calendar.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20px; -fx-spacing: 10px; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label label = new Label("Calendar");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");

        TextField eventInput = new TextField();
        eventInput.setPromptText("Enter event description");

        Button addEventButton = new Button("Add Event");
        addEventButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px; -fx-border-radius: 5px;");
        addEventButton.setOnMouseEntered(e -> addEventButton.setStyle("-fx-background-color: #0056b3;"));
        addEventButton.setOnMouseExited(e -> addEventButton.setStyle("-fx-background-color: #007bff;"));
        addEventButton.setOnAction(e -> {
            String eventDescription = eventInput.getText();
            if (!eventDescription.isEmpty()) {
                LocalDateTime selectedDate = LocalDateTime.of(datePicker.getValue(), LocalDateTime.now().toLocalTime());
                String event = selectedDate.toLocalDate() + ": " + eventDescription;
                @SuppressWarnings("unchecked")
                ListView<String> eventList = (ListView<String>) ((VBox) calendar.getChildren().get(1)).getChildren().get(3);
                eventList.getItems().add(event);
                eventInput.clear();
            }
        });

        ListView<String> eventList = new ListView<>();
        eventList.getItems().addAll();

        VBox eventSection = new VBox(10);
        eventSection.getChildren().addAll(datePicker, eventInput, addEventButton, eventList);

        calendar.getChildren().addAll(label, eventSection);
        return calendar;
    }

    private static VBox analysisChartSection() {
        VBox analysisChart = new VBox();
        analysisChart.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20px; -fx-spacing: 10px; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label label = new Label("Analysis Chart");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #343a40;");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px;");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Sample Data");
        series.getData().add(new XYChart.Data<>(1, 10));
        series.getData().add(new XYChart.Data<>(2, 20));
        series.getData().add(new XYChart.Data<>(3, 15));
        series.getData().add(new XYChart.Data<>(4, 25));

        lineChart.getData().add(series);

        analysisChart.getChildren().addAll(label, lineChart);
        return analysisChart;
    }
}
