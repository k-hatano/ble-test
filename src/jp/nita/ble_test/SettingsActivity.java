package jp.nita.ble_test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SettingsActivity extends Activity implements OnItemClickListener {

	int peripheralAdvertiseMode;
	int peripheralTxPower;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		updatePreferenceValues();
	}

	public void updatePreferenceValues() {
		peripheralAdvertiseMode = Statics.preferenceValue(this, Statics.SETTING_PERIPHERAL_ADVERTISE_MODE, 0);
		peripheralTxPower = Statics.preferenceValue(this, Statics.SETTING_PERIPHERAL_TX_POWER, 0);
	}

	public void updateSettingsListView() {
		ListView items = (ListView) findViewById(R.id.listview_settings);
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		{
			Map<String, String> map;

			map = new HashMap<String, String>();
			map.put("key", getString(R.string.settings_peripheral_advertise_mode));
			map.put("value", Statics.getPeripheralAdvertiseMode(peripheralAdvertiseMode));
			list.add(map);

			map = new HashMap<String, String>();
			map.put("key", getString(R.string.settings_peripheral_tx_power));
			map.put("value", Statics.getPeripheralTxPower(peripheralTxPower));
			list.add(map);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_expandable_list_item_2,
				new String[] { "key", "value" }, new int[] { android.R.id.text1, android.R.id.text2 });
		items.setAdapter(adapter);

		items.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		if (parent == findViewById(R.id.listview_settings)) {
			switch (position) {
			case 0: {
				CharSequence list[] = new String[3];
				for (int i = 0; i < 3; i++) {
					list[i] = Statics.getPeripheralAdvertiseMode(i);
				}
				new AlertDialog.Builder(SettingsActivity.this)
						.setTitle(getString(R.string.settings_peripheral_advertise_mode))
						.setSingleChoiceItems(list, peripheralAdvertiseMode, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								peripheralAdvertiseMode = arg1;
								Statics.setPreferenceValue(SettingsActivity.this,
										Statics.SETTING_PERIPHERAL_ADVERTISE_MODE, peripheralAdvertiseMode);
								updatePreferenceValues();
								updateSettingsListView();
								arg0.dismiss();
								((ListView) findViewById(R.id.listview_settings)).setSelection(position);
							}
						}).show();
				break;

			}
			case 1: {
				CharSequence list[] = new String[4];
				for (int i = 0; i < 4; i++) {
					list[i] = Statics.getPeripheralTxPower(i);
				}
				new AlertDialog.Builder(SettingsActivity.this)
						.setTitle(getString(R.string.settings_peripheral_tx_power))
						.setSingleChoiceItems(list, peripheralTxPower, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								peripheralTxPower = arg1;
								Statics.setPreferenceValue(SettingsActivity.this, Statics.SETTING_PERIPHERAL_TX_POWER,
										peripheralTxPower);
								updatePreferenceValues();
								updateSettingsListView();
								arg0.dismiss();
								((ListView) findViewById(R.id.listview_settings)).setSelection(position);
							}
						}).show();
				break;
			}
			default:
				break;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		updatePreferenceValues();
		updateSettingsListView();
	}
}
