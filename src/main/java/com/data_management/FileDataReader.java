package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;


/**
 * This class implements DataReader to load data into DataStorage
 * reads patient data from text files
 * each line in the files should follow the format:
 * patientId, measurementValue, recordType, timeStamp
 */

public class FileDataReader implements DataReader {

    private String directory;

    public FileDataReader(String directory) {
        this.directory = directory;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        File folder = new File(directory);
        File[] files = folder.listFiles();

        //if folder is empty or doesn't exist, just stop
        if (files == null) {
            return;
        }
        for (File file : files) {
            readFile(file.toPath(), dataStorage);
        }
    }

    private void readFile(Path path, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, dataStorage);
            }
        }
    }

    private void parseLine(String line, DataStorage dataStorage) {
        try {
            String[] parts = line.split(",");
            if (parts.length != 4) return;

            int patientId = Integer.parseInt(parts[0].trim());
            double measurementValue = Double.parseDouble(parts[1].trim());
            String recordType = parts[2].trim();
            long timestamp = Long.parseLong(parts[3].trim());

            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
        }catch (NumberFormatException e) {
            //skip lines that can't be parsed
            System.err.println("Error parsing line " + line);
        }
    }


}
