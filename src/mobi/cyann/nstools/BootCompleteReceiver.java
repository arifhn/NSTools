package mobi.cyann.nstools;

import mobi.cyann.nstools.services.ObserverService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author arif
 *
 */
public class BootCompleteReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = "NSTools.BootCompleteReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG_TAG, "starting service");
		
		// start OnBootCompleteService (restore setting)
		context.startService(new Intent(context, OnBootCompleteService.class));
		
		// start our ObserverService (monitor missed call)
		ObserverService.startService(context, false);
	}
	
}
