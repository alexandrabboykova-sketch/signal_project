package com.cardio_generator.outputs;

/**
 * interface for handling the output of generated patient data.
 * any class that wants to output data must implement this interface
 */
public interface OutputStrategy {
    /**
     * outputs a single piece of patient health data.
     *
     * @param patientId the ID of the patient the data belongs to
     * @param timestamp the time the data was generated, in milliseconds
     * @param label the type of data
     * @param data the actual value of the health measurement
     */
    void output(int patientId, long timestamp, String label, String data);
}
