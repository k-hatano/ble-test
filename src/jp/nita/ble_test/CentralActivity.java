package jp.nita.ble_test;

import java.util.Timer;
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
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class CentralActivity extends Activity {

	BluetoothAdapter mBluetoothAdapter;
	BluetoothLeScanner mBluetoothLeScanner;
	private BluetoothGatt mBleGatt;
	private boolean mIsBluetoothEnable = false;
	private BluetoothGattCharacteristic mBleCharacteristic;

	Handler guiThreadHandler = new Handler();
	
	private String mStrReceivedNum = "";
	private Timer mTimer;

	private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
	private final static int MESSAGE_NEW_SENDNUM = 1;

	private static final String SERVICE_UUID = "0A917941-40E4-40E8-81B8-146FD1F2479A";
	private static final String CHAR_UUID = "0015D5AE-2653-4BB1-8EE1-AF566EE846DC";
	private static final String CHAR_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

	private final LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mBleGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
				}
			});
		}
	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				showToastAsync(finalActivity, "connected");
				gatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				showToastAsync(finalActivity, "disconnected");
				if (mBleGatt != null) {
					mBleGatt.close();
					mBleGatt = null;
				}
				mIsBluetoothEnable = false;
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				BluetoothGattService service = gatt.getService(UUID.fromString(CentralActivity.SERVICE_UUID));
				if (service != null) {
					showToastAsync(finalActivity, "found service : " + CentralActivity.SERVICE_UUID);
					mBleCharacteristic = service.getCharacteristic(UUID.fromString(CentralActivity.CHAR_UUID));

					if (mBleCharacteristic != null) {
						showToastAsync(finalActivity, "found characteristic : " + CentralActivity.CHAR_UUID);
						mBleGatt = gatt;

						boolean registered = mBleGatt.setCharacteristicNotification(mBleCharacteristic, true);

						BluetoothGattDescriptor descriptor = mBleCharacteristic
								.getDescriptor(UUID.fromString(CentralActivity.CHAR_CONFIG_UUID));

						descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						mBleGatt.writeDescriptor(descriptor);
						mIsBluetoothEnable = true;
					}
				}
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if (CentralActivity.CHAR_UUID.equals(characteristic.getUuid().toString().toUpperCase())) {
				showToastAsync(finalActivity, "characteristic changed : " + CentralActivity.CHAR_UUID);
				mStrReceivedNum = characteristic.getStringValue(0);
				mBleHandler.sendEmptyMessage(MESSAGE_NEW_RECEIVEDNUM);
			}
		}
	};

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_central);

		BluetoothManager bluetoothManager = (BluetoothManager) (this.getSystemService(Context.BLUETOOTH_SERVICE));

		mBluetoothAdapter = bluetoothManager.getAdapter();
		mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

		this.scanNewDevice();
	}

	private void scanNewDevice() {
		mBluetoothAdapter.startLeScan(mScanCallback);
	}

	@Override
	protected void onDestroy() {
		// 画面遷移時は通信を切断する.
		mIsBluetoothEnable = false;
		if (mBleGatt != null) {
			mBleGatt.close();
			mBleGatt = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.central, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showToastAsync(final CentralActivity activity ,final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (CentralActivity.this != null) {
					// Toast.makeText(PeripheralActivity.this, text,
					// Toast.LENGTH_SHORT).show();
					TextView textView = ((TextView) (activity.findViewById(R.id.textview_central)));
					textView.setText(textView.getText() + "\n" + text);
				}
			}
		});
	}
}
