package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.bluetooth.BluetoothDevice;

public interface BluetoothLeServiceListener
{
    void onLeDeviceConnected(BluetoothDevice device, boolean isLeft);

    void onLeDeviceDisconnected(BluetoothDevice device);
    
    void onLeServiceDiscovered(boolean status);

    void onQuaternionChanged(BluetoothDevice device, float[] quaternion);

    void onGestureChanged(BluetoothDevice device, float[] gesture);

    void onTouchChanged(BluetoothDevice device, float[] touch);

    void onAccelerationChanged(BluetoothDevice device, float[] acceleration);

    void onRSSIRead(BluetoothDevice device, int rssi);
}
