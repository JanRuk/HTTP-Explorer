package http.explorer.controller;

import http.explorer.util.ParsedURL;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

import java.io.*;
import java.lang.foreign.PaddingLayout;
import java.net.Socket;

public class MainSceneController {
    public AnchorPane root;
    public WebView wbDisplay;
    public TextField txtAddress;

    public void initialize() throws IOException {
        txtAddress.setText("http://www.google.com");
        loadWebPage(txtAddress.getText());
        txtAddress.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> txtAddress.selectAll());
            }
        });
    }

    public void txtAddressOnAction(ActionEvent event) throws IOException {
        String url = txtAddress.getText();
        if (url.isBlank()) return;

        loadWebPage(url);
    }

    private void loadWebPage(String url) throws IOException {
        System.out.println("url : " + url);
        ParsedURL parsedURL = ParsedURL.parse(url);
        String host = parsedURL.host();
        String protocol = parsedURL.protocol();
        int port = parsedURL.port();
        String path = parsedURL.path();

//        String path = "";
//        if (url.contains("://")) {
//            host = url.substring(url.indexOf("://") + 3);
//            protocol = url.substring(0, url.indexOf("://"));
//        } else {
//            protocol = "http";
//            host = url;
//        }
//        if (host.contains(":")) {
//            port = host.substring(host.indexOf(":") + 1);
//            host = host.substring(0, host.indexOf(":"));
//        } else if (protocol.equals("http")) {
//            port = "80";
//        } else if (protocol.equals("https")) {
//            port = "443";
//        }
//
//        if (port.contains("/")) {
//            path = port.substring(port.indexOf("/"));
//            port = port.substring(0, port.indexOf("/"));
//        } else {
//            path = "/";
//        }
//
//        if (host.contains("/")) {
//            path = host.substring(host.indexOf("/"));
//        }
//
//        if (port.isBlank() || host.isBlank()) {
//            throw new RuntimeException("Invalid web page address");
//        }
//
//        int portInt = Integer.parseInt(port);
        Socket socket = new Socket(host, port);
        System.out.println("Connected to " +socket.getRemoteSocketAddress());

        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);

        String request = """
                GET %S HTTP/1.1
                Host: %s
                User-Agent: dep-browser/1
                Connection: close
                Accept: text/html

                """.formatted(path, host);

        bos.write(request.getBytes());
        bos.flush();

        new Thread(() -> {
            try {
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bsr = new BufferedReader(isr);

                // Read the status line
                String statusLine = bsr.readLine();
                int statusCode = Integer.parseInt(statusLine.split(" ")[1]);
                System.out.println("statusCode : " + statusCode);
                boolean redirection = statusCode >= 300 && statusCode < 400;

                String contentType = null;
                // Read request headers
                String line;
                while ((line = bsr.readLine()) != null && !line.isBlank()) {
                    String header = line.split(":")[0].strip();
                    String value = line.substring(line.indexOf(":") + 1);

                    if (redirection) {
                        if (!header.equalsIgnoreCase("Location")) continue;
                        System.out.println("Redirection" + value);
                        Platform.runLater(() -> txtAddress.setText(value));
                        loadWebPage(value);
                        return;
                    } else {
                        if (!header.equalsIgnoreCase("content-type")) continue;
                        contentType = value;
                    }
                }
                System.out.println("Content Type : " + contentType);
                String content = "";
                while ((line = bsr.readLine()) != null) {
                    content += (line + "\n");
                }
                System.out.println("Content" + "\n"+ content);
                String finalContent = content;
                Platform.runLater(() -> {
                    wbDisplay.getEngine().loadContent(finalContent);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        wbDisplay.getEngine().load(url);

            System.out.println("host : " + host);
            System.out.println("protocol : " + protocol);
            System.out.println("port : " + port);
            System.out.println("path : " + path);
    }
}


