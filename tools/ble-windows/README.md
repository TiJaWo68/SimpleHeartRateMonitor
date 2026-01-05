# BLE Windows helper

This small .NET 6 app uses the Windows Bluetooth LE WinRT APIs to discover a Heart Rate (0x180D) device,
subscribe to the Heart Rate Measurement characteristic (0x2A37) and print JSON lines to stdout:

{"timestamp":"2026-01-05T12:34:56.789Z","bpm":72}

Build & run (requires .NET 6+ SDK on Windows 10/11):

dotnet run --project tools/ble-windows

Recommended: publish a single-file executable for distribution:

dotnet publish -c Release -r win-x64 -p:PublishSingleFile=true --self-contained true -o tools/ble-windows/publish

Place the produced `ble-windows.exe` alongside the Java application or name it `ble-helper.exe` so the Java BluetoothService will pick it up automatically.
