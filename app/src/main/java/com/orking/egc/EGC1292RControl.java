package com.orking.egc;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.orking.egc.utils.Method;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by zhanglei on 2017/4/10.
 */

public class EGC1292RControl {

    private final byte[] START_MEASURE_COMMEND = new byte[]{(byte) 0x02, (byte) 0x93, (byte) 0x03, (byte) 0x0d};
    private final byte[] STOP_MEASURE_COMMEND = new byte[]{(byte) 0x02, (byte) 0x00, (byte) 0x03, (byte) 0x0d};

    private UsbManager mUsbManager;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbEpOut;
    private UsbEndpoint mUsbEpIn;
    private UsbDevice mUsbDevice;
    private ReceiveDataAsyncTask mReceiveDataAsyncTask;

    /**
     *
     * @param usbManager
     * @param usbInterface
     * @param endpointIn
     * @param endpointOut
     * @param usbDevice
     */
    public EGC1292RControl(UsbManager usbManager, UsbInterface usbInterface, UsbEndpoint endpointIn,
                           UsbEndpoint endpointOut, UsbDevice usbDevice){
        mUsbManager = usbManager;
        mUsbInterface = usbInterface;
        mUsbEpOut = endpointOut;
        mUsbEpIn = endpointIn;
        mUsbDevice = usbDevice;
        mUsbDeviceConnection = mUsbManager.openDevice(usbDevice);
    }

    public void startMeasure() {
        new StartMeasureAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stopMeasure(){
        new StopMeasureAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class StartMeasureAsyncTask extends AsyncTask<Void, String, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            UsbDeviceConnection connection = mUsbManager.openDevice(mUsbDevice);
            if (connection.claimInterface(mUsbInterface, true)) {
                //
            } else {
                //
            }
            return connection.bulkTransfer(mUsbEpOut, START_MEASURE_COMMEND, START_MEASURE_COMMEND.length, 1000);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if(i >= 0){
                mReceiveDataAsyncTask = new ReceiveDataAsyncTask();
                mReceiveDataAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private class StopMeasureAsyncTask extends AsyncTask<UsbDevice, String, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(UsbDevice... params) {
            return mUsbDeviceConnection.bulkTransfer(mUsbEpOut, STOP_MEASURE_COMMEND, STOP_MEASURE_COMMEND.length, 1000);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            mReceiveDataAsyncTask.cancel(true);
        }
    }

    private class ReceiveDataAsyncTask extends AsyncTask<UsbDevice, String, String> {

        @Override
        protected String doInBackground(UsbDevice... params) {
            int inMax = mUsbEpIn.getMaxPacketSize();
            while (getStatus() != Status.FINISHED) {
                publishProgress("Max packet size:" + inMax + "\n");
                ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
                byte[] butter = byteBuffer.array();
                publishProgress("buffer size :" + butter.length + "\n");
                if (mUsbDeviceConnection.claimInterface(mUsbInterface, true)) {
                    //
                } else {
                    //
                    break;
                }
                int result = mUsbDeviceConnection.bulkTransfer(mUsbEpIn, butter, inMax, 3000);
                if (result >= 0) {
                    publishProgress(Method.bytesToHex(butter));
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
