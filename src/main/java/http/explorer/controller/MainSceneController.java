package http.explorer.controller;

import http.explorer.util.ParsedURL;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


        Socket socket = new Socket(host, port);
        String baseUrl = protocol + "://" + host + ":" + port + "/";

        new Thread(() -> {
            try {
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bsr = new BufferedReader(isr);

                String statusLine = bsr.readLine();
                int statusCode = Integer.parseInt(statusLine.split(" ")[1]);
                boolean redirection = statusCode >= 300 && statusCode < 400;

                String line;
                String contentType = null;
                boolean chunked = false;

                while ((line = bsr.readLine()) != null && !line.isBlank()) {
                    String header = line.split(":")[0].strip();
                    String value = line.substring(line.indexOf(":") + 1).strip();
                    if (redirection) {
                        if (!header.equalsIgnoreCase("Location")) continue;
                        System.out.println("Redirection: " + value);
                        Platform.runLater(() -> txtAddress.setText(value));
                        loadWebPage(value);
                        return;
                    } else {
                        if (header.equalsIgnoreCase("Content-Type")) {
                            contentType = value;
                        } else if (header.equalsIgnoreCase("Transfer-Encoding")) {
                            chunked = value.equalsIgnoreCase("chunked");
                        }
                    }
                }

                if (contentType == null || !contentType.contains("text/html")) {
                    displayError("Sorry, content type not supported");
                } else {
                    if (chunked) bsr.readLine();    // Skip the chunk size
                    StringBuilder sb = new StringBuilder();
                    while ((line = bsr.readLine()) != null) {
                        sb.append(line);
                    }
                    if (chunked) sb.deleteCharAt(sb.length() - 1); // Delete the chunk boundary

                    if (!Pattern.compile("<head>.*<base .*</head>").matcher(sb).find()) {
                        Matcher headMatcher = Pattern.compile("<head>").matcher(sb);
                        if (headMatcher.find()) {
                            sb.insert(headMatcher.end(), "<base href='%s'>".formatted(baseUrl));
                        }
                    }

                    Matcher titleMatcher = Pattern.compile("<title>(.+)</title>", Pattern.CASE_INSENSITIVE).matcher(sb);
                    if (titleMatcher.find()) {
                        Platform.runLater(() -> {
                            ((Stage) (root.getScene().getWindow())).setTitle("DEP Browser - " + titleMatcher.group(1));
                        });
                    }


                    Platform.runLater(() -> wbDisplay.getEngine().loadContent(sb.toString()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayError("Connection Failed");
            }
        }).start();

        String httpRequest = """
                GET %s HTTP/1.1
                Host: %s
                User-Agent: Mozilla/5.0
                Connection: close
                Accept: text/html
                
                """.formatted(path, host);
        socket.getOutputStream().write(httpRequest.getBytes());
        socket.getOutputStream().flush();

    }

    private void displayError(String message) {
        Platform.runLater(() -> {
            wbDisplay.getEngine().loadContent("""
                    <!DOCTYPE html>
                    <html>
                    <body>
                    <h1 style="text-align: center;">%s</h1>
                    </body>
                    </html>
                    """.formatted(message));
        });
    }

}


