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

	BluetoothManager mManager;
	BluetoothAdapter mAdapter;
	BluetoothLeScanner mScanner;
	private BluetoothGatt mBleGatt;
	private boolean mIsBluetoothEnable = false;
	private BluetoothGattCharacteristic mCharacteristic;

	private HashMap<String, BluetoothDevice> foundDevices = new HashMap<String, BluetoothDevice>();

	Handler guiThreadHandler = new Handler();

	private String mStrReceivedNum = "";

	private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
	private final static int MESSAGE_NEW_SENDNUM = 1;

	final static int STATE_NONE = 0;
	final static int STATE_SCANNING = 1;
	final static int STATE_PAIRED = 2;
	int state = STATE_NONE;

	static Object bleProcess = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_central);

		state = STATE_NONE;

		mManager = (BluetoothManager) (this.getSystemService(Context.BLUETOOTH_SERVICE));

		mAdapter = mManager.getAdapter();

		if ((mAdapter == null) || (!mAdapter.isEnabled())) {
			this.setResult(MainActivity.RESULT_MADAPTER_IS_NULL);
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			finish();
			return;
		}

		mScanner = mAdapter.getBluetoothLeScanner();

		final CentralActivity activity = this;
		String macAddress = android.provider.Settings.Secure.getString(activity.getContentResolver(),
				"bluetooth_address");
		showToastAsync(finalActivity, "self : " + macAddress);

		foundDevices = new HashMap<String, BluetoothDevice>();
		this.scanPairedDevices();
		this.scanNewDevice();

		this.setListeners(this);
	}

	private void setListeners(final CentralActivity activity) {
		findViewById(R.id.button_stop_scanning).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CentralActivity.this.stopScanning();
			}
		});

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
									CentralActivity.this.state = CentralActivity.STATE_PAIRED;
									synchronized (bleProcess) {
										showToastAsync(finalActivity, "connecting to " + list[arg1]);
										foundDevices.get(list[arg1]).connectGatt(getApplicationContext(), true,
												mGattCallback);
									}
								}
							}).show();
				}
			}
		});

		findViewById(R.id.button_disconnect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (bleProcess) {
					if (mBleGatt == null) {
						showToastAsync(activity, "mBleGatt is null");
						return;
					}

					mBleGatt.disconnect();
				}
			}
		});

		findViewById(R.id.button_send_00_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (bleProcess) {
					if (mBleGatt == null) {
						showToastAsync(activity, "mBleGatt is null");
						return;
					}
					if (mIsBluetoothEnable == false) {
						showToastAsync(activity, "mIsBluetoothEnable is false");
						return;
					}

					byte[] bytes = { 00 };
					mCharacteristic.setValue(bytes);
					mBleGatt.writeCharacteristic(mCharacteristic);
				}
			}
		});

		findViewById(R.id.button_send_01_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (bleProcess) {
					if (mBleGatt == null) {
						showToastAsync(activity, "mBleGatt is null");
						return;
					}
					if (mIsBluetoothEnable == false) {
						showToastAsync(activity, "mIsBluetoothEnable is false");
						return;
					}

					byte[] bytes = { 01 };
					mCharacteristic.setValue(bytes);
					mBleGatt.writeCharacteristic(mCharacteristic);
				}
			}
		});

		findViewById(R.id.button_send_02_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (bleProcess) {
					if (mBleGatt == null) {
						showToastAsync(activity, "mBleGatt is null");
						return;
					}
					if (mIsBluetoothEnable == false) {
						showToastAsync(activity, "mIsBluetoothEnable is false");
						return;
					}

					byte[] bytes = { 02 };
					mCharacteristic.setValue(bytes);
					mBleGatt.writeCharacteristic(mCharacteristic);
				}
			}
		});

		findViewById(R.id.button_send_03_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (bleProcess) {
					if (mBleGatt == null) {
						showToastAsync(activity, "mBleGatt is null");
						return;
					}
					if (mIsBluetoothEnable == false) {
						showToastAsync(activity, "mIsBluetoothEnable is false");
						return;
					}

					byte[] bytes = { 03 };
					mCharacteristic.setValue(bytes);
					mBleGatt.writeCharacteristic(mCharacteristic);
				}
			}
		});

		findViewById(R.id.button_send_04_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (bleProcess) {
					if (mBleGatt == null) {
						showToastAsync(activity, "mBleGatt is null");
						return;
					}
					if (mIsBluetoothEnable == false) {
						showToastAsync(activity, "mIsBluetoothEnable is false");
						return;
					}

					byte[] bytes = { 04 };
					mCharacteristic.setValue(bytes);
					mBleGatt.writeCharacteristic(mCharacteristic);
				}
			}
		});

		findViewById(R.id.button_send_abc_central).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (bleProcess) {
					if (mBleGatt == null) {
						showToastAsync(activity, "mBleGatt is null");
						return;
					}
					if (mIsBluetoothEnable == false) {
						showToastAsync(activity, "mIsBluetoothEnable is false");
						return;
					}

					byte[] bytes = { 'A', 'B', 'C', 0 };
					mCharacteristic.setValue(bytes);
					mBleGatt.writeCharacteristic(mCharacteristic);
				}
			}
		});
	}

	private void scanPairedDevices() {
		showToastAsync(finalActivity, "trying to connect to paired devices");
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> btDevices = btAdapter.getBondedDevices();
		for (BluetoothDevice device : btDevices) {
			synchronized (bleProcess) {
				int type = device.getType();
				if ((type != BluetoothDevice.DEVICE_TYPE_CLASSIC) && mManager.getConnectionState(device,
						BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTING) {
					showToastAsync(finalActivity, "connecting : " + device.getAddress() + " / " + device.getName());
					device.connectGatt(getApplicationContext(), true, mGattCallback);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void scanNewDevice() {
		if (Build.VERSION.SDK_INT >= 5.0) {
			state = STATE_SCANNING;
			this.startScanByBleScanner();
		} else {
			state = STATE_SCANNING;
			mAdapter.startLeScan(mScanCallback);
			showToastAsync(finalActivity, "scanning started");
		}
	}

	private void stopScanning() {
		if (Build.VERSION.SDK_INT >= 5.0) {
			if (mScanner == null) {
				showToastAsync(finalActivity, "mScanner is null");
				return;
			}
			state = STATE_NONE;
			mScanner.stopScan(mLeScanCallback);
			showToastAsync(finalActivity, "scanning stopped");
		} else {
			state = STATE_NONE;
			mAdapter.stopLeScan(mScanCallback);
			showToastAsync(finalActivity, "scanning stopped");
		}
	}

	private void startScanByBleScanner() {
		state = STATE_SCANNING;
		synchronized (bleProcess) {
			mScanner.startScan(mLeScanCallback);
			showToastAsync(finalActivity, "scanning started");
		}
	}

	final ScanCallback mLeScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			super.onScanResult(callbackType, result);
			synchronized (bleProcess) {
				if (result.getDevice() == null) {
					return;
				}
				int type = result.getDevice().getType();
				if ((type != BluetoothDevice.DEVICE_TYPE_CLASSIC) && mManager.getConnectionState(result.getDevice(),
						BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTING) {
					showToastAsync(finalActivity,
							"connecting : " + result.getDevice().getAddress() + " / " + result.getDevice().getName());
					result.getDevice().connectGatt(getApplicationContext(), true, mGattCallback);
				}
			}
		}

		@Override
		public void onScanFailed(int intErrorCode) {
			super.onScanFailed(intErrorCode);

			String description = "";
			if (intErrorCode == ScanCallback.SCAN_FAILED_ALREADY_STARTED) {
				description = "SCAN_FAILED_ALREADY_STARTED";
			} else if (intErrorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
				description = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
			} else if (intErrorCode == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED) {
				description = "SCAN_FAILED_FEATURE_UNSUPPORTED";
			} else if (intErrorCode == ScanCallback.SCAN_FAILED_INTERNAL_ERROR) {
				description = "SCAN_FAILED_INTERNAL_ERROR";
			} else {
				description = "" + intErrorCode;
			}

			showToastAsync(finalActivity, "starting failed : " + description);
		}
	};

	private final LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					synchronized (bleProcess) {
						int type = device.getType();
						if ((type == BluetoothDevice.DEVICE_TYPE_LE || type == BluetoothDevice.DEVICE_TYPE_DUAL)
								&& mManager.getConnectionState(device,
										BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTING) {
							showToastAsync(finalActivity,
									"connecting : " + device.getAddress() + " / " + device.getName());
							BluetoothGatt gatt = device.connectGatt(getApplicationContext(), true, mGattCallback);
						}
					}
				}
			});
		}
	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			synchronized (bleProcess) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					showToastAsync(finalActivity,
							"connected : " + gatt.getDevice().getAddress() + " / " + gatt.getDevice().getName());
					if (gatt.getServices().size() == 0) {
						showToastAsync(finalActivity, "discovering services : " + gatt.getDevice().getAddress() + " / "
								+ gatt.getDevice().getName());
						gatt.discoverServices();
					} else {
						handleServices(gatt);
					}
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					showToastAsync(finalActivity,
							"disconnected : " + gatt.getDevice().getAddress() + " / " + gatt.getDevice().getName());
					if (mBleGatt.getDevice().getAddress().equals(gatt.getDevice().getAddress())) {
						mBleGatt.close();
						mBleGatt = null;
						mIsBluetoothEnable = false;
						finalActivity.setUuidTextAsync(finalActivity, "");
					}
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				handleServices(gatt);
			}
		}

		public void handleServices(BluetoothGatt gatt) {
			BluetoothGattService service = gatt.getService(UUID.fromString(MainActivity.SERVICE_UUID));
			if (service != null) {
				showToastAsync(finalActivity, "found service : " + service.getUuid().toString());
				mCharacteristic = service.getCharacteristic(UUID.fromString(MainActivity.CHAR_UUID));

				if (mCharacteristic != null) {
					showToastAsync(finalActivity, "found char : " + mCharacteristic.getUuid().toString());

					if (CentralActivity.this.state == CentralActivity.STATE_SCANNING) {
						showToastAsync(finalActivity, "added device : " + mBleGatt.getDevice().getAddress() + " / "
								+ mBleGatt.getDevice().getName());
						if (!finalActivity.foundDevices.containsKey(mBleGatt.getDevice().getAddress())) {
							finalActivity.foundDevices.put(mBleGatt.getDevice().getAddress(), mBleGatt.getDevice());
						}

						finalActivity.setUuidTextAsync(finalActivity,
								mBleGatt.getDevice().getAddress() + " / " + mBleGatt.getDevice().getName());
					} else if (CentralActivity.this.state == CentralActivity.STATE_PAIRED) {
						synchronized (bleProcess) {
							mBleGatt = gatt;
							BluetoothGattDescriptor descriptor = mCharacteristic
									.getDescriptor(UUID.fromString(MainActivity.CHAR_CONFIG_UUID));

							descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
							mBleGatt.writeDescriptor(descriptor);
							mIsBluetoothEnable = true;
							showToastAsync(finalActivity, "paired : " + mBleGatt.getDevice().getAddress() + " / "
									+ mBleGatt.getDevice().getName());

							mBleGatt.getDevice().createBond();
							CentralActivity.this.state = CentralActivity.STATE_NONE;
						}
					}
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
		if (mScanner != null) {
			mScanner.stopScan(mLeScanCallback);
		}
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
