package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;


/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {

        //get the last 24 hours of records in milliseconds
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (24 * 60 * 60 * 1000);
        List<PatientRecord> records = dataStorage.getRecords(patient.getPatientId(), startTime, endTime);

        checkBloodPressureAlerts(patient, records);
        checkBloodSaturationAlerts(patient, records);
        checkHypotensiveHypoxemiaAlert(patient, records);
        checkEcgAlerts(patient, records);
        checkTriggeredAlerts(patient, records);
        // Implementation goes here
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("ALERT triggered for patient: " + alert.getPatientId() + " Condition: " + alert.getCondition() + "  Timestamp: " + alert.getTimestamp());

        // Implementation might involve logging the alert or notifying staff
    }


    private void checkBloodPressureAlerts(Patient patient, List<PatientRecord> records) {

        List<PatientRecord> systolic = new ArrayList<>();
        List<PatientRecord> diastolic = new ArrayList<>();
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("SystolicPressure")) systolic.add(r);
            if (r.getRecordType().equals("DiastolicPressure")) diastolic.add(r);
        }

        checkBpTrend(patient, systolic, "Systolic");
        checkBpTrend(patient, diastolic, "Diastolic");

        //check for critical threshold for systolic (above 180 or below 90)
        for (PatientRecord r : systolic) {
            if (r.getMeasurementValue() > 180) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Systolic BP too high: " + r.getMeasurementValue(), r.getTimestamp()));
            } else if (r.getMeasurementValue() < 90) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Systolic BP too low: " + r.getMeasurementValue(), r.getTimestamp()));

            }
        }

        //check critical threshold for diastolic (above 120 or below 60)
        for (PatientRecord r : diastolic) {
            if (r.getMeasurementValue() > 120) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Diastolic BP too high: " + r.getMeasurementValue(), r.getTimestamp()));
            } else if (r.getMeasurementValue() < 60) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Diastolic BP too low: " + r.getMeasurementValue(), r.getTimestamp()));
            }

        }

    }


    //check if 3 consecutive readings each change by more than 10 mmHg in the same direction
    private void checkBpTrend(Patient patient, List<PatientRecord> records, String type) {
        if (records.size() < 3) {
            return;
        }

        for (int i = 2; i < records.size(); i++) {
            double first = records.get(i - 2).getMeasurementValue();
            double second = records.get(i - 1).getMeasurementValue();
            double third = records.get(i).getMeasurementValue();

            //each step has to increase by more than 10
            boolean increasing = (second - first > 10) && (third - second > 10);
            //each step has to decrease by more than 10
            boolean decreasing = (first - second > 10) && (second - third > 10);

            if (increasing) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), type + " BP increasing trend detected", records.get(i).getTimestamp()));
            } else if (decreasing) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                        type + " BP decreasing trend detected", records.get(i).getTimestamp()));
            }
        }
    }

    // checks blood oxygen saturation for low levels and quick drops
    private void checkBloodSaturationAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> satRecords = new ArrayList<>();
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("Saturation")) satRecords.add(r);
        }

        for (int i = 0; i < satRecords.size(); i++) {
            double value = satRecords.get(i).getMeasurementValue();

            // alert if saturation drops below 92%
            if (value < 92) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                        "Low blood saturation: " + value + "%", satRecords.get(i).getTimestamp()));
            }

            // check if saturation drops 5% or more within 10 minutes
            for (int j = i + 1; j < satRecords.size(); j++) {
                long timeDiff = satRecords.get(j).getTimestamp() - satRecords.get(i).getTimestamp();
                if (timeDiff > 10 * 60 * 1000) break; // stop if outside 10 minute window
                double drop = value - satRecords.get(j).getMeasurementValue();
                if (drop >= 5) {
                    triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                            "Rapid saturation drop of " + drop + "% in 10 minutes",
                            satRecords.get(j).getTimestamp()));
                }
            }
        }
    }

    // triggers alert if patient has BOTH low systolic BP and low blood oxygen at the same time
// this combination is called hypotensive hypoxemia and is very dangerous
    private void checkHypotensiveHypoxemiaAlert(Patient patient, List<PatientRecord> records) {
        boolean lowSystolic = false;
        boolean lowSaturation = false;

        for (PatientRecord r : records) {
            if (r.getRecordType().equals("SystolicPressure") && r.getMeasurementValue() < 90) {
                lowSystolic = true;
            }
            if (r.getRecordType().equals("Saturation") && r.getMeasurementValue() < 92) {
                lowSaturation = true;
            }
        }

        // only trigger if both conditions are true at the same time
        if (lowSystolic && lowSaturation) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia Alert - low BP and low oxygen detected",
                    System.currentTimeMillis()));
        }
    }

    //checks ECG data for abnormal peaks using a sliding window average
    // assumption: a peak is abnormal if it is more than double the recent average
    private void checkEcgAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecgRecords = new ArrayList<>();
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("ECG")) ecgRecords.add(r);
        }

        if (ecgRecords.size() < 10) return; // need enough data for a meaningful average

        int windowSize = 10; // look at last 10 readings to calculate average

        for (int i = windowSize; i < ecgRecords.size(); i++) {
            // calculate average of the last 10 readings
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double average = sum / windowSize;
            double current = ecgRecords.get(i).getMeasurementValue();

            // if current value is more than double the average, its an abnormal peak
            if (average > 0 && current > average * 2) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                        "Abnormal ECG peak detected: " + current + " vs average " + average,
                        ecgRecords.get(i).getTimestamp()));
            }
        }
    }

    // handles manual alerts triggered by nurses or patients pressing the alert button
    private void checkTriggeredAlerts(Patient patient, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            // value of 1 means triggered, 0 means resolved
            if (r.getRecordType().equals("Alert") && r.getMeasurementValue() == 1) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                        "Manual alert triggered by patient or nurse", r.getTimestamp()));
            }
        }
    }

}

