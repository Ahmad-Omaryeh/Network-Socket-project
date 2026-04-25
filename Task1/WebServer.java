import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.regex.*;

public class WebServer {
    public static void main(String[] args) throws IOException {
        int port = 5012;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is running on the port = " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleRequest(clientSocket)).start();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            System.out.println("Request: " + requestLine);
            String[] tokens = requestLine.split(" ");
            if (tokens.length < 2) return;

            String path = tokens[1];

            if (path.startsWith("/search?")) {
                String[] queryParams = path.split("\\?")[1].split("&");
                String keyword = "";
                String type = "text";
                boolean isArabic = false;

                for (String param : queryParams) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("keyword")) {
                            keyword = URLDecoder.decode(pair[1], "UTF-8").toLowerCase(Locale.ROOT);
                        } else if (pair[0].equals("type")) {
                            type = URLDecoder.decode(pair[1], "UTF-8").toLowerCase(Locale.ROOT);
                        } else if (pair[0].equals("lang")) {
                            isArabic = "ar".equals(URLDecoder.decode(pair[1], "UTF-8"));
                        }
                    }
                }

                String eventFileName = isArabic ? "./html/event_details_ar.html" : "./html/event_details.html";
                File eventFile = new File(eventFileName);

                if (eventFile.exists()) {
                    String html = Files.readString(eventFile.toPath()).toLowerCase(Locale.ROOT);

                    Pattern pattern = Pattern.compile("<h[23]>(.*?)</h[23]>");
                    Matcher matcher = pattern.matcher(html);
                    boolean found = false;

                    while (matcher.find()) {
                        String heading = matcher.group(1).toLowerCase();
                        if (heading.contains(keyword)) {
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        byte[] content = Files.readAllBytes(eventFile.toPath());
                        out.write("HTTP/1.1 200 OK\r\n".getBytes());
                        out.write("Content-Type: text/html\r\n".getBytes());
                        out.write(("Content-Length: " + content.length + "\r\n\r\n").getBytes());
                        out.write(content);
                    } else {
                        String location;
                        if (isArabic) {
                            switch (type) {
                                case "video":
                                    location = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(keyword, "UTF-8") + "&hl=ar";
                                    break;
                                case "image":
                                    location = "https://www.google.com/search?q=" + URLEncoder.encode(keyword, "UTF-8") + "&tbm=isch&hl=ar";
                                    break;
                                default:
                                    location = "https://www.aljazeera.net/search/" + URLEncoder.encode(keyword, "UTF-8");
                                    break;
                            }
                        } else {
                            switch (type) {
                                case "video":
                                    location = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(keyword, "UTF-8");
                                    break;
                                case "image":
                                    location = "https://www.google.com/search?q=" + URLEncoder.encode(keyword, "UTF-8") + "&tbm=isch";
                                    break;
                                default:
                                    location = "https://www.aljazeera.com/search/" + URLEncoder.encode(keyword, "UTF-8");
                                    break;
                            }
                        }

                        out.write("HTTP/1.1 307 Temporary Redirect\r\n".getBytes());
                        out.write(("Location: " + location + "\r\n\r\n").getBytes());
                    }
                } else {
                    out.write("HTTP/1.1 500 Internal Server Error\r\n".getBytes());
                    out.write("Content-Type: text/plain\r\n\r\n".getBytes());
                    out.write("Event file not found.".getBytes());
                }

                out.flush();
                clientSocket.close();
                return;
            }

            switch (path) {
                case "/":
                case "/en":
                case "/index.html":
                case "/main_en.html":
                    path = "/html/main_en.html";
                    break;
                case "/ar":
                case "/main_ar.html":
                    path = "/html/main_ar.html";
                    break;
            }

            File file = new File("." + path);
            if (file.exists()) {
                String contentType = Files.probeContentType(file.toPath());
                byte[] content = Files.readAllBytes(file.toPath());

                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write(("Content-Type: " + contentType + "\r\n").getBytes());
                out.write(("Content-Length: " + content.length + "\r\n\r\n").getBytes());
                out.write(content);
            } else {
                String errorHtml = "<html><head><title>Error 404</title></head>" +
                        "<body><h1 style='color:red;'>The file is not found</h1>" +
                        "<p>Client IP: " + clientSocket.getInetAddress() + "</p>" +
                        "<p>Client Port: " + clientSocket.getPort() + "</p></body></html>";

                out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
                out.write("Content-Type: text/html\r\n\r\n".getBytes());
                out.write(errorHtml.getBytes());
            }

            out.flush();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}