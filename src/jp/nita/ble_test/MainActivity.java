package jp.nita.ble_test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static final String SERVICE_UUID = "7865087B-D9D0-423A-9C80-042D9BBEA524";
	public static final String CHAR_UUID = "608072DD-6825-4293-B3E7-324CF0B5CA08";
	public static final String CHAR_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.button_central).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CentralActivity.class);
				startActivity(intent);
			}
			
		});

		findViewById(R.id.button_peripheral).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, PeripheralActivity.class);
				startActivity(intent);
			}
			
		});
		
		((TextView)findViewById(R.id.textview_service_uuid)).setText("Service : " + SERVICE_UUID);
		((TextView)findViewById(R.id.textview_characteristic_uuid)).setText("Characteristic : " + CHAR_UUID);

		BluetoothManager bluetoothManager = (BluetoothManager) (this.getSystemService(Context.BLUETOOTH_SERVICE));
		BluetoothAdapter adapter = bluetoothManager.getAdapter();

		if ((adapter == null) || (!adapter.isEnabled())) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			return;
		}
	}

}