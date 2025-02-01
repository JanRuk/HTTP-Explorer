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
        System.out.println("url : " + url);
        String host = "";
        String protocol = "";
        String port = "";

        String path = "";
        if (url.contains("://")) {
            host = url.substring(url.indexOf("://") + 3);
            protocol = url.substring(0, url.indexOf("://"));
        } else {
            protocol = "http";
            host = url;
        }
        if (host.contains(":")) {
            port = host.substring(host.indexOf(":") + 1);
            host = host.substring(0, host.indexOf(":"));
        } else if (protocol.equals("http")) {
            port = "80";
        } else if (protocol.equals("https")) {
            port = "443";
        }

        if (port.contains("/")) {
            path = port.substring(port.indexOf("/"));
            port = port.substring(0, port.indexOf("/"));
        } else {
            path = "/";
        }

        if (host.contains("/")) {
            path = host.substring(host.indexOf("/"));
        }

        if (port.isBlank() || host.isBlank()) {
            throw new RuntimeException("Invalid web page address");
        }

        System.out.println("Host : " + host);
        System.out.println("Protocol : " + protocol);
        System.out.println("Port : " + port);
        System.out.println("Path : " + path);
    }

    private String separateHost(String url) {
        String host = url;
        return host;
    }
}
