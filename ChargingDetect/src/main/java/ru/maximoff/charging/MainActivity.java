package ru.maximoff.charging;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	private final int REQUEST = 101;
	private final int OPEN = 102;
	private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		checkPerm(this, new String[]{"READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"});
    }

	@Override
	public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
		switch (requestCode) {
			case REQUEST:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					init();
				} else if (!shouldShowRequestPermissionRationale(permissions[0])) {
					Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
					startActivity(settings);
					finish();
				} else {
					checkPerm(this, new String[]{"READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"});
				}
				break;

			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	public void checkPerm(Activity act, String[] groups) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			List<String> permissions = new ArrayList<>();
			for (String permission : groups) {
				permission = "android.permission." + permission;
				if (act.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
					permissions.add(permission);
				}
			}
			if (permissions.isEmpty()) {
				init();
				return;
			}
			String[] request = permissions.toArray(new String[permissions.size()]);
			act.requestPermissions(request, REQUEST);
		} else {
			init();
		}
	}

	private void batteryOptimization() throws Exception {
		if (Build.VERSION.SDK_INT < 23) {
			return;
		}
		final String packageName = getPackageName();
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (!pm.isIgnoringBatteryOptimizations(packageName)) {
			try {
				Intent intent = new Intent();
				intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + packageName));
				startActivity(intent);
			} catch (Exception e) {}
		}
	}

	private void openFile(String mime) {
		Intent intent = new Intent();
		intent.setType(mime);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
		intent.setAction(Intent.ACTION_GET_CONTENT);
		try {
			startActivityForResult(Intent.createChooser(intent, getString(R.string.app_name)), OPEN);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == OPEN && resultCode == Activity.RESULT_OK) {
			if (data != null && data.getData() != null) {
				String path = FileHelper.getRealPathFromURI(this, data.getData());
				if (path == null || path.isEmpty()) {
					Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
				} else {
					editText.setText(path);
				}
			} else {
				Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void init() {
		try {
			batteryOptimization();
		} catch (Exception e) {}
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final CheckBox bg = findViewById(R.id.mainCheckBox1);
		Intent servBg = new Intent(MainActivity.this, BackgroundService.class);
		if (pref.getBoolean("service", true)) {
			bg.setChecked(true);
		} else {
			bg.setChecked(false);
			servBg.putExtra("stop", true);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(servBg);
		} else {
			startService(servBg);
		}
		final CheckBox vib = findViewById(R.id.mainCheckBox2);
		vib.setChecked(pref.getBoolean("vibration", true));
		editText = findViewById(R.id.mainEditText1);
		String sound = pref.getString("sound", null);
		if (sound != null) {
			File f = new File(sound);
			if (f.isFile()) {
				editText.setText(sound);
				editText.setSelection(sound.length());
			}
		}
		editText.requestFocus();
		OnClickListener click = new OnClickListener() {
			@Override
			public void onClick(View p1) {
				switch (p1.getId()) {
					case R.id.mainCheckBox1:
						Intent servBg = new Intent(MainActivity.this, BackgroundService.class);
						if (!bg.isChecked()) {
							servBg.putExtra("stop", true);
						}
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							startForegroundService(servBg);
						} else {
							startService(servBg);
						}
						pref.edit().putBoolean("service", bg.isChecked()).commit();
						break;

					case R.id.mainCheckBox2:
						pref.edit().putBoolean("vibration", vib.isChecked()).commit();
						break;

					case R.id.mainButton3:
						openFile("audio/*");
						break;

					case R.id.mainButton1:
					case R.id.mainButton2:
						String path = editText.getText().toString().trim();
						File f = new File(path);
						if (!path.isEmpty() && !f.isFile()) {
							Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
						} else {
							pref.edit().putString("sound", path).commit();
							Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
						}
						if (p1.getId() == R.id.mainButton2) {
							Intent serv = new Intent(MainActivity.this, SoundService.class);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
								startForegroundService(serv);
							} else {
								startService(serv);
							}
						}
						break;
				}
			}
		};
		bg.setOnClickListener(click);
		vib.setOnClickListener(click);
		Button save = findViewById(R.id.mainButton1);
		save.setOnClickListener(click);
		Button test = findViewById(R.id.mainButton2);
		test.setOnClickListener(click);
		Button open = findViewById(R.id.mainButton3);
		open.setOnClickListener(click);
		open.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View p1) {
					editText.setText("");
					return true;
				}
			});
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
