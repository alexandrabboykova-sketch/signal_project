package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * generates alert data for patients
 * randomly triggers and resolves alerts to simulate real medical alerts,
 * such as a patient pressing an emergency button
 */

public class AlertGenerator implements PatientDataGenerator {
    //renamed randomGenerator to all caps to follow the naming rule for static final field (constants)
    public static final Random RANDOM_GENERATOR = new Random();
    //renamed AlertStates to alertStates to follow lowerCamelCase for local variables / non-constant
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * sets up the alert generator for the given number of patients
     *      * all patients start with no active alerts
     * @param patientCount the number of patients to track alerts for
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * generates an alert update for a patient
     * if an alert is active, there is a 90% chance it gets resolved
     * if no alert is active, there is a small random chance one gets triggered
     *
     * @param patientId the ID of the patient to generate data for
     * @param outputStrategy where to send  the generated data (console, file, etc)
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                //renamed Lambda to lambda to follow lowerCamelCase for local variables
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
