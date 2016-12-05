package jp.nita.ble_test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

public class PeripheralActivity extends Activity {

	BluetoothGattServer gattServer;
	BluetoothDevice mDevice;
	BluetoothGattCharacteristic mCharacteristic;
	BluetoothLeAdvertiser mAdvertiser;

	Handler guiThreadHandler = new Handler();

	private static final String SERVICE_UUID = "0A917941-40E4-40E8-81B8-146FD1F2479A";
	private static final String CHAR_UUID = "0015D5AE-2653-4BB1-8EE1-AF566EE846DC";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peripheral);

		BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter adapter = manager.getAdapter();
		mAdvertiser = adapter.getBluetoothLeAdvertiser();

		AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
		settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
		settingBuilder.setConnectable(true);
		settingBuilder.setTimeout(0);
		settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW);
		AdvertiseSettings settings = settingBuilder.build();

		AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
		dataBuilder.addServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)));
		AdvertiseData advertiseData = dataBuilder.build();

		setGattServer();
		final Activity activity = this;
		mAdvertiser.startAdvertising(settings, advertiseData, new AdvertiseCallback() {
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				super.onStartSuccess(settingsInEffect);
				Toast.makeText(activity, "start succeeded", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onStartFailure(int errorCode) {
				super.onStartFailure(errorCode);
				Toast.makeText(activity, "start failed : "+errorCode, Toast.LENGTH_LONG).show();
			}
		});

		findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gattServer == null) {
					Toast.makeText(v.getContext(), "gattServer is null", Toast.LENGTH_LONG).show();
					return;
				}
				if (mDevice == null) {
					Toast.makeText(v.getContext(), "mDevice is null", Toast.LENGTH_LONG).show();
					return;
				}
				if (mCharacteristic == null) {
					Toast.makeText(v.getContext(), "mCharacteristic is null", Toast.LENGTH_LONG).show();
					return;
				}
				mCharacteristic.setValue("ABC".getBytes());
				gattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);
			}
		});
	}
	
	@Override
	public void onPause(){
		super.onPause();

        final Activity finalActivity = this;
        //アドバタイズを停止
        if (mAdvertiser != null) {
        	mAdvertiser.stopAdvertising(new AdvertiseCallback(){
        		@Override
    			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
    				super.onStartSuccess(settingsInEffect);
    				Toast.makeText(finalActivity, "stop succeeded", Toast.LENGTH_LONG).show();
    			}

    			@Override
    			public void onStartFailure(int errorCode) {
    				super.onStartFailure(errorCode);
    				Toast.makeText(finalActivity, "stop failed : "+errorCode, Toast.LENGTH_LONG).show();
    			}
        	});
        	mAdvertiser = null;
        }
		
		//サーバーを閉じる
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
        }
	}

	public void setGattServer() {

		BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

		gattServer = manager.openGattServer(getApplicationContext(), new BluetoothGattServerCallback() {
			@Override
			public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
					BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
					int offset, byte[] value) {
				super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded,
						offset, value);
				if (value != null) {
					showToastAsync("get characteristic write request : " + new String(value));
					Log.d("TAG", "value ~ " + new String(value));
				}
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
			}

			@Override
			public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
					BluetoothGattCharacteristic characteristic) {
				super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
				showToastAsync("get characteristic read request");
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "ABC".getBytes());
			}

			@Override
			public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
				super.onConnectionStateChange(device, status, newState);
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					showToastAsync("state changed to connected");
					mDevice = device;
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					showToastAsync("state changed to disconnected");
					mDevice = null;
				}
			}
		});

		BluetoothGattService service = new BluetoothGattService(UUID.fromString(SERVICE_UUID),
				BluetoothGattService.SERVICE_TYPE_PRIMARY);
		mCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(CHAR_UUID),
				BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ
						| BluetoothGattCharacteristic.PROPERTY_WRITE,
				BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
		service.addCharacteristic(mCharacteristic);
		gattServer.addService(service);
	}

	public void showToastAsync(final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (PeripheralActivity.this != null) {
					Toast.makeText(PeripheralActivity.this, text, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}