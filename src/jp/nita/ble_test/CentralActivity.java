package jp.nita.ble_test;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.content.DialogInterface;
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
	
	private HashMap<String, BluetoothDevice> foundDevices = new HashMap<String, BluetoothDevice>();

	Handler guiThreadHandler = new Handler();

	private String mStrReceivedNum = "";

	private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
	private final static int MESSAGE_NEW_SENDNUM = 1;

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

		foundDevices = new HashMap<String, BluetoothDevice>();
		this.scanPairedDevices();
		this.scanNewDevice();

		final CentralActivity activity = this;

		findViewById(R.id.button_re_scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CentralActivity.this.scanPairedDevices();
				CentralActivity.this.scanNewDevice();
			}
		});
		
		findViewById(R.id.button_connect_to_a_found_device).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (foundDevices.size() <= 0) {
					showToastAsync(finalActivity, "no devices found");
				} else {
					final String list[] = new String[foundDevices.size()];
					int i = 0;
					for (String address : foundDevices.keySet()) {
						list[i] = address;
						i++;
					}
					new AlertDialog.Builder(finalActivity).setTitle("Select device")
					.setItems(list, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							showToastAsync(finalActivity, "connecting to " + list[arg1]);
							foundDevices.get(list[arg1]).connectGatt(getApplicationContext(), true, mGattCallback);
						}
					}).show();
				}
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
		
		findViewById(R.id.button_send_02_central).setOnClickListener(new View.OnClickListener() {
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

				byte[] bytes = { 02 };
				mBleCharacteristic.setValue(bytes);
				mBleGatt.writeCharacteristic(mBleCharacteristic);
			}
		});
		
		findViewById(R.id.button_send_03_central).setOnClickListener(new View.OnClickListener() {
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

				byte[] bytes = { 03 };
				mBleCharacteristic.setValue(bytes);
				mBleGatt.writeCharacteristic(mBleCharacteristic);
			}
		});
		
		findViewById(R.id.button_send_04_central).setOnClickListener(new View.OnClickListener() {
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

				byte[] bytes = { 04 };
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
	
	private void scanPairedDevices() {
		showToastAsync(finalActivity, "trying to connect to paired devices");
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> btDevices = btAdapter.getBondedDevices();
		for (BluetoothDevice device : btDevices) {
			int type = device.getType();
			if (type == BluetoothDevice.DEVICE_TYPE_LE || type == BluetoothDevice.DEVICE_TYPE_DUAL) {
				showToastAsync(finalActivity, "connecting : " + device.getAddress() + " / " + device.getName());
				device.connectGatt(getApplicationContext(), false, mGattCallback);
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
				showToastAsync(finalActivity, "connecting : " + result.getDevice().getAddress() + " / " + result.getDevice().getName());
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
				mBluetoothLeScanner.stopScan(scanCallback);
				showToastAsync(finalActivity, "scanning stopped");
			}
		}, 10000);

		mBluetoothLeScanner.startScan(scanCallback);
		showToastAsync(finalActivity, "scanning started");
	}

	private final LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showToastAsync(finalActivity, "connecting to scanned " + device.getAddress() + " / " + device.getName());
					BluetoothGatt gatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
				}
			});
		}
	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				showToastAsync(finalActivity, "connected : " + gatt.getDevice().getAddress() + " / " + gatt.getDevice().getName());
				if (gatt.getServices().size() == 0) {
					gatt.discoverServices();
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				showToastAsync(finalActivity, "disconnected : " + gatt.getDevice().getAddress() + " / " + gatt.getDevice().getName());
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
				BluetoothGattService service = gatt.getService(UUID.fromString(MainActivity.SERVICE_UUID));
				if (service != null) {
					showToastAsync(finalActivity, "found service : " + service.getUuid().toString());
					mBleCharacteristic = service.getCharacteristic(UUID.fromString(MainActivity.CHAR_UUID));

					if (mBleCharacteristic != null) {
						showToastAsync(finalActivity, "found char : " + mBleCharacteristic.getUuid().toString());
						mBleGatt = gatt;
						// TODO: スキャンしているだけの時はいきなりgattに登録しない

						BluetoothGattDescriptor descriptor = mBleCharacteristic
								.getDescriptor(UUID.fromString(MainActivity.CHAR_CONFIG_UUID));

						descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						mBleGatt.writeDescriptor(descriptor);
						mIsBluetoothEnable = true;
						showToastAsync(finalActivity, "char matches : "  + mBleGatt.getDevice().getAddress() + " / " + mBleGatt.getDevice().getName());
						
						if (!finalActivity.foundDevices.containsKey(mBleGatt.getDevice().getAddress())) {
							finalActivity.foundDevices.put(mBleGatt.getDevice().getAddress(), mBleGatt.getDevice());
						}

						finalActivity.setUuidTextAsync(finalActivity, mBleGatt.getDevice().getAddress() + " / " + mBleGatt.getDevice().getName());
					} else {
						gatt.disconnect();
					}
				} else {
					gatt.disconnect();
				}
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if (MainActivity.CHAR_UUID.equals(characteristic.getUuid().toString().toUpperCase())) {
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
