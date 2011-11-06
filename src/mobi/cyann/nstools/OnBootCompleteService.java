/**
 * OnBootCompleteService.java
 * 9:39:01 PM
 */
package mobi.cyann.nstools;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
		Log.d(LOG_TAG, "service start");
		// load preferences
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Restore each tweak preference on boot
		
		// BLD
		String status = preferences.getString(getString(R.string.key_bld_status), "-1");
		if(!status.equals("-1")) {
			String delay = preferences.getString(getString(R.string.key_bld_delay), "0");
			SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/backlightdimmer/enabled");
			SysCommand.getInstance().suRun("echo", delay, ">", "/sys/class/misc/backlightdimmer/delay");
		}
		
		// BLN
		status = preferences.getString(getString(R.string.key_bln_status), "-1");
		if(!status.equals("-1")) {
			SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/backlightnotification/enabled");
		}

		// BLX
		status = preferences.getString(getString(R.string.key_blx_charging_limit), "-1");
		if(!status.equals("-1")) {
			SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/batterylifeextender/charging_limit");
		}
		
		// Deepidle
		status = preferences.getString(getString(R.string.key_deepidle_status), "-1");
		if(!status.equals("-1")) {
			SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/deepidle/enabled");
		}
		
		// Screendimmer
		status = preferences.getString(getString(R.string.key_screendimmer_status), "-1");
		if(!status.equals("-1")) {
			String delay = preferences.getString(getString(R.string.key_screendimmer_delay), "0");
			SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/screendimmer/enabled");
			SysCommand.getInstance().suRun("echo", delay, ">", "/sys/class/misc/screendimmer/delay");
		}
		
		// Touchwake
		status = preferences.getString(getString(R.string.key_touchwake_status), "-1");
		if(!status.equals("-1")) {
			String delay = preferences.getString(getString(R.string.key_touchwake_delay), "0");
			SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/touchwake/enabled");
			SysCommand.getInstance().suRun("echo", delay, ">", "/sys/class/misc/touchwake/delay");
		}
	}
}
