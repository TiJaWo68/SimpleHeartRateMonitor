package com.simplehrm.infra;

/**
 * Listener interface for receiving measurements from BluetoothService.
 */
public interface BluetoothListener {
    /**
     * Called when a new measurement is received.
     * @param bpm measured beats per minute
     * @param timestamp optional ISO 8601 timestamp from the helper, may be null
     */
    void onMeasurement(int bpm, String timestamp);
}
