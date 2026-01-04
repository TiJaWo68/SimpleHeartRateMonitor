package com.simplehrm.infra;

import com.simplehrm.core.Device;
import com.simplehrm.core.HeartRateListener;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MockBluetoothServiceTest {

    @Test
    void testScanReturnsDevices() throws ExecutionException, InterruptedException {
        MockBluetoothService service = new MockBluetoothService();
        CompletableFuture<List<Device>> future = service.scanForDevices();

        List<Device> devices = future.get();
        assertNotNull(devices);
        assertFalse(devices.isEmpty());
        assertEquals("Mock HR Strap 1", devices.get(0).getName());
    }

    @Test
    void testConnectReceivesUpdates() throws InterruptedException {
        MockBluetoothService service = new MockBluetoothService();
        Device device = new Device("Test", "00:00");

        AtomicInteger updateCount = new AtomicInteger(0);
        HeartRateListener listener = bpm -> {
            updateCount.incrementAndGet();
            System.out.println("Received: " + bpm);
        };

        service.connect(device, listener);

        // Wait for 3 seconds to receive ~3 updates
        Thread.sleep(3500);

        service.disconnect();

        assertTrue(updateCount.get() >= 2, "Should receive at least 2 updates in 3.5 seconds");
    }
}
