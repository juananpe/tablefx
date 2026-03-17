module eus.ehu {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    opens eus.ehu to javafx.fxml;
    exports eus.ehu;
}
