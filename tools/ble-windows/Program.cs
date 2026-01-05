using System;
using System.Linq;
using System.Threading.Tasks;
using Windows.Devices.Bluetooth;
using Windows.Devices.Bluetooth.Advertisement;
using Windows.Devices.Bluetooth.GenericAttributeProfile;
using Windows.Storage.Streams;

class Program
{
    static async Task Main(string[] args)
    {
        var watcher = new BluetoothLEAdvertisementWatcher();
        watcher.ScanningMode = BluetoothLEScanningMode.Active;

        watcher.Received += async (w, btAdv) =>
        {
            try
            {
                var adv = btAdv.Advertisement;
                // Only proceed when the advertisement advertises the Heart Rate service (0x180D)
                if (!adv.ServiceUuids.Contains(GattServiceUuids.HeartRate)) return;

                ulong address = btAdv.BluetoothAddress;
                var device = await BluetoothLEDevice.FromBluetoothAddressAsync(address);
                if (device == null) return;

                var servicesResult = await device.GetGattServicesForUuidAsync(GattServiceUuids.HeartRate);
                if (servicesResult.Status != GattCommunicationStatus.Success) return;
                var service = servicesResult.Services.FirstOrDefault();
                if (service == null) return;

                // Heart Rate Measurement characteristic UUID: 00002a37-0000-1000-8000-00805f9b34fb
                var hrCharGuid = new Guid("00002a37-0000-1000-8000-00805f9b34fb");
                var charsResult = await service.GetCharacteristicsForUuidAsync(hrCharGuid);
                if (charsResult.Status != GattCommunicationStatus.Success) return;
                var hrChar = charsResult.Characteristics.FirstOrDefault();
                if (hrChar == null) return;

                hrChar.ValueChanged += (s, ev) =>
                {
                    try
                    {
                        var reader = DataReader.FromBuffer(ev.CharacteristicValue);
                        byte flags = reader.ReadByte();
                        int bpm;
                        if ((flags & 0x01) == 0)
                        {
                            bpm = reader.ReadByte();
                        }
                        else
                        {
                            bpm = reader.ReadUInt16();
                        }

                        Console.WriteLine($"{{\"timestamp\":\"{DateTime.UtcNow:o}\",\"bpm\":{bpm}}}");
                        Console.Out.Flush();
                    }
                    catch (Exception ex)
                    {
                        Console.Error.WriteLine(ex);
                    }
                };

                await hrChar.WriteClientCharacteristicConfigurationDescriptorAsync(
                    GattClientCharacteristicConfigurationDescriptorValue.Notify);
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine(ex);
            }
        };

        watcher.Start();
        await Task.Delay(-1);
    }
}
