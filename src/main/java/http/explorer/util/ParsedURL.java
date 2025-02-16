package http.explorer.util;

public record ParsedURL(String protocol, String host, int port, String path ) {

    public static ParsedURL parse(String url) {
        String protocol = "";
        String host = "";
        int port = -1;
        String path = "";

        if (url.contains("://")) {
            protocol = url.substring(0, url.indexOf("://"));
            host = url.substring(url.indexOf("://") + 3);
        } else {
            protocol = "http";
            host = url;
        }

        if (host.contains(":")) {
            String portStr = url.substring(url.indexOf(":") + 1);
            host = host.substring(0, host.indexOf(":"));

            if (portStr.contains("/")) {
                path = url.substring(url.indexOf("/"));
                portStr = portStr.substring(0, portStr.indexOf("/"));
            }

            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Invalid port number: " + portStr);
            }
        } else {
            port = switch (protocol) {
                case "http" -> 80;
                case "https" -> 443;
                default -> -1;
            };
        }
        if (host.contains("/")) {
            path = url.substring(url.indexOf("/"));
            host = host.substring(0, host.indexOf("/"));
        }
        if (path.isBlank()) {
            path = "/";
        }
        if (host.isBlank() || port == -1) {
            throw new RuntimeException("Invalid web page address");
        }
        return new ParsedURL(protocol, host, port, path);
    }
}
