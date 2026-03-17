package eus.ehu;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PrimaryController {

    @FXML
    private Label messageLabel;

    @FXML
    private void handleClick() {
        messageLabel.setText("Button was clicked!");
    }
}
