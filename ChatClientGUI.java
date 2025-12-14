import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ChatClientGUI extends Application {

    private TextArea chatArea;
    private TextField inputField;
    private TextField usernameField;
    private PrintWriter writer;
    private BufferedReader reader;
    private Socket socket;

    private Thread listeningThread;
    private boolean connected = false;

    private final String HOST = "localhost";
    private final int PORT = 5000;

    @Override
    public void start(Stage stage) {

        chatArea = new TextArea();
        chatArea.setEditable(false);

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        inputField = new TextField();
        inputField.setDisable(true);

        Button connectBtn = new Button("Connect");
        connectBtn.setOnAction(e -> connect());

        Button disconnectBtn = new Button("Disconnect");
        disconnectBtn.setOnAction(e -> disconnect());

        Button sendBtn = new Button("Send");
        sendBtn.setDisable(true);
        sendBtn.setOnAction(e -> send());

        HBox top = new HBox(10, usernameField, connectBtn, disconnectBtn);
        HBox bottom = new HBox(10, inputField, sendBtn);

        BorderPane root = new BorderPane(chatArea, top, null, bottom, null);
        Scene scene = new Scene(root, 500, 400);

        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();

        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (writer != null) sendBtn.setDisable(newVal.trim().isEmpty());
        });
    }

    // -------------------------------
    // CONNECT TO SERVER
    // -------------------------------
    private void connect() {
        if (connected) return;

        listeningThread = new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                String username = usernameField.getText().trim();
                writer.println(username);

                connected = true;

                Platform.runLater(() -> {
                    chatArea.appendText("Connected as " + username + "\n");
                    inputField.setDisable(false);
                });

                String msg;
                while (connected && (msg = reader.readLine()) != null) {
                    String m = msg;
                    Platform.runLater(() -> chatArea.appendText(m + "\n"));
                }

            } catch (Exception e) {
                Platform.runLater(() -> chatArea.appendText("Connection lost.\n"));
            }
        });

        listeningThread.start();
    }

    // -------------------------------
    // DISCONNECT FROM SERVER
    // -------------------------------
    private void disconnect() {
        if (!connected) return;

        connected = false;

        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}

        Platform.runLater(() -> {
            chatArea.appendText("Disconnected.\n");
            inputField.setDisable(true);
        });

        if (listeningThread != null && listeningThread.isAlive()) {
            listeningThread.interrupt();
        }
    }

    // -------------------------------
    // SEND MESSAGE
    // -------------------------------
    private void send() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            writer.println(msg);
        }
        inputField.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

