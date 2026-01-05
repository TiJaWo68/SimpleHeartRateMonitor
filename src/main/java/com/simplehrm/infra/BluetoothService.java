package com.simplehrm.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BluetoothService starts a native Windows BLE helper (dotnet or compiled exe) that streams
 * JSON lines with heart rate measurements. Each line is parsed and written to CsvDataLogger.
 *
 * Usage:
 *   BluetoothService svc = new BluetoothService(new CsvDataLogger());
 *   svc.start();
 *   ... svc.stop();
 */
public class BluetoothService {
    private static final Logger logger = LoggerFactory.getLogger(BluetoothService.class);
    private final CsvDataLogger csvLogger;
    private Process process;
    private Thread readerThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public BluetoothService(CsvDataLogger csvLogger) {
        this.csvLogger = csvLogger;
    }

    /**
     * Start the BLE helper. It will try to run a local "ble-helper.exe" if present,
     * otherwise it will run `dotnet run --project tools/ble-windows` (requires dotnet SDK).
     */
    public synchronized void start() throws IOException {
        if (running.get()) {
            logger.info("BluetoothService already running");
            return;
        }

        List<String> command = new ArrayList<>();
        if (new File("ble-helper.exe").exists()) {
            command.add("ble-helper.exe");
        } else {
            command.add("dotnet");
            command.add("run");
            command.add("--project");
            command.add("tools/ble-windows");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        process = pb.start();
        running.set(true);

        readerThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while (running.get() && (line = br.readLine()) != null) {
                    handleLine(line);
                }
            } catch (IOException e) {
                logger.error("Error reading BLE helper output", e);
            } finally {
                running.set(false);
            }
        }, "BLE-stdout-reader");

        readerThread.setDaemon(true);
        readerThread.start();
        logger.info("BluetoothService started (command: {})", String.join(" ", command));
    }

    private void handleLine(String line) {
        if (line == null || line.isEmpty()) return;
        logger.debug("BLE helper: {}", line);
        try {
            int idx = line.indexOf("\"bpm\"");
            if (idx != -1) {
                int colon = line.indexOf(':', idx);
                if (colon != -1) {
                    int i = colon + 1;
                    // skip non-digit characters
                    while (i < line.length() && !Character.isDigit(line.charAt(i)) && line.charAt(i) != '-') i++;
                    int start = i;
                    while (i < line.length() && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')) i++;
                    if (start < i) {
                        String numStr = line.substring(start, i);
                        int bpm = Integer.parseInt(numStr);
                        csvLogger.log(bpm);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse BPM from line: {}", line, e);
        }
    }

    public synchronized void stop() {
        running.set(false);
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
        logger.info("BluetoothService stopped");
    }

    public boolean isRunning() {
        return running.get();
    }
}
