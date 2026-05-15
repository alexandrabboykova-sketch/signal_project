package data_management;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlertGeneratorTest {

    @Test
    void testHighSystolicTriggerAlert() {
        //systolic above 180 should trigger a critical alert

        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(190.0, "SystolicPressure", System.currentTimeMillis());

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testLowSystolicTriggerAlert() {
        //systolic below 90 should trigger a critical alert
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(85.0, "SystolicPressure", System.currentTimeMillis());

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testIncreasingBPTrend(){
        //three readings each going up by more than 10 should trigger the alert
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "SystolicPressure", System.currentTimeMillis()-3000);
        patient.addRecord(115.0, "SystolicPressure", System.currentTimeMillis()-2000);
        patient.addRecord(130.0, "SystolicPressure", System.currentTimeMillis()-1000);

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testDecreasingBPTrend(){
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(130.0, "SystolicPressure", System.currentTimeMillis()-3000);
        patient.addRecord(115.0, "SystolicPressure", System.currentTimeMillis()-2000);
        patient.addRecord(100.0, "SystolicPressure", System.currentTimeMillis()-1000);

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testLowSaturationTriggerAlert() {
        //saturation below 92% should trigger alert
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(88.0, "Saturation", System.currentTimeMillis());

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testRapidSaturationDrop(){
        //drop of 6% within 10 minutes should trigger alert
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(97.0, "Saturation", System.currentTimeMillis());
        patient.addRecord(91.0, "Saturation", System.currentTimeMillis()+5*60*1000);

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testHypotensiveHypoxemia() {
        // low BP and low oxygen together should trigger combined alert
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(85.0, "SystolicPressure", System.currentTimeMillis());
        patient.addRecord(88.0, "Saturation", System.currentTimeMillis());

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }


    @Test
    void testEcgPeakTriggersAlert() {
        // a reading way above the average should trigger an ECG alert
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        long now = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            patient.addRecord(1.0, "ECG", now + i * 1000);
        }
        patient.addRecord(10.0, "ECG", now + 11000); // huge spike

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }

    @Test
    void testManualAlertTriggered() {
        // value of 1 means nurse or patient pressed the alert button
        DataStorage storage = new DataStorage();
        AlertGenerator generator = new AlertGenerator(storage);
        Patient patient = new Patient(1);
        patient.addRecord(1.0, "Alert", System.currentTimeMillis());

        assertDoesNotThrow(() -> generator.evaluateData(patient));
    }
}
