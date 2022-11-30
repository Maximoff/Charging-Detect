package ru.maximoff.charging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

public class ChargingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if (!pref.getBoolean("service", true)) {
			return;
		}
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean pluggedBattery = (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB);
		boolean statusCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
		boolean charging = pref.getBoolean("charging", false);
		if (pluggedBattery && statusCharging) {
			if (!charging) {
				Intent serv = new Intent(context, SoundService.class);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					context.startForegroundService(serv);
				} else {
					context.startService(serv);
				}
				pref.edit().putBoolean("charging", true).commit();
			}
        } else {
			if (charging) {
				Intent serv = new Intent(context, SoundService.class);
				serv.putExtra("stop", true);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					context.startForegroundService(serv);
				} else {
					context.startService(serv);
				}
				pref.edit().putBoolean("charging", false).commit();
			}
		}
    }
}
