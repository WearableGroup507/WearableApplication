package tw.edu.ntust.jojllman.wearableapplication.BLE;

import java.util.UUID;

public abstract class GloveGattAttributes
{
    // The characteristic's UUID of sending notifications from server (glove MCU) to client (Android device).
    // Write (writeCharacteristic) 0x01 to start sending notifications; 0x00 to stop.
    public static final UUID UUID_SEND_NOTIFICATION = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    // The characteristic's UUID of sending notifications from server (glove MCU) to client (Android device).
    // Write (writeCharacteristic) 0x01 to start sending notifications; 0x00 to stop.
    public static final UUID UUID_SEND_COMMAND = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");

    // The characteristic's UUID for client (Android device) to enable/disable notifications receiving.
    // Use setNotification for enable/disable notifications.
    public static final UUID UUID_SET_NOTIFICATION = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");

    // Start notification byte. (Using with UUID_SEND_NOTIFICATION)
    public static final byte NOTIFICATION_START = 0x01;

    // Stop notification byte. (Using with UUID_SEND_NOTIFICATION)
    public static final byte NOTIFICATION_STOP = 0x00;

    // Remote device reset byte. (Using with UUID_SEND_COMMAND)
    public static final byte COMMAND_RESET = 0x01;

    // The identification byte to distinguish which data the package is carrying.
    // This package will be carrying quaternion (w, x, y, z).
    public static final byte IDENTIFIER_QUATERNION = 0x00;

    // The identification byte to distinguish which data the package is carrying.
    // This package will be carrying flex, touch and acceleration.
    public static final byte IDENTIFIER_GESTURE_N_ACCELERATION = 0x01;
}