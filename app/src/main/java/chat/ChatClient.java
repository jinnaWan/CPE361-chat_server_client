package chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ChatClient extends Application {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private Button connectButton;
    private TextField serverField;
    private TextField portField;
    private Label statusLabel;
    private boolean connected = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PA10 Chat Client");
        
        // Create UI components
        createUI(primaryStage);
        
        // Handle window closing
        primaryStage.setOnCloseRequest(e -> {
            disconnect();
            Platform.exit();
        });
        
        primaryStage.show();
    }
    
    private void createUI(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Top panel - Connection controls
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));
        
        Label titleLabel = new Label("PA10 Chat Client");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox connectionBox = new HBox(10);
        connectionBox.getChildren().addAll(
            new Label("Server:"),
            serverField = new TextField("localhost"),
            new Label("Port:"),
            portField = new TextField("8080"),
            connectButton = new Button("Connect")
        );
        
        statusLabel = new Label("Not connected");
        statusLabel.setStyle("-fx-text-fill: red;");
        
        topPanel.getChildren().addAll(titleLabel, connectionBox, statusLabel);
        
        // Center panel - Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefRowCount(20);
        chatArea.setStyle("-fx-font-family: monospace;");
        chatArea.appendText("Welcome to PA10 Chat!\n");
        chatArea.appendText("Enter server details above and click Connect to start chatting.\n\n");
        
        // Bottom panel - Message input
        HBox bottomPanel = new HBox(10);
        bottomPanel.setPadding(new Insets(10));
        
        messageField = new TextField();
        messageField.setDisable(true);
        messageField.setPrefColumnCount(40);
        messageField.setPromptText("Type your message here...");
        
        sendButton = new Button("Send");
        sendButton.setDisable(true);
        
        bottomPanel.getChildren().addAll(
            new Label("Message:"),
            messageField,
            sendButton
        );
        
        // Layout
        root.setTop(topPanel);
        root.setCenter(chatArea);
        root.setBottom(bottomPanel);
        
        // Event handlers
        connectButton.setOnAction(e -> toggleConnection());
        sendButton.setOnAction(e -> sendMessage());
        
        // Enter key sends message
        messageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });
        
        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
    }
    
    private void toggleConnection() {
        if (!connected) {
            connect();
        } else {
            disconnect();
        }
    }
    
    private void connect() {
        String server = serverField.getText().trim();
        String portText = portField.getText().trim();
        
        if (server.isEmpty() || portText.isEmpty()) {
            showMessage("Please enter server and port");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            
            showMessage("Connecting to " + server + ":" + port + "...");
            
            socket = new Socket(server, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            connected = true;
            updateUI();
            
            showMessage("Connected to chat server!");
            
            // Start message listener thread
            new Thread(this::listenForMessages).start();
            
        } catch (NumberFormatException e) {
            showMessage("Invalid port number");
        } catch (IOException e) {
            showMessage("Connection failed: " + e.getMessage());
        }
    }
    
    private void disconnect() {
        connected = false;
        
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
        
        Platform.runLater(() -> {
            updateUI();
            showMessage("Disconnected from server");
        });
    }
    
    private void updateUI() {
        if (connected) {
            connectButton.setText("Disconnect");
            statusLabel.setText("Connected");
            statusLabel.setStyle("-fx-text-fill: green;");
            messageField.setDisable(false);
            sendButton.setDisable(false);
            serverField.setDisable(true);
            portField.setDisable(true);
            messageField.requestFocus();
        } else {
            connectButton.setText("Connect");
            statusLabel.setText("Not connected");
            statusLabel.setStyle("-fx-text-fill: red;");
            messageField.setDisable(true);
            sendButton.setDisable(true);
            serverField.setDisable(false);
            portField.setDisable(false);
        }
    }
    
    private void sendMessage() {
        if (!connected || writer == null) return;
        
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;
        
        // Send message to server
        writer.println(message);
        
        // Display in chat area (your own message)
        Platform.runLater(() -> {
            chatArea.appendText("You: " + message + "\n");
            messageField.clear();
        });
    }
    
    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = reader.readLine()) != null) {
                final String finalMessage = message;
                Platform.runLater(() -> {
                    chatArea.appendText(finalMessage + "\n");
                    // Auto-scroll to bottom
                    chatArea.setScrollTop(Double.MAX_VALUE);
                });
            }
        } catch (IOException e) {
            if (connected) {
                Platform.runLater(() -> {
                    showMessage("Connection lost: " + e.getMessage());
                    disconnect();
                });
            }
        }
    }
    
    private void showMessage(String message) {
        Platform.runLater(() -> {
            chatArea.appendText("*** " + message + " ***\n");
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 