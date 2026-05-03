package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating patient health data.
 * Any class that generates health data must implement this interface
 */
public interface   PatientDataGenerator {

    /**
     * Generates health data for a single patient and sends it to the output
     * @param patientId the ID of the patient to generate data for
     * @param outputStrategy where to send  the generated data (console, file, etc)
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
