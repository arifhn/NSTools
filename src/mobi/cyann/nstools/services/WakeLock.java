/**
 * WakeLock.java
 * Jan 20, 2012 9:36:47 AM
 */
package mobi.cyann.nstools.services;

import mobi.cyann.nstools.R;
import android.content.Context;
import android.os.PowerManager;

/**
 * @author arif
 *
 */
public class WakeLock {
	private static PowerManager.WakeLock wakeLock;
	
	public static void acquire(Context context) {
		if(wakeLock == null) {
			PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getString(R.string.app_name));
		}
		wakeLock.acquire();
	}
	
	public static void release() {
		if(wakeLock != null) {
			wakeLock.release();
		}
	}
}
