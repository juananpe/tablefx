module eus.ehu {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires okhttp3;
    requires com.google.gson;

    opens eus.ehu to javafx.fxml, com.google.gson;
    exports eus.ehu;
}

