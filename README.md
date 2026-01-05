# SimpleHeartRateMonitor - Windows BLE helper integration

This repository contains a small .NET helper (tools/ble-windows) and a Java BluetoothService
that starts the helper and consumes JSON lines with heart rate measurements. The values are
logged using the existing CsvDataLogger (bpm_log.csv).

## Overview
- tools/ble-windows: .NET 6 app using Windows.Devices.Bluetooth to subscribe to the
  Heart Rate Measurement characteristic (UUID 0x2A37) and print JSON lines:
  {"timestamp":"2026-01-05T12:34:56.789Z","bpm":72}
- src/main/java/com/simplehrm/infra/BluetoothService.java: Java service that launches the
  helper (or dotnet run), parses JSON using Gson, and forwards bpm values to CsvDataLogger.

## Build & publish the Windows helper
(Requires Windows + .NET 6+ SDK)

1. Publish a single-file, self-contained executable:

   dotnet publish -c Release -r win-x64 -p:PublishSingleFile=true --self-contained true -o tools/ble-windows/publish tools/ble-windows

2. Copy the produced exe (e.g. `ble-windows.exe`) to the Java application's working directory
   and optionally rename it to `ble-helper.exe` so the Java BluetoothService will pick it up
   automatically.

## Run without publishing (developer)

On a Windows dev machine with .NET SDK installed, the Java service will fall back to:

  dotnet run --project tools/ble-windows

which requires the SDK and a compatible environment.

## Java usage example

Example snippet showing how to start the service, receive callbacks and log to CSV:

```java
CsvDataLogger csv = new CsvDataLogger();
csv.start();
BluetoothService bt = new BluetoothService(csv);

bt.addListener((bpm, ts) -> {
    System.out.println("Got BPM: " + bpm + " ts=" + ts);
});

bt.start();

// ... application runs; when stopping:
bt.stop();
csv.stop();
```

## Troubleshooting
- Ensure Bluetooth is enabled and the adapter supports BLE.
- If no devices are found, verify the sensor advertises the Heart Rate service (0x180D).
- For deployment, prefer publishing a self-contained exe so the target system does not need
  the .NET SDK installed.
