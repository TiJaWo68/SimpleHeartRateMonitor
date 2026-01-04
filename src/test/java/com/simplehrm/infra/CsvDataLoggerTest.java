package com.simplehrm.infra;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvDataLoggerTest {
    private static final String FILE_NAME = "bpm_log.csv";

    @BeforeEach
    void setup() throws IOException {
        Files.deleteIfExists(Path.of(FILE_NAME));
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Path.of(FILE_NAME));
    }

    @Test
    void testLogWritesToFile() throws IOException {
        CsvDataLogger logger = new CsvDataLogger();
        logger.start();
        logger.log(75);
        logger.stop();

        File file = new File(FILE_NAME);
        assertTrue(file.exists());

        List<String> lines = Files.readAllLines(file.toPath());
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("\"75\"")); // OpenCSV quotes values by default
    }
}
