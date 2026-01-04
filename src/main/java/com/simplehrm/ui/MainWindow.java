package com.simplehrm.ui;

import com.simplehrm.core.BluetoothService;
import com.simplehrm.core.Device;
import com.simplehrm.core.HeartRateListener;
import com.simplehrm.infra.CsvDataLogger;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame implements HeartRateListener {
    private final BluetoothService bluetoothService;
    private final CsvDataLogger dataLogger;

    private DefaultListModel<Device> deviceListModel;
    private JList<Device> deviceList;
    private JLabel bpmLabel;
    private JButton scanButton;
    private JButton connectButton;
    private JButton disconnectButton;

    public MainWindow(BluetoothService bluetoothService, CsvDataLogger dataLogger) {
        this.bluetoothService = bluetoothService;
        this.dataLogger = dataLogger;

        setTitle("Simple Heart Rate Monitor");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Main Layout
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Device Discovery
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Bluetooth Devices"));

        deviceListModel = new DefaultListModel<>();
        deviceList = new JList<>(deviceListModel);
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(deviceList);
        scrollPane.setPreferredSize(new Dimension(0, 150));

        topPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        scanButton = new JButton("Scan");
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        connectButton.setEnabled(false);

        buttonPanel.add(scanButton);
        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel: BPM Display
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Heart Rate"));
        bpmLabel = new JLabel("-- BPM");
        bpmLabel.setFont(new Font("Arial", Font.BOLD, 48));
        centerPanel.add(bpmLabel);

        add(centerPanel, BorderLayout.CENTER);

        // Event Listeners
        scanButton.addActionListener(e -> startScan());
        connectButton.addActionListener(e -> connectToDevice());
        disconnectButton.addActionListener(e -> disconnectDevice());

        deviceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                connectButton.setEnabled(deviceList.getSelectedValue() != null);
            }
        });
    }

    private void startScan() {
        scanButton.setEnabled(false);
        deviceListModel.clear();

        bluetoothService.scanForDevices().thenAccept(devices -> SwingUtilities.invokeLater(() -> {
            for (Device device : devices) {
                deviceListModel.addElement(device);
            }
            scanButton.setEnabled(true);
        })).exceptionally(e -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Scan failed: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                scanButton.setEnabled(true);
            });
            return null;
        });
    }

    private void connectToDevice() {
        Device selected = deviceList.getSelectedValue();
        if (selected == null)
            return;

        scanButton.setEnabled(false);
        connectButton.setEnabled(false);
        deviceList.setEnabled(false);

        dataLogger.start();
        bluetoothService.connect(selected, this);

        disconnectButton.setEnabled(true);
    }

    private void disconnectDevice() {
        bluetoothService.disconnect();
        dataLogger.stop();

        disconnectButton.setEnabled(false);
        scanButton.setEnabled(true);
        connectButton.setEnabled(true);
        deviceList.setEnabled(true);
        bpmLabel.setText("-- BPM");
    }

    @Override
    public void onHeartRateUpdate(int bpm) {
        SwingUtilities.invokeLater(() -> {
            bpmLabel.setText(bpm + " BPM");
            dataLogger.log(bpm);
        });
    }
}
