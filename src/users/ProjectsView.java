package users;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ProjectsView {

    public Node getView() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        Label titleLabel = new Label("Projects");
        titleLabel.getStyleClass().add("title-label");

        // Simulated project data, replace with actual data retrieval logic if needed
        for (int i = 1; i <= 5; i++) {
            Label projectLabel = new Label("Project " + i);
            vbox.getChildren().add(projectLabel);
        }

        return vbox;
    }
}
