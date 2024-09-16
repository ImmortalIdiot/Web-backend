import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler {

    private static final String OK_HTTP = "HTTP/1.1 200OK\n";
    private static final String JSON_CONTENT_TYPE = "Content-Type: text/plain\n";
    private static final String DOWNLOAD_CONTENT_TYPE = """
            Content-Type: text/plain
            Content-Disposition: attachment; filename=test.txt
            """;

    private static final String JSON = "{ student: 'Krasulya Maxim'}";
    private static final String TXT_CONTENT = "Downloaded file";

    private final Socket socket;
    private String endPointPath;

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
            writeResponse(outputStreamWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseRequest(BufferedReader inputStreamReader) throws IOException {
        var request = inputStreamReader.readLine();
        if (request != null && !request.isEmpty()) {
            var endPoints = request.split(" ");
            endPointPath = endPoints[1];
        }

        while (request != null && !request.isEmpty()) {
            System.out.println(request);
            request = inputStreamReader.readLine();
        }
    }

    private void writeResponse(BufferedWriter outputStreamWriter) {
        try {
            if ("/json".equalsIgnoreCase(endPointPath)) {
                sendJson(outputStreamWriter);
            } else if("/download".equalsIgnoreCase(endPointPath)) {
                downloadTxtFile(outputStreamWriter);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendJson(BufferedWriter bufferedWriter) throws IOException {
        writeContent(bufferedWriter, JSON, JSON_CONTENT_TYPE);
    }

    private void downloadTxtFile(BufferedWriter bufferedWriter) throws IOException {
        writeContent(bufferedWriter, TXT_CONTENT, DOWNLOAD_CONTENT_TYPE);
    }

    private void writeContent(BufferedWriter bufferedWriter, String content, String contentType) throws IOException {
        bufferedWriter.write(OK_HTTP);
        bufferedWriter.write("Content-Length: " + content.length() + "\n");
        bufferedWriter.write(contentType);
        bufferedWriter.newLine();
        bufferedWriter.write(content);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
}
