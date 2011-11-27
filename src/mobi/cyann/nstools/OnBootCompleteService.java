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
			Log.d(LOG_TAG, "trying to load & write settings to kernel...");
			int ret = SettingsManager.writeToInterface(this, null, true);
			if(ret == SettingsManager.SUCCESS) {
				Log.d(LOG_TAG, "write success");
			}else if(ret == SettingsManager.ERR_SET_ON_BOOT_FALSE) {
				Log.d(LOG_TAG, "write canceled, set_on_boot flag is false");
			}else if(ret == SettingsManager.ERR_DIFFERENT_KERNEL) {
				Log.d(LOG_TAG, "write canceled, different kernel version");
			}
		}catch(Exception ex) {
			Log.e(LOG_TAG, "write failed", ex);
		}
	}
}
