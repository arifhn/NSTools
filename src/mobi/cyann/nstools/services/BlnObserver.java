/**
 * BlnObserver.java
 * Jan 20, 2012 9:42:24 AM
 */
package mobi.cyann.nstools.services;

import mobi.cyann.nstools.R;
import mobi.cyann.nstools.SysCommand;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author arif
 *
 */
public class BlnObserver extends FileObserver {
	private final static String LOG_TAG = "NSTools.BlnObserver";
	
	private AlarmReceiver alarmReceiver;
	private Context context;
	
	public BlnObserver(Context context) {
		super("/sys/class/misc/backlightnotification/notification_led");
		this.context = context;
		
		alarmReceiver = new AlarmReceiver();
		// register alarm receiver
		context.registerReceiver(alarmReceiver, new IntentFilter("mobi.cyann.nstools.SHUTDOWN_BLN"));
	}
	
	private int getBlnTimeout() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt(context.getString(R.string.key_bln_timeout), 0);
	}
	
	@Override
	public void onEvent(int event, String path) {
		if(event == FileObserver.CLOSE_WRITE) {
			WakeLock.acquire(context);
			int blnTimeout = getBlnTimeout();
			if(blnTimeout > 0) {
				// recalculate timeout from seconds to miliseconds
				blnTimeout = blnTimeout * 1000;
				SysCommand sc = SysCommand.getInstance();
				int n = sc.readSysfs("/sys/class/misc/backlightnotification/notification_led");
				if(n > 0 && sc.getLastResult(0).equals("1")) { // BLN on
					Log.d(LOG_TAG, "set alarm to shutdown BLN in " + blnTimeout + " ms");
					// get alarm manager
					AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
					
					// set alarm to turn off BLN
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("mobi.cyann.nstools.SHUTDOWN_BLN"), PendingIntent.FLAG_CANCEL_CURRENT);
					alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + blnTimeout, pendingIntent);
				}
			}
			WakeLock.release();
		}
	}

	class AlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			WakeLock.acquire(context);
			Log.d(LOG_TAG, "shutdown BLN now");
			SysCommand.getInstance().writeSysfs("/sys/class/misc/backlightnotification/notification_led", "0");
			WakeLock.release();
		}
	}

	@Override
	public void stopWatching() {
		try {
			context.unregisterReceiver(alarmReceiver);
		}catch(Exception ex) {
			Log.d(LOG_TAG, "exception when unregister receiver", ex);
		}
		super.stopWatching();
	}
}
