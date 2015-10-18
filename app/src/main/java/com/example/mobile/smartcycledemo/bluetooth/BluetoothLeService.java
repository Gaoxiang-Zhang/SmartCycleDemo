package com.example.mobile.smartcycledemo.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

    // bluetooth manager and bluetooth adapter
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    // address of device to be connected
    private String bluetoothDeviceAddress;

    // the tag for debug
    private String DEBUG_TAG = "debug_tag";

    // different broadcast action
    public final static String ACTION_GATT_CONNECTED =
            "com.example.mobile.smartcycle.Interface.BluetoothLowEnergy.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.mobile.smartcycle.Interface.BluetoothLowEnergy.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.mobile.smartcycle.Interface.BluetoothLowEnergy.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.mobile.smartcycle.Interface.BluetoothLowEnergy.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.mobile.smartcycle.Interface.BluetoothLowEnergy.EXTRA_DATA";

    // specify the notification uuid (set by hardware)
    private final static UUID HEART_BEAT_UUID = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);


    // gattCallback: the interface that app cares about. For example, connection change or characteristic changed.
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction, null);
                Log.d(DEBUG_TAG, "Connected t GATT Server");
                Log.d(DEBUG_TAG, "Attempting to start service discovery: " + bluetoothGatt.discoverServices());
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction, null);
                Log.d(DEBUG_TAG, "Disconnected to GATT Server");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    /**
     * broadcastUpdate: broadcast the result of gettCallback
     */
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);
        if(characteristic == null){
            sendBroadcast(intent);
            return;
        }
        Log.d(DEBUG_TAG,characteristic.getStringValue(0));
        // heartbeat data
        if(HEART_BEAT_UUID.equals(characteristic.getUuid())){
            String value = characteristic.getStringValue(0);
            intent.putExtra(EXTRA_DATA, value);
            sendBroadcast(intent);
        }

    }

    /**
     * initialize: initialize the bluetooth manager and adapter
     */
    public boolean initialize(){
        if(bluetoothManager == null){
            bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null){
                Log.d(DEBUG_TAG, "initialize bluetooth manager failed");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null){
            Log.d(DEBUG_TAG, "initialize bluetooth adapter failed");
            return false;
        }
        return true;
    }

    /**
     * connect: connect to the gatt interface with gattCallback and given address
     */
    public boolean connect(final String address){
        if(bluetoothAdapter == null || address == null){
            Log.d(DEBUG_TAG, "connect bluetooth adapter failed");
            return false;
        }
        // previously connected to device. Try to reconnect
        if(bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress) && bluetoothGatt != null){
            Log.d(DEBUG_TAG, "trying to use an existing gatt for connection.");
            if(bluetoothGatt.connect()){
                Log.d(DEBUG_TAG,"connecting gatt");
                return true;
            }
            else{
                Log.d(DEBUG_TAG, "connect gatt failed");
                return false;
            }
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            Log.d(DEBUG_TAG, "connect device failed");
        }
        // setting the autoConnect parameter to false to directly connect to the device
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Log.d(DEBUG_TAG, "Trying to create a new connection");
        bluetoothDeviceAddress = address;
        return true;
    }

    /**
     * disconnect(): disconnect with the gatt
     */
    public void disconnect(){
        if(bluetoothAdapter == null || bluetoothGatt == null){
            return;
        }
        bluetoothGatt.disconnect();
    }

    /**
     * close(): close the gatt
     */
    public void close(){
        if(bluetoothGatt == null){
            return;
        }
        disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    /**
     * readCharacteristic: read the requested characteristic from the associate remote device
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if(bluetoothManager == null || bluetoothAdapter == null){
            Log.d(DEBUG_TAG, "read bluetooth character failed");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * setCharacteristic: enable/disable notifications for a given characteristic
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled){
        if(bluetoothAdapter == null || bluetoothGatt == null){
            Log.d(DEBUG_TAG, "set bluetooth character failed");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // if the characteristic fit the uuid of heart beat channel, enable notification value
        if(HEART_BEAT_UUID.equals(characteristic.getUuid())){
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * getSupportGattServices: get the list of gatt services ( gatt services contain several characteristics )
     */
    public List<BluetoothGattService> getSupportedGattServices(){
        if(bluetoothGatt == null){
            return null;
        }
        return bluetoothGatt.getServices();
    }

    /**
     * The following code is common for bind service
     */

    private final IBinder staticBinder = new LocalBinder();

    public class LocalBinder extends Binder{
        public BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return staticBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
}
