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
import android.widget.TextView;

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
		if (mAdvertiser == null) {
			showToastAsync(this, "mAdvertiser is null");
			finish();
			return;
		}

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
		final PeripheralActivity activity = this;
		mAdvertiser.startAdvertising(settings, advertiseData, new AdvertiseCallback() {
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				super.onStartSuccess(settingsInEffect);
				showToastAsync(activity, "start succeeded");
			}

			@Override
			public void onStartFailure(int errorCode) {
				super.onStartFailure(errorCode);
				showToastAsync(activity, "start failed : "+errorCode);
			}
		});

		findViewById(R.id.button_send_abc).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gattServer == null) {
					showToastAsync(activity, "gattServer is null");
					return;
				}
				if (mDevice == null) {
					showToastAsync(activity, "mDevice is null");
					return;
				}
				if (mCharacteristic == null) {
					showToastAsync(activity, "mCharacteristic is null");
					return;
				}
				mCharacteristic.setValue("ABC".getBytes());
				gattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);
			}
		});
		
		final PeripheralActivity finalActivity = this;
		findViewById(R.id.button_stop_advertising).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
		        //アドバタイズを停止
		        if (mAdvertiser != null) {
		        	mAdvertiser.stopAdvertising(new AdvertiseCallback(){
		        		@Override
		    			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
		    				super.onStartSuccess(settingsInEffect);
		    				showToastAsync(finalActivity, "stop succeeded");
		    			}

		    			@Override
		    			public void onStartFailure(int errorCode) {
		    				super.onStartFailure(errorCode);
		    				showToastAsync(finalActivity, "stop failed : "+errorCode);
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
		});
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}

	public void setGattServer() {

		BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		final PeripheralActivity finalActivity = this;

		gattServer = manager.openGattServer(getApplicationContext(), new BluetoothGattServerCallback() {
			@Override
			public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
					BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
					int offset, byte[] value) {
				super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded,
						offset, value);
				if (value != null) {
					showToastAsync(finalActivity, "get characteristic write request : " + new String(value));
					Log.d("TAG", "value ~ " + new String(value));
				}
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
			}

			@Override
			public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
					BluetoothGattCharacteristic characteristic) {
				super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
				showToastAsync(finalActivity, "get characteristic read request");
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "ABC".getBytes());
			}

			@Override
			public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
				super.onConnectionStateChange(device, status, newState);
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					showToastAsync(finalActivity ,"state changed to connected");
					mDevice = device;
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					showToastAsync(finalActivity, "state changed to disconnected");
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

	public void showToastAsync(final PeripheralActivity activity ,final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (PeripheralActivity.this != null) {
					// showToastAsync(PeripheralActivity.this, text,
					// Toast.LENGTH_SHORT).show();
					TextView textView = ((TextView) (activity.findViewById(R.id.textview_peripheral)));
					textView.setText(textView.getText() + "\n" + text);
				}
			}
		});
	}
}