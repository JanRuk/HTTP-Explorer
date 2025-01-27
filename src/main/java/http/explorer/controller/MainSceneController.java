package http.explorer.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

public class MainSceneController {
    public AnchorPane root;
    public TextField txtAddress;
    public WebView wbDisplay;

    public void initialize() {
        txtAddress.setText("www.google.com");
        loadWebPage(txtAddress.getText());
        txtAddress.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                txtAddress.selectAll();
            }
        });
//        wbDisplay.getEngine().load("https://www.google.com");
    }

    public void txtAddressOnAction(ActionEvent event) {
        String url = txtAddress.getText();

        if (url.isBlank()) return;
        loadWebPage(url);
    }

    private void loadWebPage(String url) {

        String host = separateHost(url);
    }

    private String separateHost(String url) {
        String host = url;
        return host;
    }
}
