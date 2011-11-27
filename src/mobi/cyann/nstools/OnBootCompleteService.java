/**
 * OnBootCompleteService.java
 * 9:39:01 PM
 */
package mobi.cyann.nstools;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * @author arif
 *
 */
public class OnBootCompleteService extends IntentService {
	private final static String LOG_TAG = "NSTools.OnBootCompleteService";
	public OnBootCompleteService() {
		super("NSToolsService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			Log.d(LOG_TAG, "load & write settings to kernel...");
			SettingsManager.writeToInterface(this, null, true);
			Log.d(LOG_TAG, "write finished");
		}catch(Exception ex) {
			Log.e(LOG_TAG, "write failed", ex);
		}
	}
}
