package threadnetwork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class WebSocketClient extends Thread {
    private String host;
    private int port;
    private String path;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean connected = false;
    private CountDownLatch connectLatch = new CountDownLatch(1);
    private MessageHandler messageHandler;

    public interface MessageHandler {
        void onMessage(String message);
        void onClose();
        void onError(Exception ex);
    }

    public WebSocketClient(String host, int port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
    }

    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    public void connect() {
        this.start();
        try {
            connectLatch.await();
        } catch (InterruptedException e) {
            if (messageHandler != null) {
                messageHandler.onError(e);
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        try {
            // Create socket
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            
            // Perform WebSocket handshake
            performHandshake();
            
            // Mark as connected
            connected = true;
            connectLatch.countDown();
            
            // Start message listening loop
            listenForMessages();
            
        } catch (Exception e) {
            if (messageHandler != null) {
                messageHandler.onError(e);
            }
            connectLatch.countDown();
        }
    }

    private void performHandshake() throws IOException, NoSuchAlgorithmException {
        // Generate random WebSocket key
        byte[] keyBytes = new byte[16];
        new SecureRandom().nextBytes(keyBytes);
        String key = Base64.getEncoder().encodeToString(keyBytes);
        
        // Create handshake request
        String handshake =
            "GET " + path + " HTTP/1.1\r\n" +
            "Host: " + host + ":" + port + "\r\n" +
            "Upgrade: websocket\r\n" +
            "Connection: Upgrade\r\n" +
            "Sec-WebSocket-Key: " + key + "\r\n" +
            "Sec-WebSocket-Version: 13\r\n" +
            "\r\n";
        
        // Send handshake
        out.write(handshake.getBytes(StandardCharsets.UTF_8));
        
        // Read response
        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
        String response = scanner.useDelimiter("\\r\\n\\r\\n").next();
        
        // Verify response contains "101 Switching Protocols"
        if (!response.contains("101 Switching Protocols")) {
            throw new IOException("Invalid handshake response: " + response);
        }
        
        System.out.println("WebSocket handshake completed");
    }

    private void listenForMessages() {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                if (bytesRead == 0) continue;
                
                // Check for WebSocket frame
                byte firstByte = buffer[0];
                boolean fin = (firstByte & 0x80) != 0;
                int opcode = firstByte & 0x0F;
                
                // Check second byte for mask and payload length
                byte secondByte = buffer[1];
                boolean masked = (secondByte & 0x80) != 0;
                int payloadLength = secondByte & 0x7F;
                
                int maskingKeyOffset;
                int dataOffset;
                
                if (payloadLength <= 125) {
                    maskingKeyOffset = 2;
                } else if (payloadLength == 126) {
                    // 16-bit length
                    maskingKeyOffset = 4;
                } else {
                    // 64-bit length
                    maskingKeyOffset = 10;
                }
                
                dataOffset = masked ? maskingKeyOffset + 4 : maskingKeyOffset;
                
                // Handle different opcodes
                switch (opcode) {
                    case 0x8: // Close
                        System.out.println("Received close frame");
                        if (messageHandler != null) {
                            messageHandler.onClose();
                        }
                        return; // Exit the method to close the connection
                        
                    case 0x1: // Text frame
                        // Create array for the payload
                        byte[] payloadData = new byte[bytesRead - dataOffset];
                        System.arraycopy(buffer, dataOffset, payloadData, 0, bytesRead - dataOffset);
                        
                        // Unmask if needed
                        if (masked) {
                            byte[] maskingKey = new byte[4];
                            System.arraycopy(buffer, maskingKeyOffset, maskingKey, 0, 4);
                            
                            for (int i = 0; i < payloadData.length; i++) {
                                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
                            }
                        }
                        
                        String message = new String(payloadData, StandardCharsets.UTF_8);
                        
                        if (messageHandler != null) {
                            messageHandler.onMessage(message);
                        } else {
                            System.out.println("Received message: " + message);
                        }
                        break;
                        
                    case 0x9: // Ping
                        // Send pong
                        sendPong(buffer, bytesRead);
                        break;
                        
                    default:
                        System.out.println("Received frame with opcode: " + opcode);
                }
            }
            
        } catch (IOException e) {
            if (connected && messageHandler != null) {
                messageHandler.onError(e);
            }
        } finally {
            close();
        }
    }
    
    private void sendPong(byte[] pingData, int length) throws IOException {
        // Create pong frame by modifying ping frame
        pingData[0] = (byte) (pingData[0] & 0xF0 | 0x0A); // Change opcode to pong (0x0A)
        out.write(pingData, 0, length);
    }

    public void sendMessage(String message) throws IOException {
        if (!connected) {
            throw new IOException("WebSocket is not connected");
        }
        
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        int payloadLength = payload.length;
        
        // Generate random mask key
        byte[] maskKey = new byte[4];
        new SecureRandom().nextBytes(maskKey);
        
        // Create WebSocket frame
        ByteArrayOutputStream frameBytes = new ByteArrayOutputStream();
        
        // First byte: FIN bit set, text frame opcode
        frameBytes.write(0x81);
        
        // Second byte: mask bit set + payload length
        if (payloadLength <= 125) {
            frameBytes.write(0x80 | payloadLength);
        } else if (payloadLength <= 65535) {
            frameBytes.write(0x80 | 126);
            frameBytes.write((payloadLength >> 8) & 0xFF);
            frameBytes.write(payloadLength & 0xFF);
        } else {
            frameBytes.write(0x80 | 127);
            // Write 8 bytes for payload length
            for (int i = 7; i >= 0; i--) {
                frameBytes.write((payloadLength >> (i * 8)) & 0xFF);
            }
        }
        
        // Write masking key
        frameBytes.write(maskKey);
        
        // Write masked payload
        for (int i = 0; i < payloadLength; i++) {
            frameBytes.write(payload[i] ^ maskKey[i % 4]);
        }
        
        out.write(frameBytes.toByteArray());
        out.flush();
    }

    public void close() {
        if (connected) {
            connected = false;
            
            try {
                // Send close frame
                byte[] closeFrame = {(byte) 0x88, 0x00};
                out.write(closeFrame);
                out.flush();
                
                // Close socket
                socket.close();
                System.out.println("WebSocket closed");
            } catch (IOException e) {
                System.err.println("Error closing WebSocket: " + e.getMessage());
            }
        }
    }
} 