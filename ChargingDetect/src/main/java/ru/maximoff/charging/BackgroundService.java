package ru.maximoff.charging;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class BackgroundService extends Service {
	private ChargingReceiver chargeDetector;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		NotificationMan nManager = new NotificationMan(this);
		nManager.silent();
		Notification n = nManager.sendNotification(getString(R.string.background));
		startForeground(nManager.getId(), n);
		if ((intent != null && intent.hasExtra("stop")) || !PreferenceManager.getDefaultSharedPreferences(this).getBoolean("service", true)) {
			stopForeground(true);
			stopSelf();
		} else {
			if (chargeDetector != null) {
				unregisterReceiver(chargeDetector);
				chargeDetector = null;
			}
			chargeDetector = new ChargingReceiver();
			IntentFilter filter = new IntentFilter();
			filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
//			filter.addAction(Intent.ACTION_POWER_CONNECTED);
//			filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
			registerReceiver(chargeDetector, filter);
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (chargeDetector != null) {
			unregisterReceiver(chargeDetector);
			chargeDetector = null;
		}
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("service", true)) {
			Intent intent = new Intent(BackgroundService.this, BootReceiver.class);
			intent.setAction("ru.maximoff.charging.RESTART_SERVICE");
			sendBroadcast(intent);
		}
		super.onDestroy();
	}   
}
