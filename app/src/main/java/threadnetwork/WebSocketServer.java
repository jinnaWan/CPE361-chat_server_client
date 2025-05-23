package threadnetwork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;
    private int timeout = 60000; // Default timeout: 60 seconds

    public WebSocketServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(timeout);
            this.start();
            running = true;
            System.out.println("WebSocket server started on port: " + port);
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    public void stopServer() {
        running = false;
        this.interrupt();
        try {
            serverSocket.close();
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.setSoTimeout(timeout);
            } catch (SocketException e) {
                System.err.println("Error setting timeout: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Step 3: Accept a connection and create a socket
                System.out.println("Waiting for client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                
                // Handle client in a new thread
                new Thread(() -> processClient(clientSocket)).start();
                
            } catch (SocketTimeoutException e) {
                System.out.println("Server socket timed out waiting for connection");
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    private void processClient(Socket clientSocket) {
        try {
            // Step 4: Process socket
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            
            // Read HTTP request for WebSocket handshake
            Scanner s = new Scanner(in, "UTF-8");
            String data = s.useDelimiter("\\r\\n\\r\\n").next();
            
            // Extract the WebSocket key from headers
            Matcher get = Pattern.compile("^GET").matcher(data);
            if (get.find()) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                match.find();
                String key = match.group(1);
                
                // Perform WebSocket handshake
                String acceptKey = generateAcceptKey(key);
                String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                                  "Upgrade: websocket\r\n" +
                                  "Connection: Upgrade\r\n" +
                                  "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
                
                out.write(response.getBytes("UTF-8"));
                System.out.println("WebSocket handshake completed");
                
                // Handle WebSocket communication
                handleWebSocketCommunication(in, out);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing client: " + e.getMessage());
        } finally {
            // Step 5: Close socket
            try {
                clientSocket.close();
                System.out.println("Client connection closed");
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private String generateAcceptKey(String key) throws NoSuchAlgorithmException {
        String websocketMagicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String concatenatedKey = key + websocketMagicString;
        
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(concatenatedKey.getBytes());
        
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    private void handleWebSocketCommunication(InputStream in, OutputStream out) throws IOException {
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
                    return; // Exit the method to close the connection
                    
                case 0x1: // Text frame
                    byte[] maskingKey = new byte[4];
                    System.arraycopy(buffer, maskingKeyOffset, maskingKey, 0, 4);
                    
                    // Unmask the data
                    byte[] payloadData = new byte[bytesRead - dataOffset];
                    System.arraycopy(buffer, dataOffset, payloadData, 0, bytesRead - dataOffset);
                    
                    if (masked) {
                        for (int i = 0; i < payloadData.length; i++) {
                            payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
                        }
                    }
                    
                    String message = new String(payloadData);
                    System.out.println("Received message: " + message);
                    
                    // Echo the message back to client
                    sendMessage(out, "Echo: " + message);
                    break;
                    
                case 0x9: // Ping
                    // Respond with pong
                    buffer[0] = (byte) 0x8A; // FIN bit set, opcode for pong
                    out.write(buffer, 0, bytesRead);
                    break;
                    
                default:
                    System.out.println("Received frame with opcode: " + opcode);
            }
        }
    }

    private void sendMessage(OutputStream out, String message) throws IOException {
        byte[] payload = message.getBytes();
        int payloadLength = payload.length;
        
        // Create WebSocket frame
        ByteArrayOutputStream frameBytes = new ByteArrayOutputStream();
        
        // First byte: FIN bit set, text frame opcode
        frameBytes.write(0x81);
        
        // Second byte: payload length
        if (payloadLength <= 125) {
            frameBytes.write(payloadLength);
        } else if (payloadLength <= 65535) {
            frameBytes.write(126);
            frameBytes.write((payloadLength >> 8) & 0xFF);
            frameBytes.write(payloadLength & 0xFF);
        } else {
            frameBytes.write(127);
            // Write 8 bytes for payload length
            for (int i = 7; i >= 0; i--) {
                frameBytes.write((payloadLength >> (i * 8)) & 0xFF);
            }
        }
        
        // Write payload
        frameBytes.write(payload);
        
        out.write(frameBytes.toByteArray());
        out.flush();
    }
} 