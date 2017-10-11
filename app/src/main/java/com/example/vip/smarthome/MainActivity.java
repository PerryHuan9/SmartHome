package com.example.vip.smarthome;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vip.smarthome.bluetooth.BluetoothLeService;
import com.example.vip.smarthome.bluetooth.ConnectActivity;
import com.example.vip.smarthome.fragment.AirFragment;
import com.example.vip.smarthome.fragment.CurtainFragment;
import com.example.vip.smarthome.fragment.FanFragment;
import com.example.vip.smarthome.fragment.LightFragment;
import com.example.vip.smarthome.fragment.OnFragmentListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.vip.smarthome.bluetooth.BluetoothLeService.ACTION_DATA_AVAILABLE;
import static com.example.vip.smarthome.bluetooth.BluetoothLeService.ACTION_GATT_CONNECTED;
import static com.example.vip.smarthome.bluetooth.BluetoothLeService.ACTION_GATT_DISCONNECTED;
import static com.example.vip.smarthome.bluetooth.BluetoothLeService
        .ACTION_GATT_SERVICES_DISCOVERED;


/**
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        OnFragmentListener {
    private ImageButton toConnect;
    private TextView temperature, humidity, date;
    private Handler handler;
    private RelativeLayout light, fan, curtain, air, tv;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static int WRITE_DATA_PERIOD = 40;
    private static int REQ_CNT = 0;
    private final static int REQUEST_CONNECT_DEVICE = 1;
    public static final String FRAGMENT_FLAG = "flag";

    private boolean connected;
    private String deviceName;
    private String deviceAddress;
    private boolean isLightSend, isFanSend, isCurtainSend, isGoOutModeSend,
            isSmartModeSend, isPowerSavingModeSend;
    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
    private BluetoothLeService mBluetoothLeService;

    private LightFragment lightFragment;
    private CurtainFragment curtainFragment;
    private FanFragment fanFragment;
    private AirFragment airFragment;
    private ImageView menuView;


    private byte isHallLightOpen, isBedroonLightOpen, isBedLightopen, isFanOpen, isCurtainOpen;
    private int hallLightTime, bedroomLightTime, bedLightTime, fanTime, curtainTime;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.w("tag", "mBluetoothLeService:" + mBluetoothLeService);
            if (!mBluetoothLeService.initialize()) {
                Log.w("tag", "Unable to initialize Bluetooth");
                finish();

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w("tag", "连接失败");
            mBluetoothLeService = null;
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                handler.postDelayed(this, WRITE_DATA_PERIOD);
                date.setText(format.format(new Date()));

                if (REQ_CNT >= 10) {
                    REQ_CNT = 0;
                    btSendBytes(Protocol.getSendData(Protocol.REQUIRE_STATE, Protocol
                            .getCommandData(Protocol.REQUIRE_STATE)));
                    //Log.w("send", "require state send successfully");
                }
                REQ_CNT++;
                if (isLightSend) {
                    btSendBytes(Protocol.getSendData(Protocol.SET_LIGHT, Protocol
                            .getCommandData(Protocol.SET_LIGHT)));
                    isLightSend = false;
                    //Log.w("send", "Light send successfully");

                }
                if (isFanSend) {
                    btSendBytes(Protocol.getSendData(Protocol.SET_FAN, Protocol
                            .getCommandData(Protocol.SET_FAN)));
                    isFanSend = false;
                    //Log.w("send", "Fan send successfully");
                }
                if (isCurtainSend) {
                    btSendBytes(Protocol.getSendData(Protocol.SET_CURTAIN, Protocol
                            .getCommandData(Protocol.SET_CURTAIN)));
                    isCurtainSend = false;
                    //Log.w("send", "Curtain send successfully");
                }
                if (isGoOutModeSend) {
                    btSendBytes(Protocol.getSendData(Protocol.OUTSIDE_MODE, Protocol
                            .getCommandData(Protocol.OUTSIDE_MODE)));
                    isGoOutModeSend = false;
                    //Log.w("send", "Curtain send successfully");
                }
                if (isPowerSavingModeSend) {
                    btSendBytes(Protocol.getSendData(Protocol.POWERSAVING_MODE, Protocol
                            .getCommandData(Protocol.POWERSAVING_MODE)));
                    isPowerSavingModeSend = false;
                    //Log.w("send", "Curtain send successfully");
                }
                if (isSmartModeSend) {
                    btSendBytes(Protocol.getSendData(Protocol.SMART_MODE, Protocol
                            .getCommandData(Protocol.SMART_MODE)));
                    isSmartModeSend = false;
                    //Log.w("send", "Curtain send successfully");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private DrawerLayout drawerLayout;

    /**
     * 定义处理BLE收发服务的各类事件接收机mGattUpdateReceiver，主要包括下面几种：
     * ACTION_GATT_CONNECTED: 连接到GATT
     * ACTION_GATT_DISCONNECTED: 断开GATT
     * ACTION_GATT_SERVICES_DISCOVERED: 发现GATT下的服务
     * ACTION_DATA_AVAILABLE: BLE收到数据
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int reCmd = -2;
            Log.w("tag", "已经进入接收函数mGattUpdateReceiver");
            if (ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                toConnect.setBackground(getResources().getDrawable(R.drawable.connected));
                Log.w("tag", "已经在连接");
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                toConnect.setBackground(getResources().getDrawable(R.drawable.disconnected));
                Log.w("tag", "连接不成功");
            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mBluetoothLeService.getSupportedGattServices();
                //接收到数据了
            } else if (ACTION_DATA_AVAILABLE.equals(action)) {
                final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if (data != null && data.length > 0) {
//					final StringBuilder stringBuilder = new StringBuilder(data.length);
//					for (byte byteChar : data) {
//						stringBuilder.append(String.format("%02X ", byteChar));
//					}
//					Log.w("check","dataInLen:" + data.length+";data:"+data[0]+"  "+data[1]+"  
// "+data[2] );
                    Protocol.processDataIn(data, data.length);
                    temperature.setText(Protocol.temperature + "℃");
                    humidity.setText(Protocol.humidity + "%");
//					Log.w("tag","receiveData:"+stringBuilder.toString());
                }
                //解析接收到的数据

            }
        }
    };

    private void initAll() {
        handler = new Handler();
        toConnect = (ImageButton) findViewById(R.id.bt_connect);
        date = (TextView) findViewById(R.id.date);
        temperature = (TextView) findViewById(R.id.temperature);
        humidity = (TextView) findViewById(R.id.humidity);
        toConnect.setOnClickListener(this);
        light = (RelativeLayout) findViewById(R.id.light);
        fan = (RelativeLayout) findViewById(R.id.fan);
        curtain = (RelativeLayout) findViewById(R.id.curtain);
        air = (RelativeLayout) findViewById(R.id.air);
        tv = (RelativeLayout) findViewById(R.id.tv);
        drawerLayout = (DrawerLayout) findViewById(R.id.smart_mode);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                return true;
            }
        });
        menuView = (ImageView) findViewById(R.id.menu_view);
        menuView.setOnClickListener(this);
        light.setOnClickListener(this);
        fan.setOnClickListener(this);
        curtain.setOnClickListener(this);
        air.setOnClickListener(this);
        tv.setOnClickListener(this);
        android.app.FragmentManager manager = getFragmentManager();
        curtainFragment = new CurtainFragment();
        fanFragment = new FanFragment();
        airFragment = new AirFragment();
        curtainFragment.setOnFragmentListener(this);
        fanFragment.setOnFragmentListener(this);
        lightFragment = (LightFragment) manager.findFragmentById(R.id.content);
        if (null == lightFragment) {
            lightFragment = new LightFragment();
            manager.beginTransaction().add(R.id.content, lightFragment).commit();
        }
        lightFragment.setOnFragmentListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smarthome_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        initAll();
        bindService(new Intent(this, BluetoothLeService.class), serviceConnection,
                BIND_AUTO_CREATE);
        handler.postDelayed(runnable, WRITE_DATA_PERIOD);

        Log.w("tag", "已进入主程序onCreate");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_out:
                isGoOutModeSend = true;
                Toast.makeText(this, "已关闭所有用电器", Toast.LENGTH_LONG).show();
                break;
            case R.id.save_power:
                isPowerSavingModeSend = true;
                break;
            case R.id.smart_mode:
                isSmartModeSend = true;
                break;
        }
        Log.w("tag", "click menu");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_connect:
                if (!connected) {
                    Intent intent = new Intent(this, ConnectActivity.class);
                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                } else {
                    mBluetoothLeService.disconnect();
                }
                break;
            case R.id.light:
                getFragmentManager().beginTransaction().replace(R.id.content, lightFragment)
                        .commit();
                setSelectedBackgroud("light");
                break;
            case R.id.fan:
                getFragmentManager().beginTransaction().replace(R.id.content, fanFragment)
                        .commit();
                setSelectedBackgroud("fan");
                break;
            case R.id.curtain:
                getFragmentManager().beginTransaction().replace(R.id.content, curtainFragment)
                        .commit();
                setSelectedBackgroud("curtain");
                break;
            case R.id.air:
                getFragmentManager().beginTransaction().replace(R.id.content, airFragment)
                        .commit();
                setSelectedBackgroud("air");
                break;
            case R.id.tv:
                getFragmentManager().beginTransaction().replace(R.id.content, airFragment)
                        .commit();
                setSelectedBackgroud("tv");
                break;
            case R.id.menu_view:
                drawerLayout.openDrawer(GravityCompat.START);
        }

    }

    private void setSelectedBackgroud(String tag) {
        switch (tag) {
            case "light":
                light.setBackgroundColor(Color.argb(118, 240, 177, 50));
                fan.setBackgroundColor(Color.alpha(256));
                curtain.setBackgroundColor(Color.alpha(256));
                air.setBackgroundColor(Color.alpha(256));
                tv.setBackgroundColor(Color.alpha(256));
                break;
            case "fan":
                fan.setBackgroundColor(Color.argb(118, 240, 177, 50));
                light.setBackgroundColor(Color.alpha(256));
                curtain.setBackgroundColor(Color.alpha(256));
                air.setBackgroundColor(Color.alpha(256));
                tv.setBackgroundColor(Color.alpha(256));
                break;
            case "curtain":
                curtain.setBackgroundColor(Color.argb(118, 240, 177, 50));
                light.setBackgroundColor(Color.alpha(256));
                fan.setBackgroundColor(Color.alpha(256));
                air.setBackgroundColor(Color.alpha(256));
                tv.setBackgroundColor(Color.alpha(256));
                break;
            case "air":
                air.setBackgroundColor(Color.argb(118, 240, 177, 50));
                light.setBackgroundColor(Color.alpha(256));
                curtain.setBackgroundColor(Color.alpha(256));
                fan.setBackgroundColor(Color.alpha(256));
                tv.setBackgroundColor(Color.alpha(256));
                break;
            case "tv":
                tv.setBackgroundColor(Color.argb(118, 240, 177, 50));
                light.setBackgroundColor(Color.alpha(256));
                fan.setBackgroundColor(Color.alpha(256));
                air.setBackgroundColor(Color.alpha(256));
                curtain.setBackgroundColor(Color.alpha(256));
                break;
        }
    }


    @Override
    public void onFragment(Bundle bundle) {
        if (bundle == null) return;
        switch (bundle.getString(FRAGMENT_FLAG)) {
            case LightFragment.LIGHT_FLAG:
                isHallLightOpen = (byte) (bundle.getInt(LightFragment.HALL_LIGHT_FLAG) & 0xff);
                isBedroonLightOpen = (byte) (bundle.getInt(LightFragment.BEDROOM_LIGHT_FLAG) &
                        0xff);
                isBedLightopen = (byte) (bundle.getInt(LightFragment.BED_LIGHT_FLAG) & 0xff);
                Protocol.lightList[0] = isHallLightOpen;
                Protocol.lightList[1] = isBedroonLightOpen;
                Protocol.lightList[2] = isBedLightopen;

                hallLightTime = bundle.getInt(LightFragment.HALL_TIME_FLAG);
                bedroomLightTime = bundle.getInt(LightFragment.BEDROOM_TIME_FLAG);
                bedLightTime = bundle.getInt(LightFragment.BED_TIME_FLAG);
                Protocol.lightList[3] = (byte) (hallLightTime & 0xff);
                Protocol.lightList[4] = (byte) ((hallLightTime >> 8) & 0xff);
                Protocol.lightList[5] = (byte) (bedroomLightTime & 0xff);
                Protocol.lightList[6] = (byte) ((bedroomLightTime >> 8) & 0xff);
                Protocol.lightList[7] = (byte) (bedLightTime & 0xff);
                Protocol.lightList[8] = (byte) ((bedLightTime >> 8) & 0xff);

                Log.w("smart", "isHallLightOpen:" + isHallLightOpen);
                Log.w("smart", "isBedroomLightOpen:" + isBedroonLightOpen);
                Log.w("smart", "LIGHTNESS:" + isBedLightopen);
                Log.w("smart", "halltime:" + this.hallLightTime);
                Log.w("smart", "BEDROOMTIME:" + this.bedroomLightTime);
                Log.w("smart", "BEDTIME:" + this.bedLightTime);
                isLightSend = true;
                break;
            case FanFragment.FAN_FLAG:
                isFanOpen = (byte) (bundle.getInt(FanFragment.FAN_SPEED_FLAG) & 0xff);
                fanTime = bundle.getInt(FanFragment.FAN_TIME_FLAG);
                Protocol.fanList[0] = isFanOpen;
                Protocol.fanList[1] = (byte) (fanTime & 0xff);
                Protocol.fanList[2] = (byte) ((fanTime >> 8) & 0xff);
                isFanSend = true;
                Log.w("smart", "isFanOpen:" + isFanOpen);
                Log.w("smart", "fanTime:" + fanTime);
                Log.w("smart", "Protocol.fanList:" + (int) ((Protocol.fanList[1] & 0xff) + (
                        (Protocol.fanList[2]) << 8)));
                break;
            case CurtainFragment.CURTAIN_FLAG:
                isCurtainOpen = (byte) (bundle.getInt(CurtainFragment.CURTAIN_STATE_FLAG) & 0xff);
                curtainTime = bundle.getInt(CurtainFragment.CURTAIN_TIME_FLAG);
                Protocol.curtainList[0] = isCurtainOpen;
                Protocol.curtainList[1] = (byte) (curtainTime & 0xff);
                Protocol.curtainList[2] = (byte) ((curtainTime >> 8) & 0xff);
                isCurtainSend = true;
                Log.w("smart", "isCurtainOpen:" + isCurtainOpen);
                Log.w("smart", "curtainTime:" + curtainTime);
                Log.w("smart", "Protocol.curtainList:" + (int) ((Protocol.curtainList[1] & 0xff)
                        + (Protocol.curtainList[2] << 8)));
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            Log.w("tag", "mBluetoothLeService NOT null");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == Activity.RESULT_OK) {
            deviceName = data.getExtras().getString(EXTRAS_DEVICE_NAME);
            deviceAddress = data.getExtras().getString(EXTRAS_DEVICE_ADDRESS);
            Log.w("tag", "mDeviceName:" + deviceName + ",mDeviceAddress:" + deviceAddress);
            Log.w("tag", "mBluetoothLeService:" + mBluetoothLeService);
            if (mBluetoothLeService != null) {
                mBluetoothLeService.disconnect();
                final boolean result = mBluetoothLeService.connect(deviceAddress);
                Log.w("tag", "Connect request result=" + result);
            }

        }

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        mBluetoothLeService = null;
        handler.removeCallbacks(runnable);
    }

    public void btSendBytes(byte[] data) {
        if (connected) {
            mBluetoothLeService.writeCharacteristic(data);
            Log.w("send", " send successfully");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
