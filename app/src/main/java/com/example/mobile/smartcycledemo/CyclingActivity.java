package com.example.mobile.smartcycledemo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.smartcycledemo.bluetooth.BluetoothDialog;
import com.example.mobile.smartcycledemo.bluetooth.BluetoothLeService;
import com.example.mobile.smartcycledemo.bluetooth.GattAttributes;
import com.example.mobile.smartcycledemo.dialog.CyclingExitDialog;
import com.example.mobile.smartcycledemo.dialog.CyclingFinishDialog;
import com.example.mobile.smartcycledemo.utils.CommonFunction;
import com.example.mobile.smartcycledemo.utils.GlobalType;
import com.example.mobile.smartcycledemo.utils.MyDatabase;
import com.example.mobile.smartcycledemo.view.DonutChart;
import com.example.mobile.smartcycledemo.view.RateMeter;
import com.rey.material.widget.ProgressView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class CyclingActivity extends AppCompatActivity implements CyclingExitDialog.OnExitCompleteListener,
        CyclingFinishDialog.OnFinishCompleteListener, BluetoothDialog.OnFoundCompleteListener{

    // activity context, menu and toolbar
    Context context;
    Menu myMenu;
    Toolbar toolbar;

    // course name
    String courseName;
    // time is the total seconds after starting the game
    int time = 0;
    // time1~time4 is the seconds in each time slice
    int time1, time2, time3, time4;
    // isCycling is the flag of whether starting the cycling
    int isCycling = 0;
    // the upper bound and lower bound of heart rate
    int upperBound, lowerBound;

    // textView of calorie using, distance using, current speed, current heartbeats and target heartbeats
    TextView timerText, heartbeatsInstruction, targetHeartbeats;
    // progress view to indicate the data retrieval
    ProgressView mProgressView;
    // start/pause button and stop button
    Button startButton, stopButton;
    // donut chart to show the progress
    DonutChart donutChart;
    // rate meter to show the current rate
    RateMeter rateMeter;

    // timer, timer task and handler to handle cycling time
    Timer timer;
    MyTimerTask timerTask;
    Handler handler;

    // receiver to receive finish broadcast
    BroadcastReceiver finishReceiver;

    // menu item id:
    // 0 for bluetooth selection dialog
    // 1 for orientation selection
    final int BAR_BLUETOOTH_ID = 0, BAR_ORIENTATION_ID = 1;

    // bluetooth service
    private BluetoothLeService bluetoothLeService;
    // keep the value of characteristic to be notified
    private BluetoothGattCharacteristic notifyCharacteristic;
    // keep the device returned from bluetooth dialog
    private String bluetoothLeAddress;
    // if the device is connected to the ble device
    private boolean isConnected;
    // if the receiver has been registered
    private boolean isRegistered;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycling);

        context = this;

        loadData();
        initInterface();
        setTimer();
        setFinishReceiver();
    }

    /**
     * loadData: load data from activity before
     */
    private void loadData(){
        Bundle bundle = getIntent().getExtras();
        time1 = bundle.getInt(GlobalType.TIME_PERIOD1);
        time2 = bundle.getInt(GlobalType.TIME_PERIOD2);
        time3 = bundle.getInt(GlobalType.TIME_PERIOD3);
        time4 = bundle.getInt(GlobalType.TIME_PERIOD4);
        courseName = bundle.getString(GlobalType.COURSE_NAME);
    }

    /**
     * initInterface: the initialization of view
     */
    private void initInterface(){

        // set toolbar
        setToolbar(getString(R.string.cycling));

        // initialize the heartbeat layout
            // initialize the data area
        ImageView imageView = (ImageView)findViewById(R.id.time_layout).findViewById(R.id.image);
        imageView.setImageResource(R.mipmap.timer);
        imageView = (ImageView)findViewById(R.id.target_layout).findViewById(R.id.image);
        imageView.setImageResource(R.mipmap.meter);
        TextView textView = (TextView)findViewById(R.id.time_layout).findViewById(R.id.title);
        textView.setText(getResources().getString(R.string.timer));
        textView = (TextView)findViewById(R.id.target_layout).findViewById(R.id.title);
        textView.setText(getResources().getString(R.string.target));
        timerText = (TextView)findViewById(R.id.time_layout).findViewById(R.id.value);
        targetHeartbeats = (TextView)findViewById(R.id.target_layout).findViewById(R.id.value);
            // set time in timer with the format of mm:ss
        timerText.setText(convertClock(time));
            // initialize the rate meter
        rateMeter = (RateMeter)findViewById(R.id.rate_meter);
        setTargetHeartRate();

        // initialize the instruction
        heartbeatsInstruction = (TextView)findViewById(R.id.instruction);
        heartbeatsInstruction.setText(getString(R.string.state_disconnected));
        mProgressView = (ProgressView)findViewById(R.id.progress_view);
        //mProgressView.setBackgroundColor(getResources().getColor(R.color.white));

        // init donut chart
        donutChart = (DonutChart)findViewById(R.id.donut_chart);
        donutChart.setQuota(new float[]{time1, time2, time3, time4});
        //init donut instruction
        initDonutInstruction();


        // set start and stop button with onclick listener
        startButton = (Button)findViewById(R.id.start_button);
        stopButton = (Button)findViewById(R.id.pause_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start timer
                if (isCycling == 0) {
                    timer = new Timer();
                    timerTask = new MyTimerTask();
                    // schedule every 1 second to change the ui
                    timer.schedule(timerTask, 0, 1000);
                    isCycling = 1;
                    startButton.setText(getString(R.string.pause));
                }
                // pause timer
                else {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                    timerTask = null;
                    isCycling = 0;
                    startButton.setText(getString(R.string.start));
                }
                changeIconEnableState(myMenu, false);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CyclingExitDialog dialog = new CyclingExitDialog();
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(), "exit_dialog");
            }
        });

    }

    /**
     * setToolbar: set the toolbar with the given title
     */
    private void setToolbar(String title){
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.close_tag);
        }
    }

    /**
     *
     */
    private void initDonutInstruction(){
        double total = time1 + time2 + time3 + time4;
        TextView textView = (TextView)findViewById(R.id.cycling_part1);
        textView.setText(Math.round((double)time1/total*100)+"%");
        textView = (TextView)findViewById(R.id.cycling_part2);
        textView.setText(Math.round((double)time2/total*100)+"%");
        textView = (TextView)findViewById(R.id.cycling_part3);
        textView.setText(Math.round((double) time3 / total * 100) + "%");
        textView = (TextView)findViewById(R.id.cycling_part4);
        textView.setText(Math.round((double) time4 / total * 100) + "%");
    }

    /**
     * setTimer: initialize the handler
     */
    private void setTimer(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // message.what is the current seconds
                if(msg.what > 0){
                    timerText.setText(convertClock(msg.what));
                    //addNewPoint(msg.what, (float)Math.random());
                    donutChart.setCurrentValue();
                }
                if(msg.what == time1 + time2+ time3 + time4){
                    timer.cancel();
                    timer.purge();
                    timer = null;
                    timerTask = null;
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("com.example.mobile.smartcycle.finish");
                    sendBroadcast(broadcastIntent);
                }
            }
        };
    }

    // MyTimerTask: task to set the timer
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = time ++;
            handler.sendMessage(msg);
        }
    }

    /**
     * convertClock: convert the seconds with the format of mm:ss
     */
    private String convertClock(int num){
        int minute = num / 60;
        int second = num % 60;
        return minute+"'"+second+"''";
    }

    /**
     * setTargetHeartRate: set the current heart rate by calculating with age and level
     */
    private void setTargetHeartRate(){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        int age = CommonFunction.calculateAge(sharedPreferences.getString(MyDatabase.UserProfile.KEY_AGE, ""));
        int level = sharedPreferences.getInt(MyDatabase.UserProfile.KEY_LEVEL, 0);
        double[] heartRate = CommonFunction.calculateHRmax(age, level);
        upperBound = (int)heartRate[1];
        lowerBound = (int)heartRate[0];
        targetHeartbeats.setText(lowerBound + " ~ " + upperBound);

        // init rate meter
        rateMeter.setInitValue(lowerBound, upperBound);
    }

    /**
     * setFinishReceiver: set the receiver to handle the end of timer
     */
    private void setFinishReceiver(){
        finishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CyclingFinishDialog dialog = new CyclingFinishDialog();
                Bundle bundle = new Bundle();
                bundle.putString(GlobalType.COURSE_NAME, courseName);
                bundle.putInt(GlobalType.TOTAL_TIME, time / 60);
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "finish_dialog");
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.mobile.smartcycle.finish");
        registerReceiver(finishReceiver, filter);
    }

    /**
     * onCreateOptionsMenu: add menu button to action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem menuItem = menu.add(0, BAR_BLUETOOTH_ID, 0, "Bluetooth Management");
        menuItem.setIcon(R.mipmap.bluetooth);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menuItem = menu.add(0, BAR_ORIENTATION_ID, 0, "Device Orientation");
        menuItem.setIcon(R.mipmap.rotation);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        myMenu = menu;

        return true;
    }

    /**
     * changeIconEnableState: change the ability of menu icon
     */
    private void changeIconEnableState(Menu menu, boolean flag){
        // flag = true means enabling menu item
        if(flag){
            menu.findItem(BAR_BLUETOOTH_ID).setEnabled(true);
            menu.findItem(BAR_ORIENTATION_ID).setEnabled(true);
        }
        else {
            menu.findItem(BAR_BLUETOOTH_ID).setEnabled(false);
            menu.findItem(BAR_ORIENTATION_ID).setEnabled(false);
        }
    }

    /**
     * changeBluetoothIconState: change the state of bluetooth
     */
    private void changeBluetoothIconState(Menu menu, boolean flag){
        if(flag){
            menu.findItem(BAR_BLUETOOTH_ID).setIcon(R.mipmap.bluetooth);
        }
        else{
            menu.findItem(BAR_BLUETOOTH_ID).setIcon(R.mipmap.bluetooth_off);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case android.R.id.home:
                finish();
                break;
            case BAR_BLUETOOTH_ID:
                if(isConnected){
                    bluetoothLeService.disconnect();
                }
                else{
                    BluetoothDialog dialog = new BluetoothDialog();
                    dialog.show(getFragmentManager(),"bluetooth_fragment");
                }
                break;
            case BAR_ORIENTATION_ID:
                //changeOrientation();
                Toast.makeText(context, courseName, Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * changeOrientation: change the orientation of the interface

    private void changeOrientation(){
        // current is portrait
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        // current is landscape
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }     */

    /**
     * parseHeartbeatData: transform the original U1:#num to num
     */
    private int parseHeartbeatData(String data){
        if(data.length() > 3) {
            return Integer.parseInt(data.substring(3, data.length() - 2));
        }
        else{
            return 0;
        }
    }

    /**
     * addNewPoint: when receive heartbeat data, add it
     */
    private void addNewPoint(String value){
        int result = parseHeartbeatData(value);
        rateMeter.setCurrentValue(result);
        String info;
        if(result < 10){
            info = getResources().getString(R.string.state_retrieving_data);
            mProgressView.start();
        }
        else if(result < lowerBound){
            info = getResources().getString(R.string.state_accelerate);
            mProgressView.stop();
        }
        else if(result >= lowerBound && result <= upperBound){
            info = getResources().getString(R.string.state_keep);
            mProgressView.stop();
        }
        else{
            info = getResources().getString(R.string.state_slow);
            mProgressView.stop();
        }
        heartbeatsInstruction.setText(info);
    }

    /**
     * onExitComplete: finish the activity when getting
     */
    public void onExitComplete(){
        finish();
    }

    /**
     * onFinishComplete: finish cycling class
     */
    public void onFinishComplete(){
        Intent confirmIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalType.TOTAL_TIME, time / 60);
        confirmIntent.putExtras(bundle);
        setResult(0, confirmIntent);
        finish();
    }

    /**
     * onFoundComplete: finish binding bluetooth device
     */
    public void onFoundComplete(String address){
        if(address != null){
            bluetoothLeAddress = address;
            Intent gattIntent = new Intent(this, BluetoothLeService.class);
            // bind service with intent, serviceConnection and parameter
            bindService(gattIntent, serviceConnection, BIND_AUTO_CREATE);
            // if the receiver has been registered
            if(!isRegistered){
                isRegistered = true;
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
                filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
                filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
                filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
                registerReceiver(gattUpdateReceiver, filter);
            }
            // connect the gatt
            if(bluetoothLeService != null && !isConnected){
                bluetoothLeService.connect(address);
            }
        }
    }

    /**
     * initNotification: enable the notification from heartbeat characteristic
     */
    private void initNotification(){
        List<BluetoothGattService> services = bluetoothLeService.getSupportedGattServices();
        for(BluetoothGattService gattService: services){
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for(BluetoothGattCharacteristic characteristic: gattCharacteristics){
                if(characteristic.getUuid().equals(UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT))){
                    int charaProp = characteristic.getProperties();
                    if((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0){
                        if(notifyCharacteristic != null){
                            bluetoothLeService.setCharacteristicNotification(notifyCharacteristic, false);
                            notifyCharacteristic = null;
                        }
                        bluetoothLeService.readCharacteristic(characteristic);
                    }
                    if((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                        notifyCharacteristic = characteristic;
                        bluetoothLeService.setCharacteristicNotification(characteristic, true);
                    }
                }
            }
        }
    }

    // service connection for bind service
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            // carry out some initialization
            if(!bluetoothLeService.initialize()){
                return;
            }
            bluetoothLeService.connect(bluetoothLeAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
        }
    };

    // broadcast receiver to receive connected/disconnected information or heartbeat data from service
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                isConnected = true;
                changeBluetoothIconState(myMenu, false);
                heartbeatsInstruction.setText(getResources().getString(R.string.state_retrieving_data));
                mProgressView.setVisibility(View.VISIBLE);
                mProgressView.start();
                Toast.makeText(context, "The device has been successfully connected.",Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                isConnected = false;
                changeBluetoothIconState(myMenu, true);
                mProgressView.setVisibility(View.INVISIBLE);
                heartbeatsInstruction.setText(getResources().getString(R.string.state_disconnected));
                Toast.makeText(context, "The device has been disconnected.",Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                initNotification();
            }
            else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
                addNewPoint(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishReceiver);
        if(isRegistered){
            unregisterReceiver(gattUpdateReceiver);
            unbindService(serviceConnection);
            bluetoothLeService.close();
        }
    }
}

