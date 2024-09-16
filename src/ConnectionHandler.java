import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ConnectionHandler {

    private static final String OK_HTTP = "HTTP/1.1 200OK\n";
    private static final String JSON_CONTENT_TYPE = "Content-Type: text/plain\n";

    private static final String JSON = "{ student: 'Krasulya Maxim'}";

    private static final Random random = new Random();

    private final int SESSION_ID = generateSessionId();
    private final int USER_ID = generateUserUnique();

    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        handle();
    }

    public void handle() {
        try {
            var inputStreamReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));

            var outputStreamWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            parseRequest(inputStreamReader);
            writeResponse(outputStreamWriter, JSON, JSON_CONTENT_TYPE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseRequest(BufferedReader inputStreamReader) throws IOException {
        var request = inputStreamReader.readLine();

        while (request != null && !request.isEmpty()) {
            System.out.println(request);
            request = inputStreamReader.readLine();
        }
    }

    private void writeResponse(BufferedWriter bufferedWriter, String content, String contentType) throws IOException {
        bufferedWriter.write(OK_HTTP);
        bufferedWriter.write("Content-Length: " + content.length() + "\n");
        bufferedWriter.write(contentType);
        bufferedWriter.newLine();
        bufferedWriter.write(content);
        bufferedWriter.write("Set-Cookie: SSID=" + SESSION_ID + "; " +
                "Path=/; Max-Age: 3600; HttpOnly\n");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private int generateSessionId() {
        return random.nextInt(1, 100 + 1);
    }

    private int generateUserUnique() {
        return random.nextInt(1000, 10000 + 1);
    }
}

