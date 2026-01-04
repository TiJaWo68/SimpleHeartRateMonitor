package com.simplehrm.core;

import com.simplehrm.infra.CsvDataLogger;
import com.simplehrm.infra.MockBluetoothService;
import com.simplehrm.ui.MainWindow;

import javax.swing.*;

public class HeartRateMonitorApp {
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            BluetoothService bluetoothService = new MockBluetoothService();
            CsvDataLogger logger = new CsvDataLogger();

            MainWindow mainWindow = new MainWindow(bluetoothService, logger);
            mainWindow.setVisible(true);
        });
    }
}
