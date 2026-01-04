package com.simplehrm.infra;

import com.simplehrm.core.BluetoothService;
import com.simplehrm.core.Device;
import com.simplehrm.core.HeartRateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class MockBluetoothService implements BluetoothService {
    private static final Logger logger = LoggerFactory.getLogger(MockBluetoothService.class);
    private ScheduledExecutorService scheduler;
    private final Random random = new Random();
    private Device currentDevice;

    @Override
    public CompletableFuture<List<Device>> scanForDevices() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000); // Simulate scanning delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            List<Device> devices = new ArrayList<>();
            devices.add(new Device("Mock HR Strap 1", "00:11:22:33:44:55"));
            devices.add(new Device("Mock HR Strap 2", "AA:BB:CC:DD:EE:FF"));
            return devices;
        });
    }

    @Override
    public void connect(Device device, HeartRateListener listener) {
        this.currentDevice = device;
        logger.info("Connecting to {}", device);

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Simulate heart rate updates every second
        scheduler.scheduleAtFixedRate(() -> {
            if (currentDevice != null) {
                int bpm = 60 + random.nextInt(40); // Random BPM between 60 and 100
                listener.onHeartRateUpdate(bpm);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void disconnect() {
        logger.info("Disconnecting from {}", currentDevice);
        currentDevice = null;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
