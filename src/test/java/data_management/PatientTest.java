package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PatientTest {

    @Test
    void testGetRecordsInRange() {
        //should return only records within the time range
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "HeartRate", 1000);
        patient.addRecord(200.0, "HeartRate", 2000);
        patient.addRecord(300.0, "HeartRate", 3000);

        List<PatientRecord> records = patient.getRecords(1000, 2000);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecordsEmptyRange() {
        // should return nothing if no records in that range
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "HeartRate", 1000);

        List<PatientRecord> records = patient.getRecords(5000, 6000);
        assertEquals(0, records.size());
    }

    @Test
    void testGetRecordsExactBoundary() {
        // records exactly on the boundary should be included
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "HeartRate", 1000);
        patient.addRecord(200.0, "HeartRate", 2000);

        List<PatientRecord> records = patient.getRecords(1000, 2000);
        assertEquals(2, records.size());
    }

}
