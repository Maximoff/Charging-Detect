package ru.maximoff.charging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class NotificationMan {
	private int NOT_ID;
	private int importance;
	private int priority;;
	private Context context;
	private NotificationManager notificationManager;

	public NotificationMan(Context ctx) {
		context = ctx;
		NOT_ID = 1001;
		importance = NotificationManager.IMPORTANCE_HIGH;
		priority = Notification.PRIORITY_HIGH;
		notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void silent() {
		NOT_ID = 1002;
		importance = NotificationManager.IMPORTANCE_LOW;
		priority = Notification.PRIORITY_LOW;
	}

	public void cancel() {
		notificationManager.cancel(NOT_ID);
	}

	public int getId() {
		return NOT_ID;
	}

	public Notification sendNotification(String text) {
		final String channelId = "channel-" + NOT_ID;
		final String channelName = context.getString(R.string.service_name);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
			channel.enableLights(false);
			channel.enableVibration(false);
			channel.setSound(null, null);
			channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			channel.setDescription(channelName);
			notificationManager.createNotificationChannel(channel);
		}
		PendingIntent pendingIntent;
		if (NOT_ID == 1001) {
			Intent intent = new Intent(context, SoundService.class);
			intent.putExtra("stop", true);
			pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		} else {
			Intent intent = new Intent(context, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		}
		Notification.Builder notBuilder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notBuilder = new Notification.Builder(context, channelId);
		} else {
			notBuilder = new Notification.Builder(context);
		}
        notBuilder.setTicker(text)
            .setContentTitle(text)
			.setOngoing(true)
			.setPriority(priority)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(R.drawable.nt)
			.setContentIntent(pendingIntent);
		Notification notification = notBuilder.build();
		notificationManager.notify(NOT_ID, notification);
		return notification;
	}
}

