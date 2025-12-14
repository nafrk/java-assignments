import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 5000;
    private static final Set<PrintWriter> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Server running on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getRemoteSocketAddress());
                new ClientHandler(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        Socket socket;
        BufferedReader in;
        PrintWriter out;

        ClientHandler(Socket s) {
            this.socket = s;
        }

        public void run() {
            try {
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                clients.add(out);
                out.println("Enter username:");
                String username = in.readLine();

                broadcast(username + " joined the chat.");

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equalsIgnoreCase("/quit")) break;
                    broadcast(username + ": " + msg);
                }

            } catch (Exception e) {
                System.out.println("Client disconnected.");
            } finally {
                clients.remove(out);
                try { socket.close(); } catch (Exception ignored) {}
            }
        }

        private void broadcast(String msg) {
            for (PrintWriter pw : clients) pw.println(msg);
        }
    }
}
