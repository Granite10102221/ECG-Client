package com.orking.egc;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orking.egc.utils.L;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                //call method to set up device communication
                                mControl = mDeviceManager.initDevice(mUsbManager, device);
                            } else {
                                Toast.makeText(mContext, "permission denied for device " + device, Toast.LENGTH_SHORT).show();
                                L.d(TAG, "permission denied for device " + device);
                            }
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    L.i(TAG, "ACCESSORY_ATTACHED");
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(usbDevice == null){
                        break;
                    }
                    Tv_USBState.append("\nUSB device connected : " + usbDevice.toString());
                    if (mUsbManager.hasPermission(usbDevice)) {
                        Tv_USBState.append("\nHas USB permission, Init device!");
                        mControl = mDeviceManager.initDevice(mUsbManager, usbDevice);
                    } else {
                        Tv_USBState.append("\nRequest permission!");
                        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    L.i(TAG, "ACCESSORY_DETACHED");
                    mControl = null;
                    Tv_USBState.append("\nDevice disconnected.");
                    break;
                default:
                    break;
            }
        }
    };

    private static final String ACTION_USB_PERMISSION = "com.orking.egc.action.USB_PERMISSION";

    private TextView Tv_USBState;
    private Button Btn_StartMeasure;
    private UsbManager mUsbManager;
    private DeviceManager mDeviceManager;
    private PendingIntent mPermissionIntent;
    private Context mContext;
    private EGC1292RControl mControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initWidget();
        mContext = this;
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void initData(){
        mDeviceManager = DeviceManager.getInstance();
    }

    private void initWidget() {
        Tv_USBState = (TextView) findViewById(R.id.text);
        Btn_StartMeasure = (Button) findViewById(R.id.start_measure);
        Btn_StartMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startMeasure(mUsbDevice);
                if(mControl != null){
                    Tv_USBState.append("\nstart measure");
                    mControl.startMeasure();
                } else {
                    Tv_USBState.append("\ncontrol = null");
                }
            }
        });
        findViewById(R.id.stop_measure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mControl != null){
                    mControl.stopMeasure();
                }
            }
        });
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public EGCData abc(byte[] buffer){
        EGCData data = new EGCData();
        if(buffer[0] != (byte) 0x02 || buffer[1] != (byte) 0x93){
            return null;
        }
        data.HRT = buffer[2];
        data.BRT = buffer[3];
        data.LEADStatus = buffer[4];
        for(int i = 4, y = 0;i < buffer.length - 1;i+=2,y++){
            data.mPoints[y] = buffer[i] | buffer[i+1] << 8;
        }
        return data;
    }
}
