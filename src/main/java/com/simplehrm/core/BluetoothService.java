package com.simplehrm.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BluetoothService {
    /**
     * Scans for available BLE devices.
     * 
     * @return A list of found devices.
     */
    CompletableFuture<List<Device>> scanForDevices();

    /**
     * Connects to a specific device and starts listening for Heart Rate updates.
     * 
     * @param device   The device to connect to.
     * @param listener The listener to receive BPM updates.
     */
    void connect(Device device, HeartRateListener listener);

    /**
     * Disconnects from the current device.
     */
    void disconnect();
}
