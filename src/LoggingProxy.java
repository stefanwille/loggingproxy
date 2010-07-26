import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A proxy that logs all packets to the console.
 *
 * Start with: java -cp . LoggingProxy <localport> <serverhost> <serverport>
 * For example: java -cp . LoggingProxy 8080 stefanwille.com 80
 *
 * @author Stefan Wille
 */
public class LoggingProxy {    
    
    public static void main(String[] args) throws Exception {
        new LoggingProxy(Integer.valueOf(args[0]), args[1], Integer.valueOf(args[2]));
    }

    public LoggingProxy(int localPort, String serverName, int serverPort) throws Exception {
        System.out.println("Waiting for connections at localhost :" + localPort);
        ServerSocket acceptSocket = new ServerSocket(localPort);
        for(; ;) {
            Socket client = acceptSocket.accept();
            Socket server = new Socket(serverName, serverPort);
            System.out.println("Connected to " + serverName + ":" + serverPort);
            forward(client, server, "Client");
            forward(server, client, "Server");
        }
    }

    private void forward(final Socket from, final Socket to, final String side) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                doForward(from, to, side);
            }
        });
        thread.start();
    }

    private void doForward(Socket from, Socket to, String side) {
        try {
            InputStream inputStream = from.getInputStream();
            OutputStream outputStream = to.getOutputStream();
            while(!from.isClosed() && !to.isClosed()) {
                byte[] buffer = new byte[4096];
                int bytes = inputStream.read(buffer, 0, 4096);
                if(bytes < 0) {
                    break;
                }
                System.out.println(side + " ----------------");
                System.out.println(new String(buffer, 0, bytes));
                outputStream.write(buffer, 0, bytes);
            }
        } catch(SocketException e) {
            if(!"Socket closed".equals(e.getMessage())) {
                e.printStackTrace();
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                from.close();
            } catch(IOException e) {
                // Ignore
            }
            try {
                to.close();
            } catch(IOException e) {
                // Ignore
            }
        }

    }
}
