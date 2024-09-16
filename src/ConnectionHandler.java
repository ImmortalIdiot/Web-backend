import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ConnectionHandler {

    private static final Random RANDOM = new Random();
    private static final String SESSION_NAME = "Session";

    private static final int SESSION = generateSessionId();
    private static final int USER = generateUserUnique();

    public String getHeader(String text) {
        return "HTTP/1.1 200OK\n" +
                "Content-Length: " + text.length() + "\n" +
                "Content-Type: text/html\n";
    }

    public String headerWithCookie(String text, int session, int user) {
        return "HTTP/1.1 200OK\n" +
                "Content-Length: " + text.length() + "\n" +
                "Content-Type: text/html\n" +
                "Set-Cookie: " + SESSION_NAME + "=" + session + "_" + user + "; lang=en\n";
    }

    private final Socket socket;

    private static final String SEND_COOKIE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <title>Simple Http Server</title>
            </head>
            <body>
            <p>Cookie not founded. Reload the page</p>
            </body>
            </html>
            """;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        handle();
    }

    private static int generateSessionId() { return RANDOM.nextInt(1, 100 + 1); }

    private static int generateUserUnique() { return RANDOM.nextInt(1000, 10000 + 1); }

    public void handle() {
        try {
            var inputStreamReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));

            var outputStreamWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            var request = parseRequest(inputStreamReader);
            var session = containSession(request);

            if (session == null) {
                writeResponse(outputStreamWriter, SEND_COOKIE,
                        headerWithCookie(SEND_COOKIE, SESSION, USER));
            } else {
                var response = "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "<meta charset=\"UTF-8\">\n" +
                        "<title>Simple Http Server</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<p>Hello " + session + "</p>\n" +
                        "</body>\n" +
                        "</html>\n";
                writeResponse(outputStreamWriter, response, getHeader(response));
            }

            outputStreamWriter.flush();
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private String containSession(String request) {
        if (!request.contains("Cookie:")) { return null; }

        int session = request.lastIndexOf(SESSION_NAME);
        if (session == -1) { return null; }

        return request.substring(session + SESSION_NAME.length() + 1, request.length() - 2);
    }

    private String parseRequest(BufferedReader inputStreamReader) throws IOException {
        StringBuilder fullRequest = new StringBuilder();
        var request = inputStreamReader.readLine();

        while (request != null && !request.isEmpty()) {
            System.out.println(request);
            request = inputStreamReader.readLine();
            fullRequest.append(request).append("\n");
        }
        return fullRequest.toString();
    }

    private void writeResponse(BufferedWriter bufferedWriter, String request, String header) throws IOException {
        bufferedWriter.write(header);
        bufferedWriter.newLine();
        bufferedWriter.write(request);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
}
