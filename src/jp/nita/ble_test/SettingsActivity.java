package jp.nita.ble_test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends Activity {

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
	}
}
