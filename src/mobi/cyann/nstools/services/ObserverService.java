/**
 * ObserverService.java
 * Jan 19, 2012 5:14:04 PM
 */
package mobi.cyann.nstools.services;

import mobi.cyann.nstools.R;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author arif
 *
 */
public class ObserverService extends Service {
	private final static String LOG_TAG = "NSTools.ObserverService";
	private MissedCallObserver missedCallObserver;
	private BlnObserver blnObserver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = null;
		if(intent != null) {
			action = intent.getAction();
		}
		if(action != null && action.equals("STOP")) {
			Log.d(LOG_TAG, "stoping service");
			if(blnObserver != null) {
				blnObserver.stopWatching();
			}
			blnObserver = null;
			if(missedCallObserver != null) {
				missedCallObserver.stopWatching();
			}
			missedCallObserver = null;
			
			stopSelf();
			Log.d(LOG_TAG, "service stoped");
			return Service.START_NOT_STICKY;
		}else {
			Log.d(LOG_TAG, "starting service");
			if(blnObserver == null) {
				// register bln observer (for bln timeout)
				blnObserver = new BlnObserver(this);
				blnObserver.startWatching();
			}
			if(missedCallObserver == null) {
				// register missed call observer
				missedCallObserver = new MissedCallObserver(this);
			}
			Log.d(LOG_TAG, "service started");
			return Service.START_STICKY;
		}
	}

	public static void startService(Context context, boolean forced) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean startService = preferences.getBoolean(context.getString(R.string.key_nstools_service), false);
		if(startService || forced) {
			context.startService(new Intent(context, ObserverService.class));
		}
	}
	
	public static void stopService(Context context) {
		Intent stopIntent = new Intent(context, ObserverService.class);
		stopIntent.setAction("STOP");
		context.startService(stopIntent);
	}
}
