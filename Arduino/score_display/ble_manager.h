
#include <ArduinoBLE.h>

namespace BLEManager
{
    //=========================================================================
    // GLOBALS
    //=========================================================================

    const char*                     deviceName = "ScoreDisplay";
    BLEService                      service("00000000-6f1e-40f5-909b-8e50c592dca9");
    BLEUnsignedCharCharacteristic   team1ScoreCharacteristic("10000000-6f1e-40f5-909b-8e50c592dca9", BLERead | BLEWrite | BLEWriteWithoutResponse);
    BLEUnsignedIntCharacteristic    team1ColorCharacteristic("10000001-6f1e-40f5-909b-8e50c592dca9", BLERead | BLEWrite | BLEWriteWithoutResponse);
    BLEUnsignedCharCharacteristic   team1BrightnessCharacteristic("10000002-6f1e-40f5-909b-8e50c592dca9", BLERead | BLEWrite | BLEWriteWithoutResponse);
    BLEUnsignedCharCharacteristic   team2ScoreCharacteristic("20000000-6f1e-40f5-909b-8e50c592dca9", BLERead | BLEWrite | BLEWriteWithoutResponse);
    BLEUnsignedIntCharacteristic    team2ColorCharacteristic("20000001-6f1e-40f5-909b-8e50c592dca9", BLERead | BLEWrite | BLEWriteWithoutResponse);
    BLEUnsignedCharCharacteristic   team2BrightnessCharacteristic("20000002-6f1e-40f5-909b-8e50c592dca9", BLERead | BLEWrite | BLEWriteWithoutResponse);

    char                            defaultScore = 0;
    int                             team1DefaultColor = 0xFF0000;
    int                             team2DefaultColor = 0x0000FF;
    char                            defaultBrightness = 16;

    void onDeviceConnected(BLEDevice device);
    void onDeviceDisconnected(BLEDevice device);
    void onTeam1ScoreChanged(BLEDevice device, BLECharacteristic characteristic);
    void onTeam1ColorChanged(BLEDevice device, BLECharacteristic characteristic);
    void onTeam1BrightnessChanged(BLEDevice device, BLECharacteristic characteristic);
    void onTeam2ScoreChanged(BLEDevice device, BLECharacteristic characteristic);
    void onTeam2ColorChanged(BLEDevice device, BLECharacteristic characteristic);
    void onTeam2BrightnessChanged(BLEDevice device, BLECharacteristic characteristic);

    //=========================================================================
    // PUBLIC INTERFACE
    //=========================================================================

    void begin()
    {
        pinMode(LED_BUILTIN, OUTPUT);
        if (!BLE.begin())
        {
            Serial.println("Failed to start BLE.");
            while (1);
        }

        digitalWrite(LED_BUILTIN, PinStatus::LOW);

        BLE.setEventHandler(BLEDeviceEvent::BLEConnected, &onDeviceConnected);
        BLE.setEventHandler(BLEDeviceEvent::BLEDisconnected, &onDeviceDisconnected);

        team1ScoreCharacteristic.setEventHandler(BLECharacteristicEvent::BLEWritten, &onTeam1ScoreChanged);
        team1ColorCharacteristic.setEventHandler(BLECharacteristicEvent::BLEWritten, &onTeam1ColorChanged);
        team1BrightnessCharacteristic.setEventHandler(BLECharacteristicEvent::BLEWritten, &onTeam1BrightnessChanged);
        team2ScoreCharacteristic.setEventHandler(BLECharacteristicEvent::BLEWritten, &onTeam2ScoreChanged);
        team2ColorCharacteristic.setEventHandler(BLECharacteristicEvent::BLEWritten, &onTeam2ColorChanged);
        team2BrightnessCharacteristic.setEventHandler(BLECharacteristicEvent::BLEWritten, &onTeam2BrightnessChanged);

        team1ScoreCharacteristic.setValue(defaultScore);
        team1ColorCharacteristic.setValue(team1DefaultColor);
        team1BrightnessCharacteristic.setValue(defaultBrightness);

        team2ScoreCharacteristic.setValue(defaultScore);
        team2ColorCharacteristic.setValue(team2DefaultColor);
        team2BrightnessCharacteristic.setValue(defaultBrightness);

        BLE.setLocalName(deviceName);
        BLE.setAdvertisedService(service);
        service.addCharacteristic(team1ScoreCharacteristic);
        service.addCharacteristic(team1ColorCharacteristic);
        service.addCharacteristic(team1BrightnessCharacteristic);
        service.addCharacteristic(team2ScoreCharacteristic);
        service.addCharacteristic(team2ColorCharacteristic);
        service.addCharacteristic(team2BrightnessCharacteristic);
        BLE.addService(service);

        BLE.advertise();
        Serial.println("Bluetooth device active, waiting for connections...");
    }

    void update()
    {
        // Required to accept connections
        BLEDevice central = BLE.central();
    }

    int getTeam1Score()
    {
        return int(team1ScoreCharacteristic.value());
    }

    int getTeam1Color()
    {
        return team1ColorCharacteristic.value();
    }

    int getTeam1Brightness()
    {
        return int(team1BrightnessCharacteristic.value());
    }

    int getTeam2Score()
    {
        return int(team2ScoreCharacteristic.value());
    }

    int getTeam2Color()
    {
        return team2ColorCharacteristic.value();
    }

    int getTeam2Brightness()
    {
        return int(team2BrightnessCharacteristic.value());
    }

    bool isDeviceConnected()
    {
        return BLE.central().connected();
    }

    //=========================================================================
    // PRIVATE USE FUNCTIONS
    //=========================================================================

    void onDeviceConnected(BLEDevice device)
    {
        Serial.println("Device connected.");
        digitalWrite(LED_BUILTIN, PinStatus::HIGH);
    }

    void onDeviceDisconnected(BLEDevice device)
    {
        Serial.println("Device disconnected.");
        digitalWrite(LED_BUILTIN, PinStatus::LOW);
    }

    void onTeam1ScoreChanged(BLEDevice device, BLECharacteristic characteristic)
    {
        char s[64];
        snprintf(s, 64, "Team 1 Score changed to %d", getTeam1Score());
        Serial.println(s);
    }
    
    void onTeam1ColorChanged(BLEDevice device, BLECharacteristic characteristic)
    {
        char s[64];
        snprintf(s, 64, "Team 1 Color changed to %x", getTeam1Color());
        Serial.println(s);
    }

    void onTeam1BrightnessChanged(BLEDevice device, BLECharacteristic characteristic)
    {
        char s[64];
        snprintf(s, 64, "Team 1 Brightness changed to %d", getTeam1Brightness());
        Serial.println(s);
    }

    void onTeam2ScoreChanged(BLEDevice device, BLECharacteristic characteristic)
    {
        char s[64];
        snprintf(s, 64, "Team 2 Score changed to %d", getTeam2Score());
        Serial.println(s);
    }

    void onTeam2ColorChanged(BLEDevice device, BLECharacteristic characteristic)
    {
        char s[64];
        snprintf(s, 64, "Team 2 Color changed to %x", getTeam2Color());
        Serial.println(s);
    }

    void onTeam2BrightnessChanged(BLEDevice device, BLECharacteristic characteristic)
    {
        char s[64];
        snprintf(s, 64, "Team 2 Brightness changed to %d", getTeam2Brightness());
        Serial.println(s);
    }
}