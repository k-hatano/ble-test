package jp.nita.ble_test;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class CentralActivity extends Activity {

	BluetoothAdapter mBluetoothAdapter;
	BluetoothLeScanner mBluetoothLeScanner;
	private BluetoothGatt mBleGatt;
	private boolean mIsBluetoothEnable = false;
	private BluetoothGattCharacteristic mBleCharacteristic;

	Handler guiThreadHandler = new Handler();

	private String mStrReceivedNum = "";

	private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
	private final static int MESSAGE_NEW_SENDNUM = 1;

	private static final String SERVICE_UUID = "7865087B-D9D0-423A-9C80-042D9BBEA524";
	private static final String CHAR_UUID = "608072DD-6825-4293-B3E7-324CF0B5CA08";
	private static final String CHAR_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_central);

		BluetoothManager bluetoothManager = (BluetoothManager) (this.getSystemService(Context.BLUETOOTH_SERVICE));

		mBluetoothAdapter = bluetoothManager.getAdapter();
		mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

		if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			return;
		}

		this.scanNewDevice();

		final CentralActivity activity = this;

		findViewById(R.id.button_re_scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CentralActivity.this.scanNewDevice();
			}
		});
		
		findViewById(R.id.button_disconnect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBleGatt == null) {
					showToastAsync(activity, "mBleGatt is null");
					return;
				}
				
				mBleGatt.disconnect();
			}
		});

		findViewById(R.id.button_send_00_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBleGatt == null) {
					showToastAsync(activity, "mBleGatt is null");
					return;
				}
				if (mIsBluetoothEnable == false) {
					showToastAsync(activity, "mIsBluetoothEnable is false");
					return;
				}

				byte[] bytes = { 00 };
				mBleCharacteristic.setValue(bytes);
				mBleGatt.writeCharacteristic(mBleCharacteristic);
			}
		});

		findViewById(R.id.button_send_01_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBleGatt == null) {
					showToastAsync(activity, "mBleGatt is null");
					return;
				}
				if (mIsBluetoothEnable == false) {
					showToastAsync(activity, "mIsBluetoothEnable is false");
					return;
				}

				byte[] bytes = { 01 };
				mBleCharacteristic.setValue(bytes);
				mBleGatt.writeCharacteristic(mBleCharacteristic);
			}
		});

		findViewById(R.id.button_send_abc_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBleGatt == null) {
					showToastAsync(activity, "mBleGatt is null");
					return;
				}
				if (mIsBluetoothEnable == false) {
					showToastAsync(activity, "mIsBluetoothEnable is false");
					return;
				}

				byte[] bytes = { 'A', 'B', 'C', 0 };
				mBleCharacteristic.setValue(bytes);
				mBleGatt.writeCharacteristic(mBleCharacteristic);
			}
		});
	}

	private void scanNewDevice() {
		if (Build.VERSION.SDK_INT >= 5.0) {
			this.startScanByBleScanner();
		} else {
			guiThreadHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mBluetoothAdapter.stopLeScan(mScanCallback);
				}
			}, 10000);

			mBluetoothAdapter.startLeScan(mScanCallback);
		}
	}

	private void startScanByBleScanner() {
		mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
		final ScanCallback scanCallback = new ScanCallback() {
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				super.onScanResult(callbackType, result);
				result.getDevice().connectGatt(getApplicationContext(), true, mGattCallback);
			}

			@Override
			public void onScanFailed(int intErrorCode) {
				super.onScanFailed(intErrorCode);
			}
		};

		guiThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				showToastAsync(finalActivity, "scanning stopped");
				mBluetoothLeScanner.stopScan(scanCallback);
			}
		}, 10000);

		showToastAsync(finalActivity, "scanning started");
		mBluetoothLeScanner.startScan(scanCallback);
	}

	private final LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					BluetoothGatt gatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
					showToastAsync(finalActivity, "scanning gatt : " + gatt.getDevice().getAddress());
				}
			});
		}
	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				showToastAsync(finalActivity, "discover services : " + gatt.getDevice().getAddress());
				gatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				showToastAsync(finalActivity, "disconnected : " + gatt.getDevice().getAddress());
				if (mBleGatt.getDevice().getAddress().equals(gatt.getDevice().getAddress())) {
					mBleGatt = null;
					mIsBluetoothEnable = false;
					finalActivity.setUuidTextAsync(finalActivity, "");
				}
				gatt.close();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				BluetoothGattService service = gatt.getService(UUID.fromString(CentralActivity.SERVICE_UUID));
				if (service != null) {
					showToastAsync(finalActivity, "found service : " + service.getUuid().toString());
					mBleCharacteristic = service.getCharacteristic(UUID.fromString(CentralActivity.CHAR_UUID));

					if (mBleCharacteristic != null) {
						showToastAsync(finalActivity, "found char : " + mBleCharacteristic.getUuid().toString());
						mBleGatt = gatt;
						boolean registered = mBleGatt.setCharacteristicNotification(mBleCharacteristic, true);

						BluetoothGattDescriptor descriptor = mBleCharacteristic
								.getDescriptor(UUID.fromString(CentralActivity.CHAR_CONFIG_UUID));

						descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						mBleGatt.writeDescriptor(descriptor);
						mIsBluetoothEnable = true;
						showToastAsync(finalActivity, "char matches : " + mBleGatt.getDevice().getName());

						finalActivity.setUuidTextAsync(finalActivity, gatt.getDevice().getAddress());
					}
				} else {
					gatt.disconnect();
				}
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if (CentralActivity.CHAR_UUID.equals(characteristic.getUuid().toString().toUpperCase())) {
				mStrReceivedNum = characteristic.getStringValue(0);
				showToastAsync(finalActivity, "char changed : " + mStrReceivedNum);
				mBleHandler.sendEmptyMessage(MESSAGE_NEW_RECEIVEDNUM);
			}		
		}
	};

	@Override
	protected void onDestroy() {
		mIsBluetoothEnable = false;
		if (mBleGatt != null) {
			mBleGatt.close();
			mBleGatt = null;
		}
		super.onDestroy();
	}

	final CentralActivity finalActivity = this;

	private Handler mBleHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_NEW_RECEIVEDNUM:
				showToastAsync(finalActivity, "received : " + msg);
				break;
			case MESSAGE_NEW_SENDNUM:
				showToastAsync(finalActivity, "sended : " + msg);
				break;
			}
		}
	};

	public void showToastAsync(final CentralActivity activity, final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (CentralActivity.this != null) {
					TextView textView = ((TextView) (activity.findViewById(R.id.textview_central)));
					String newString = text + "\n" + textView.getText();
					textView.setText(newString);
				}
			}
		});
	}

	public void setUuidTextAsync(final CentralActivity activity, final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (CentralActivity.this != null) {
					TextView textView = ((TextView) (activity.findViewById(R.id.textview_central_uuid)));
					textView.setText(text);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.central, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
