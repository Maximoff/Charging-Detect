package ru.maximoff.charging;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import java.io.File;
import ru.maximoff.charging.R;

public class SoundService extends Service {
    private MediaPlayer player;
	private Vibrator vibrator;

	@Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
		NotificationMan nManager = new NotificationMan(this);
		Notification n = nManager.sendNotification(getString(R.string.charging));
		startForeground(nManager.getId(), n);
		if (intent != null && intent.hasExtra("stop")) {
			stopForeground(true);
			stopSelf();
		} else {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			if (vibrator != null) {
				vibrator.cancel();
			}
			if (pref.getBoolean("vibration", true)) {
				vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				if (vibrator.hasVibrator()) {
					long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
					} else {
						vibrator.vibrate(pattern, 0);
					}
				}
			}
			if (player != null && player.isPlaying()) {
				return Service.START_STICKY;
			}
			String sound = pref.getString("sound", null);
			if (sound != null && !sound.isEmpty()) {
				try {
					player = MediaPlayer.create(this, Uri.fromFile(new File(sound)));
				} catch (Exception e) {
					player = MediaPlayer.create(this, R.raw.r);
				}
			} else {
				player = MediaPlayer.create(this, R.raw.r);
			}
			if (player == null) {
				player = MediaPlayer.create(this, R.raw.r);
			}
			player.setLooping(true);
			player.setVolume(100, 100);
			try {
				player.start();
			} catch (Exception e) {
				nManager.sendNotification(getString(R.string.error));
			}
		}
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
		if (vibrator != null) {
			vibrator.cancel();
		}
		super.onDestroy();
    }
}
