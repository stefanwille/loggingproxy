import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Stefan Wille
 */
public class LoggingProxy {
    public static final String SERVER_NAME = "192.168.1.15";
    public static final int SERVER_PORT = 8095;
    public static final int LOCAL_PORT = 8080;

    
    public static void main(String[] args) throws Exception {
        new LoggingProxy();
    }

    public LoggingProxy() throws Exception {
        System.out.println ("Waiting for connections at localhost :" + LOCAL_PORT);
        ServerSocket acceptSocket = new ServerSocket(LOCAL_PORT);
        Socket client = acceptSocket.accept();
        Socket server = new Socket(SERVER_NAME, SERVER_PORT);
        System.out.println("Connected to " + SERVER_NAME + ":" + SERVER_PORT);
        forward(client, server, "Client");
        forward(server, client, "Server");
    }

    private void forward(final Socket from, final Socket to, final String side) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                forwardSynchronously(from, to, side);
            }
        });
        thread.start();
    }

    private void forwardSynchronously(Socket from, Socket to, String side) {
        try {
            InputStream inputStream = from.getInputStream();
            OutputStream outputStream = to.getOutputStream();
            while(!from.isClosed() && !to.isClosed()) {
                //System.out.println ("waiting for " + side);
                byte[] buffer = new byte[4096];
                int bytes = inputStream.read(buffer, 0, 4096);
                if(bytes < 0) {
                    break;
                }
                System.out.println(side + " ----------------");
                System.out.println(new String(buffer, 0, bytes));
                outputStream.write(buffer, 0, bytes);
            }
            from.close();
            to.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
