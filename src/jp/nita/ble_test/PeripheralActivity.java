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
import android.content.Intent;
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

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peripheral);

		BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter adapter = manager.getAdapter();
		
		if ((adapter == null) || (!adapter.isEnabled())) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			return;
		}
		
		if (!adapter.isMultipleAdvertisementSupported()) {
			showToastAsync(this, "multi advertisement not supported");
		}
		if (!adapter.isOffloadedFilteringSupported()) {
			showToastAsync(this, "offload filtering not supported");
		}
		if (!adapter.isOffloadedScanBatchingSupported()) {
			showToastAsync(this, "offloaded scan batching not supported");
		}
		
		mAdvertiser = adapter.getBluetoothLeAdvertiser();
		if (mAdvertiser == null) {
			showToastAsync(this, "mAdvertiser is null");
			finish();
			return;
		}

		AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
		settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
		settingBuilder.setConnectable(true);
		settingBuilder.setTimeout(10000);
		settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
		AdvertiseSettings settings = settingBuilder.build();

		AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
		dataBuilder.addServiceUuid(new ParcelUuid(UUID.fromString(MainActivity.SERVICE_UUID)));
		dataBuilder.setIncludeDeviceName(false);
		AdvertiseData advertiseData = dataBuilder.build();

		gattServer = setGattServer();
		final PeripheralActivity activity = this;
		final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				super.onStartSuccess(settingsInEffect);
				showToastAsync(activity, "started");
			}

			@Override
			public void onStartFailure(int errorCode) {
				super.onStartFailure(errorCode);
				
				String description = "";
				if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
					description = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
				} else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
					description = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
				} else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
					description = "ADVERTISE_FAILED_ALREADY_STARTED";
				} else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE) {
					description = "ADVERTISE_FAILED_DATA_TOO_LARGE";
				} else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR) {
					description = "ADVERTISE_FAILED_INTERNAL_ERROR";
				} else {
					description = "" + errorCode;
				}

				showToastAsync(activity, "start failed : "+description);
			};
		};
		
		mAdvertiser.stopAdvertising(advertiseCallback);
		mAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback);

		findViewById(R.id.button_send_00).setOnClickListener(new View.OnClickListener() {
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
				byte[] bytes = {00};
				mCharacteristic.setValue(bytes);
				gattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);
			}
		});
		
		findViewById(R.id.button_send_01).setOnClickListener(new View.OnClickListener() {
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
				byte[] bytes = {01};
				mCharacteristic.setValue(bytes);
				gattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);
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
				byte[] bytes = {'a', 'b', 'c', 0};
				mCharacteristic.setValue(bytes);
				gattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);
			}
		});
		
		final PeripheralActivity finalActivity = this;
		findViewById(R.id.button_stop_advertising).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        if (gattServer != null) {
		            gattServer.clearServices();
		            gattServer.close();
		            gattServer = null;
		        }
		        
		        if (mAdvertiser != null) {
		        	mAdvertiser.stopAdvertising(new AdvertiseCallback(){
		        		@Override
		    			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
		    				super.onStartSuccess(settingsInEffect);
		    				showToastAsync(finalActivity, "stopped");
		    			}

		    			@Override
		    			public void onStartFailure(int errorCode) {
		    				super.onStartFailure(errorCode);
		    				showToastAsync(finalActivity, "stop failed : "+errorCode);
		    			}
		        	});
		        	mAdvertiser = null;
		        }
			}
		});
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		final PeripheralActivity finalActivity = this;
		
		if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
        }
		
        if (mAdvertiser != null) {
        	mAdvertiser.stopAdvertising(new AdvertiseCallback(){
        		@Override
    			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
    				super.onStartSuccess(settingsInEffect);
    				showToastAsync(finalActivity, "stopped");
    			}

    			@Override
    			public void onStartFailure(int errorCode) {
    				super.onStartFailure(errorCode);
    				showToastAsync(finalActivity, "stop failed : "+errorCode);
    			}
        	});
        	mAdvertiser = null;
        }
	}
	
	@Override
	protected void onDestroy() {
		if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
        }
		
        if (mAdvertiser != null) {
        	mAdvertiser.stopAdvertising(new AdvertiseCallback(){
        		@Override
    			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
    				super.onStartSuccess(settingsInEffect);
    			}

    			@Override
    			public void onStartFailure(int errorCode) {
    				super.onStartFailure(errorCode);
    			}
        	});
        	mAdvertiser = null;
        }
		
		super.onDestroy();
	}

	public BluetoothGattServer setGattServer() {

		BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		final PeripheralActivity finalActivity = this;

		BluetoothGattServer gatt = manager.openGattServer(getApplicationContext(), new BluetoothGattServerCallback() {
			@Override
			public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
					BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
					int offset, byte[] value) {
				super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded,
						offset, value);
				if (value != null) {
					showToastAsync(finalActivity, "get char write request : " + new String(value));
					Log.d("TAG", "value ~ " + new String(value));
				}
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
			}

			@Override
			public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
					BluetoothGattCharacteristic characteristic) {
				super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
				showToastAsync(finalActivity, "get char read request");
				gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "ABC".getBytes());
			}

			@Override
			public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
				super.onConnectionStateChange(device, status, newState);
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					showToastAsync(finalActivity ,"connected : " + device.getAddress() + " / " + device.getName());
					finalActivity.setUuidTextAsync(finalActivity, device.getAddress() + " / " + device.getName());
					mDevice = device;
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					showToastAsync(finalActivity, "disconnected : " + device.getAddress() + " / " + device.getName());
					finalActivity.setUuidTextAsync(finalActivity, "");
					mDevice = null;
				}
			}
		});
		
		if (gatt == null) {
			showToastAsync(finalActivity, "making gattServer failed");
			return null;
		}

		BluetoothGattService service = new BluetoothGattService(UUID.fromString(MainActivity.SERVICE_UUID),
				BluetoothGattService.SERVICE_TYPE_PRIMARY);
		mCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(MainActivity.CHAR_UUID),
				BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ
						| BluetoothGattCharacteristic.PROPERTY_WRITE,
				BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
		service.addCharacteristic(mCharacteristic);
		gatt.addService(service);
		
		return gatt;
	}

	public void showToastAsync(final PeripheralActivity activity ,final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (PeripheralActivity.this != null) {
					TextView textView = ((TextView) (activity.findViewById(R.id.textview_peripheral)));
					String newString = text + "\n" + textView.getText();
					textView.setText(newString);
				}
			}
		});
	}
	
	public void setUuidTextAsync(final PeripheralActivity activity ,final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (PeripheralActivity.this != null) {
					TextView textView = ((TextView) (activity.findViewById(R.id.textview_peripheral_uuid)));
					textView.setText(text);
				}
			}
		});
	}
}