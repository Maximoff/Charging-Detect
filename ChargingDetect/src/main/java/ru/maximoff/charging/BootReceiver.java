package ru.maximoff.charging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent p2) {
		Intent boot = new Intent(context, BackgroundService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(boot);
		} else {
			context.startService(boot);
		}
	}
}
