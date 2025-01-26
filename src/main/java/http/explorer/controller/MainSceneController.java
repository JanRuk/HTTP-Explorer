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
//        wbDisplay.getEngine().load("https://www.google.com");
    }

    public void txtAddressOnAction(ActionEvent event) {


    }
}
