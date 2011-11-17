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
		boolean restore = preferences.getBoolean(getString(R.string.key_restore_on_boot), true);
		if(restore) {
			// Restore each tweak preference on boot
			String status = null;
			
			if(!preferences.getBoolean(getString(R.string.key_default_voltage), true)) {
				// restore voltage setting if and only if key_default_voltage is false
				
				// customvoltage
				// -----------------
				// arm voltages
				status = preferences.getString(getString(R.string.key_max_arm_volt), "-1");
				if(!status.equals("-1")) {
					String armvolts = preferences.getString(getString(R.string.key_arm_volt_pref), "0");
					SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/customvoltage/max_arm_volt");
					SysCommand.getInstance().suRun("echo", armvolts, ">", "/sys/class/misc/customvoltage/arm_volt");
				}
				
				// uv_mv_table
				status = preferences.getString(getString(R.string.key_uvmvtable_pref), "-1");
				if(!status.equals("-1")) {
					SysCommand.getInstance().suRun("echo", status, ">", "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
				}
				
				// int voltages
				status = preferences.getString(getString(R.string.key_max_int_volt), "-1");
				if(!status.equals("-1")) {
					String armvolts = preferences.getString(getString(R.string.key_int_volt_pref), "0");
					SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/customvoltage/max_int_volt");
					SysCommand.getInstance().suRun("echo", armvolts, ">", "/sys/class/misc/customvoltage/int_volt");
				}
			}
			// BLD
			status = preferences.getString(getString(R.string.key_bld_status), "-1");
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
			
			// Touchwake
			status = preferences.getString(getString(R.string.key_touchwake_status), "-1");
			if(!status.equals("-1")) {
				String delay = preferences.getString(getString(R.string.key_touchwake_delay), "0");
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/touchwake/enabled");
				SysCommand.getInstance().suRun("echo", delay, ">", "/sys/class/misc/touchwake/delay");
			}
			
			// governor
			status = preferences.getString(getString(R.string.key_governor), "-1");
			if(!status.equals("-1")) {
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
			}
			
			// cpu min freq
			status = preferences.getString(getString(R.string.key_min_cpufreq), "-1");
			if(!status.equals("-1")) {
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
			}
			
			// cpu max freq
			status = preferences.getString(getString(R.string.key_max_cpufreq), "-1");
			if(!status.equals("-1")) {
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
			}
			
			// lazy screenoff max freq
			status = preferences.getString(getString(R.string.key_screenoff_maxfreq), "-1");
			if(!status.equals("-1")) {
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/devices/system/cpu/cpu0/cpufreq/lazy/screenoff_maxfreq");
			}
			
			// Liveoc
			status = preferences.getString(getString(R.string.key_liveoc), "-1");
			if(!status.equals("-1")) {
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/liveoc/oc_value");
			}
		}
	}
}
