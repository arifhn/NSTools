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
			
			// lazy screenoff max freq
			status = preferences.getString(getString(R.string.key_screenoff_maxfreq), "-1");
			if(!status.equals("-1")) {
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/devices/system/cpu/cpu0/cpufreq/lazy/screenoff_maxfreq");
			}
			
			// cmled
			status = preferences.getString(getString(R.string.key_cmled_bltimeout), "-1");
			if(!status.equals("-1")) {
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/notification/bl_timeout");
			}
			status = preferences.getString(getString(R.string.key_cmled_blink), "-1");
			if(!status.equals("-1")) {
				String timeout = preferences.getString(getString(R.string.key_cmled_blinktimeout), "5");
				SysCommand.getInstance().suRun("echo", timeout, ">", "/sys/class/misc/notification/blinktimeout");
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/notification/blink");
			}
			
			// io scheduler
			status = preferences.getString(getString(R.string.key_iosched), "-1");
			if(!status.equals("-1")) {
				// for /system and /data
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/block/mmcblk0/queue/scheduler");
				// for /cache
				SysCommand.getInstance().suRun("echo", status, ">", "/sys/block/mtdblock4/queue/scheduler");
			}
			
 			// Liveoc
 			status = preferences.getString(getString(R.string.key_liveoc), "-1");
			if(!status.equals("-1") && !status.equals("100")) {
				int liveoc = Integer.parseInt(status);
				
				int minFreq = Integer.parseInt(preferences.getString(getString(R.string.key_min_cpufreq), "-1"));
				if(minFreq != -1) {
					minFreq = minFreq * 100 / liveoc;
					SysCommand.getInstance().suRun("echo", String.valueOf(minFreq), ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
				}
				
				int maxFreq = Integer.parseInt(preferences.getString(getString(R.string.key_max_cpufreq), "-1"));
				if(maxFreq != -1) {
					maxFreq = maxFreq * 100 / liveoc;
					SysCommand.getInstance().suRun("echo", String.valueOf(maxFreq), ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
				}
				
 				SysCommand.getInstance().suRun("echo", status, ">", "/sys/class/misc/liveoc/oc_value");
			}else {
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
			}
		}
	}
}
