package com.cardio_generator;

import com.data_management.DataStorage;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            DataStorage.main(new String[]{});
        } else {
            try{
                HealthDataSimulator.main(args);
            } catch (IOException e) {
                System.err.println("Error running simulator: " + e.getMessage());
            }
        }
    }
}

