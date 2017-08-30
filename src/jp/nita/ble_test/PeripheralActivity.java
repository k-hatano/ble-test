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

import java.util.List;
import java.util.UUID;

public class PeripheralActivity extends Activity {

	BluetoothGattServer gattServer;
	BluetoothDevice mDevice;
	BluetoothGattCharacteristic mCharacteristic;
	BluetoothLeAdvertiser mAdvertiser;
	AdvertiseCallback mAdvertiseCallback;
	
	int peripheralAdvertiseMode;
	int peripheralTxPower;

	Handler guiThreadHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peripheral);

		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

		if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())) {
			this.setResult(MainActivity.RESULT_MADAPTER_IS_NULL);
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			finish();
			return;
		}

		final PeripheralActivity activity = this;
		String macAddress = android.provider.Settings.Secure.getString(activity.getContentResolver(),
				"bluetooth_address");
		showToastAsync(activity, "self : " + macAddress);

		if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
			showToastAsync(this, "multi advertisement not supported");
		}
		if (!mBluetoothAdapter.isOffloadedFilteringSupported()) {
			showToastAsync(this, "offload filtering not supported");
		}
		if (!mBluetoothAdapter.isOffloadedScanBatchingSupported()) {
			showToastAsync(this, "offloaded scan batching not supported");
		}

		mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
		if (mAdvertiser == null) {
			this.setResult(MainActivity.RESULT_MADVERTISER_IS_NULL);
			finish();
			return;
		}
		
		int advertiseMode = 0;
		switch (peripheralAdvertiseMode) {
		case Statics.SETTING_PERIPHERAL_ADVERTISE_MODE_BALANCED:
			showToastAsync(activity, "advertise mode : balanced");
			advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
			break;
		case Statics.SETTING_PERIPHERAL_ADVERTISE_MODE_LOW_LATENCY:
			showToastAsync(activity, "advertise mode : low latency");
			advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
			break;
		case Statics.SETTING_PERIPHERAL_ADVERTISE_MODE_LOW_POWER:
			showToastAsync(activity, "advertise mode : low power");
			advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
			break;
		default:
			showToastAsync(activity, "advertise mode : balanced");
			advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
			break;
		}
		
		int txPower = 0;
		switch (peripheralTxPower) {
		case Statics.SETTING_PERIPHERAL_TX_POWER_HIGH:
			showToastAsync(activity, "tx power : high");
			txPower = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
			break;
		case Statics.SETTING_PERIPHERAL_TX_POWER_LOW:
			showToastAsync(activity, "tx power : low");
			txPower = AdvertiseSettings.ADVERTISE_TX_POWER_LOW;
			break;
		case Statics.SETTING_PERIPHERAL_TX_POWER_MEDIUM:
			showToastAsync(activity, "tx power : medium");
			txPower = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
			break;
		case Statics.SETTING_PERIPHERAL_TX_POWER_ULTRA_LOW:
			showToastAsync(activity, "tx power : ultra low");
			txPower = AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW;
			break;
		default:
			showToastAsync(activity, "tx power : high");
			txPower = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
			break;
		}

		AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
		settingBuilder.setAdvertiseMode(advertiseMode);
		settingBuilder.setConnectable(true);
		settingBuilder.setTimeout(100000);
		settingBuilder.setTxPowerLevel(txPower);
		AdvertiseSettings settings = settingBuilder.build();

		AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
		dataBuilder.addServiceUuid(new ParcelUuid(UUID.fromString(MainActivity.SERVICE_UUID)));
		dataBuilder.setIncludeDeviceName(false);
		AdvertiseData advertiseData = dataBuilder.build();

		gattServer = setGattServer();
		mAdvertiseCallback = new AdvertiseCallback() {
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				super.onStartSuccess(settingsInEffect);
				showToastAsync(activity, "started advertising");
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

				showToastAsync(activity, "starting failed : " + description);
			};
		};

		showToastAsync(activity, "starting advertising");
		mAdvertiser.startAdvertising(settings, advertiseData, mAdvertiseCallback);

		this.setListeners(this);
	}
	
	private void setListeners(final PeripheralActivity activity) {
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
				byte[] bytes = { 00 };
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
				byte[] bytes = { 01 };
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
				byte[] bytes = { 'a', 'b', 'c', 0 };
				mCharacteristic.setValue(bytes);
				gattServer.notifyCharacteristicChanged(mDevice, mCharacteristic, false);
			}
		});

		findViewById(R.id.button_stop_advertising).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gattServer != null) {
					if (mDevice != null) {
						showToastAsync(activity, "disconnecting device : " + mDevice.getAddress());
						gattServer.cancelConnection(mDevice);
					}
					
					gattServer.clearServices();
					gattServer.close();
					gattServer = null;
				} else {
					showToastAsync(activity, "gattServer is null");
				}

				if (mAdvertiser != null) {
					mAdvertiser.stopAdvertising(mAdvertiseCallback);
					showToastAsync(activity, "stopped advertising");
					mAdvertiser = null;
				} else {
					showToastAsync(activity, "mAdvertiser is null");
				}
			}
		});
		
		findViewById(R.id.button_disconnect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDevice != null) {
					showToastAsync(activity, "disconnecting device : " + mDevice.getAddress());
					gattServer.cancelConnection(mDevice);
				} else {
					showToastAsync(activity, "gattServer is null");
				}
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();

		if (gattServer != null) {
			gattServer.clearServices();
			gattServer.close();
			gattServer = null;
		}

		if (mAdvertiser != null) {
			mAdvertiser.stopAdvertising(mAdvertiseCallback);
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
			mAdvertiser.stopAdvertising(mAdvertiseCallback);
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
					showToastAsync(finalActivity, "connected : " + device.getAddress() + " / " + device.getName());
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

	public void showToastAsync(final PeripheralActivity activity, final String text) {
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

	public void setUuidTextAsync(final PeripheralActivity activity, final String text) {
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
	
	public void updatePreferenceValues() {
		peripheralAdvertiseMode = Statics.preferenceValue(this, Statics.SETTING_PERIPHERAL_ADVERTISE_MODE, 0);
		peripheralTxPower = Statics.preferenceValue(this, Statics.SETTING_PERIPHERAL_TX_POWER, 0);
	}
}