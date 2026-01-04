package com.simplehrm.infra;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvDataLogger {
    private static final Logger logger = LoggerFactory.getLogger(CsvDataLogger.class);
    private static final String FILE_NAME = "bpm_log.csv";
    private CSVWriter writer;

    public void start() {
        try {
            // Append mode
            writer = new CSVWriter(new FileWriter(FILE_NAME, true));
        } catch (IOException e) {
            logger.error("Failed to initialize CSV writer", e);
        }
    }

    public void log(int bpm) {
        if (writer != null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String[] record = { timestamp, String.valueOf(bpm) };
            writer.writeNext(record);
            try {
                writer.flush(); // Ensure data is written immediately
            } catch (IOException e) {
                logger.error("Failed to flush CSV writer", e);
            }
        }
    }

    public void stop() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Failed to close CSV writer", e);
            }
        }
    }
}
