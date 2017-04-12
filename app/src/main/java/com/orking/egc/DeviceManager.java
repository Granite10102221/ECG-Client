package com.orking.egc;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

/**
 * Created by zhanglei on 2017/4/10.
 */

public class DeviceManager {

    private EGC1292RControl mControl;

    private static DeviceManager sDeviceManager;
    private static final Object LOCK = new Object();

    public static DeviceManager getInstance(){
        if(sDeviceManager == null) {
            synchronized (LOCK) {
                if (sDeviceManager == null) {
                    sDeviceManager = new DeviceManager();
                }
            }
        }
        return sDeviceManager;
    }

    private DeviceManager(){

    }

    public EGC1292RControl initDevice(UsbManager usbManager, UsbDevice usbDevice) {
        int interfaceCount = usbDevice.getInterfaceCount();
        UsbEndpoint endpointIn = null;
        UsbEndpoint endpointOut = null;
        UsbInterface usbInterface = null;
        for (int interfaceIndex = 0; interfaceIndex < interfaceCount; interfaceIndex++) {
            UsbInterface usbInterfaceTemp = usbDevice.getInterface(interfaceIndex);
            if (UsbConstants.USB_CLASS_CDC_DATA != usbInterfaceTemp.getInterfaceClass()) {
                continue;
            }

            for (int i = 0; i < usbInterfaceTemp.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterfaceTemp.getEndpoint(i);
                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                        endpointOut = ep;
                    } else if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                        endpointIn = ep;
                    }
                }
            }

            if((null != endpointIn) && (null != endpointOut)) {
                usbInterface = usbInterfaceTemp;
                UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);

                if (usbDeviceConnection.claimInterface(usbInterface, true)) {
                    //
                } else {
                    //
                    break;
                }
                // set flow control to 8N1 at 9600 baud
                int baudRate = 9600;
                byte stopBitsByte = 1;
                byte parityBitesByte = 0;
                byte dataBits = 8;
                byte[] msg = {
                        (byte) (baudRate & 0xff),
                        (byte) ((baudRate >> 8) & 0xff),
                        (byte) ((baudRate >> 16) & 0xff),
                        (byte) ((baudRate >> 24) & 0xff),
                        stopBitsByte,
                        parityBitesByte,
                        dataBits
                };
                int dataCount = usbDeviceConnection.controlTransfer(UsbConstants.USB_TYPE_CLASS | 0x01, 0x20, 0, 0, msg, msg.length, 5000);
                if(dataCount > 0){
                    mControl = new EGC1292RControl(usbManager, usbInterface, endpointIn,
                            endpointOut, usbDevice);
                }
                break;
            }
        }
        return mControl;
    }

    public void deviceDisconnect(){
    }

    public EGC1292RControl getDeviceControl(){
        return mControl;
    }
}
