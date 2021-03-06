package jp.nita.ble_test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Statics {

	public static final String PREF_KEY = "ble-test";
	public static final String SETTING_CENTRAL_AUTO_CONNECT = "centralAutoConnect";
	public static final String SETTING_PERIPHERAL_ADVERTISE_MODE = "peripheralAdvertiseMode";
	public static final String SETTING_PERIPHERAL_TX_POWER = "centralTxPower";
	public static final String SETTING_PERIPHERAL_INCLUDE_DEVICE_NAME = "peripheralIncludeDeviceName";
	
	public static final int SETTING_CENTRAL_AUTO_CONNECT_FALSE = 0;
	public static final int SETTING_CENTRAL_AUTO_CONNECT_TRUE = 1;
	public static final int SETTING_PERIPHERAL_ADVERTISE_MODE_BALANCED = 0;
	public static final int SETTING_PERIPHERAL_ADVERTISE_MODE_LOW_LATENCY = 1;
	public static final int SETTING_PERIPHERAL_ADVERTISE_MODE_LOW_POWER = 2;
	public static final int SETTING_PERIPHERAL_TX_POWER_HIGH = 0;
	public static final int SETTING_PERIPHERAL_TX_POWER_LOW = 1;
	public static final int SETTING_PERIPHERAL_TX_POWER_MEDIUM = 2;
	public static final int SETTING_PERIPHERAL_TX_POWER_ULTRA_LOW = 3;

	public static String getCentralAutoConnect(int which) {
		switch (which) {
		case Statics.SETTING_CENTRAL_AUTO_CONNECT_FALSE:
			return "false";
		case Statics.SETTING_CENTRAL_AUTO_CONNECT_TRUE:
			return "true";
		default:
			return "???";
		}
	}
	
	public static String getPeripheralAdvertiseMode(int which) {
		switch (which) {
		case Statics.SETTING_PERIPHERAL_ADVERTISE_MODE_BALANCED:
			return "ADVERTISE_MODE_BALANCED";
		case Statics.SETTING_PERIPHERAL_ADVERTISE_MODE_LOW_LATENCY:
			return "ADVERTISE_MODE_LOW_LATENCY";
		case Statics.SETTING_PERIPHERAL_ADVERTISE_MODE_LOW_POWER:
			return "ADVERTISE_MODE_LOW_POWER";
		default:
			return "???";
		}
	}
	
	public static String getPeripheralTxPower(int which) {
		switch (which) {
		case Statics.SETTING_PERIPHERAL_TX_POWER_HIGH:
			return "ADVERTISE_TX_POWER_HIGH";
		case Statics.SETTING_PERIPHERAL_TX_POWER_LOW:
			return "ADVERTISE_TX_POWER_LOW";
		case Statics.SETTING_PERIPHERAL_TX_POWER_MEDIUM:
			return "ADVERTISE_TX_POWER_MEDIUM";
		case Statics.SETTING_PERIPHERAL_TX_POWER_ULTRA_LOW:
			return "ADVERTISE_TX_POWER_ULTRA_LOW";
		default:
			return "???";
		}
	}
	
	public static String getIncludeDeviceName(Context context, int which) {
		switch (which) {
		case 0:
			return context.getString(R.string.no);
		default:
			return context.getString(R.string.yes);
		}
	}
	
	public static int preferenceValue(Context context, String key, int def) {
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
		return pref.getInt(key, def);
	}

	public static void setPreferenceValue(Context context, String key, int val) {
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(key, val);
		editor.commit();
	}
	
}
