import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {

    private int tcpPort;

    public HttpServer(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public void startServer() {
        try (var serverSocket = new ServerSocket(this.tcpPort)) {
            System.out.println("Server accepting requests on port " + tcpPort);

            while (true) {
                var acceptedSocket = serverSocket.accept();
                var connectionHandler = new ConnectionHandler(acceptedSocket);
                connectionHandler.handle();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
