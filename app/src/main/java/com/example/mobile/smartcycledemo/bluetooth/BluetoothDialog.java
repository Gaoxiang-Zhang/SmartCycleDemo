package com.example.mobile.smartcycledemo.bluetooth;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mobile.smartcycledemo.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDialog extends DialogFragment{

    // context
    Context context;

    // show progress indicator when finding
    ProgressWheel progressWheel;
    // show different error hint
    TextView errorText;

    // show bluetooth devices found
    ListView listView;
    // list adapter suited for listView
    CustomListAdapter customListAdapter;
    // List holding the bluetooth device
    List<BluetoothDevice> bluetoothList;

    // bluetooth adapter
    BluetoothAdapter bluetoothAdapter;

    // handler to handle scan task
    Handler handler;

    // scan period
    private static final long SCAN_PERIOD = 10000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // remove the dialog title of dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_bluetooth, container);

        context = getActivity();

        initInterface(view);
        initBluetooth();

        scanLeDevice(true);

        return view;
    }

    /**
     * initialize the interface
     */
    private void initInterface(View view){
        // set cancel button logic
        TextView textView = (TextView)view.findViewById(R.id.cancel);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(false);
                getDialog().dismiss();
            }
        });
        // set refresh button logic
        textView = (TextView)view.findViewById(R.id.refresh);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothList.clear();
                customListAdapter.notifyDataSetChanged();
                scanLeDevice(true);
                initBluetooth();
            }
        });
        errorText = (TextView)view.findViewById(R.id.error_massage);
        progressWheel = (ProgressWheel)view.findViewById(R.id.progress_wheel);
        listView = (ListView)view.findViewById(R.id.list_view);
        bluetoothList = new ArrayList<>();
        customListAdapter = new CustomListAdapter(context, bluetoothList);
        listView.setAdapter(customListAdapter);
        // onclick listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = bluetoothList.get(position);
                listener.onFoundComplete(device.getAddress());
                getDialog().dismiss();
                scanLeDevice(false);
            }
        });
        progressWheel.spin();

    }

    /**
     * initBluetooth: initialize the procedure of ble device scanning
     */
    private void initBluetooth(){
        // initialize bluetooth manager
        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager == null){
            setErrorInfo(getString(R.string.bluetooth_initial_failed));
            return;
        }
        // initialize bluetooth adapter
        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null){
            setErrorInfo(getString(R.string.bluetooth_initial_failed));
            return;
        }
        // check if ble supported
        if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            setErrorInfo(getString(R.string.ble_not_supported));
            return;
        }
        // enable bluetooth
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
        handler = new Handler();
    }

    /**
     * setErrorInfo: set error text
     */
    private void setErrorInfo(String info){
        errorText.setText(info);
    }

    /**
     * scanLeDevice: start/stop scanning
     */
    private void scanLeDevice(final boolean enable){
        // (re)start scanning device
        if(enable){
            progressWheel.spin();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            bluetoothAdapter.startLeScan(leScanCallback);
        }
        else{
            progressWheel.stopSpinning();
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    // leScanCallBack: callback interface to deliver LE scan results
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        // if finding device
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // stop spinning
                    if (progressWheel.isSpinning()) {
                        progressWheel.stopSpinning();
                    }
                    // add to list and notify data change
                    addNewDevice(device);
                    customListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void addNewDevice(BluetoothDevice device){
        for(int i = 0; i < bluetoothList.size(); i++){
            if(device.getAddress().equals(bluetoothList.get(i).getAddress())){
                return;
            }
        }
        bluetoothList.add(device);
    }

    /**
     * CustomListAdapter: the adapter of listView
     */
    private class CustomListAdapter extends BaseAdapter {
        private List<BluetoothDevice> bluetoothDevices;
        private Context context;

        public CustomListAdapter(Context ctx, List<BluetoothDevice> devices){
            this.context = ctx;
            this.bluetoothDevices = devices;
        }

        @Override
        public int getCount() {
            return bluetoothDevices.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                view = inflater.inflate(R.layout.layout_bluetooth, null);
                TextView textView = (TextView)view.findViewById(R.id.title);
                String name = bluetoothDevices.get(position).getName();
                textView.setText((name != null && name.length() != 0)
                        ? name : getString(R.string.unknown_device));
                textView = (TextView)view.findViewById(R.id.address);
                textView.setText(bluetoothDevices.get(position).getAddress());
            }
            else{
                TextView textView = (TextView)convertView.findViewById(R.id.title);
                String name = bluetoothDevices.get(position).getName();
                textView.setText((name != null && name.length() != 0)
                        ? name : getString(R.string.unknown_device));
                textView = (TextView)convertView.findViewById(R.id.address);
                textView.setText(bluetoothDevices.get(position).getAddress());
                view = convertView;
            }
            return view;
        }
    }

    /**
     * The following interface is for returning value to its former activity/fragment when dialog dismiss
     */
    public static interface OnFoundCompleteListener{
        public abstract void onFoundComplete(String address);
    }

    private OnFoundCompleteListener listener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.listener = (OnFoundCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }


}
