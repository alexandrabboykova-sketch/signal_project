package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * saves patient health data to text files
 * each type of data gets its own file inside the specified output
 * directory
 */
//renamed fileOutputStrategy to FileOutputStrategy to follow class naming rule
public class FileOutputStrategy implements OutputStrategy {

    //renamed BaseDirectory to baseDirectory to follow naming rule for a local variable
    private String baseDirectory;

    //renamed file_map to fileMap to follow camelCase naming rule
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * creates a FileOutputStrategy that saves files to the given directory
     *
     * @param baseDirectory the folder where output files will be saved
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes a patient's health data to a text file.
     * if the file does not exist yet it will be created automatically
     * each data type gets its own separate file
     *
     * @param patientId the ID of the patient the data belongs to
     * @param timestamp the time the data was generated, in milliseconds
     * @param label the type of data
     * @param data the actual value of the health measurement
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        //renamed FilePath to filePath to follow naming rule for a local variable
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}