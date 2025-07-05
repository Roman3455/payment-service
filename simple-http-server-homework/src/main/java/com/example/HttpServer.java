package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HttpServer {
    private final static String SOURCE_DIR = "simple-http-server-homework/static";
    private final static Path NOT_FOUND = Paths.get(SOURCE_DIR, "/404.html");
    private final static List<String> CONTENT_TYPES = Arrays.asList("text/html", "application/json");

    public static void main(String[] args) throws IOException {
        try (ServerSocket socket = new ServerSocket(8080)) {
            while (true) {
                try (Socket client = socket.accept();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(client.getInputStream()));
                     BufferedWriter out = new BufferedWriter(
                             new OutputStreamWriter(client.getOutputStream()))) {

                    String[] request = in.readLine().split(" ");
                    if (request.length > 1) {
                        out.write(getServerResponse(request[1]));
                        System.out.println(getServerResponse(request[1]));
                        out.flush();
                    }
                }
            }
        }
    }

    private static String getServerResponse(String request) throws IOException {
        Path basePath = Paths.get(SOURCE_DIR).toAbsolutePath().normalize();
        Path requestedPath = basePath.resolve(request.substring(1)).normalize();
        boolean isFileFound = requestedPath.startsWith(basePath) && Files.exists(requestedPath);
        Path fileToRead =  isFileFound ? requestedPath : NOT_FOUND;
        String statusCode = isFileFound ? "200 OK" : "404 Not Found";
        String extension = getExtension(requestedPath.getFileName().toString());
        Optional<String> contentType = CONTENT_TYPES.stream()
                .filter(c -> c.endsWith(extension))
                .findFirst();
        return String.format("""
                        HTTP/1.1 %s\r
                        Content-Type: %s; charset=UTF-8\r
                        Content-Length: %d\r
                        \r
                        %s
                        """,
                statusCode,
                contentType.orElse("text/html"),
                Files.readAllBytes(fileToRead).length,
                Files.readString(fileToRead)
        );
    }

    private static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1 && dotIndex < filename.length() - 1)
                ? filename.substring(dotIndex + 1)
                : "";
    }
}
