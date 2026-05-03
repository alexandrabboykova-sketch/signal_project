package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * sends patient health data to a client over TCP connection.
 * Starts a server on a given port and waits for a client to connect,
 * then streams data to that client as it is generated.
 */

public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * starts a TCP server on the given part and waits for a client to connect
     * The connection is handled is a separate thread so the simulated
     *
     * @param port the port number to start the TCP server on
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sends a patient's health data to the connected TCP client.
     * Does nothing if no client is connected yet.
     *
     * @param patientId the ID of the patient the data belongs to
     * @param timestamp the time the data was generated, in milliseconds
     * @param label the type of data
     * @param data the actual value of the health measurement
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
