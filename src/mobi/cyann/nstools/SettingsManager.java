/**
 * SettingsManager.java
 * Nov 27, 2011 11:19:28 AM
 */
package mobi.cyann.nstools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author arif
 *
 */
public class SettingsManager {
	
	public static void writeToInterface(Context c, String sharedPreferenceName, boolean onBoot) {
		SharedPreferences preferences = null;
		if(sharedPreferenceName != null) {
			preferences = c.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE);
		}else {
			preferences = PreferenceManager.getDefaultSharedPreferences(c);
		}
		
		boolean restore = preferences.getBoolean(c.getString(R.string.key_restore_on_boot), true);
		if(onBoot && !restore) {
			return;
		}
		
		StringBuilder command = new StringBuilder();
		
		String status = null;
		if(!preferences.getBoolean(c.getString(R.string.key_default_voltage), true)) {
			// restore voltage setting if and only if key_default_voltage is false
			
			// customvoltage
			// -----------------
			// arm voltages
			status = preferences.getString(c.getString(R.string.key_max_arm_volt), "-1");
			if(!status.equals("-1")) {
				String armvolts = preferences.getString(c.getString(R.string.key_arm_volt_pref), "0");
				command.append("echo " + status + " > " + "/sys/class/misc/customvoltage/max_arm_volt\n");
				command.append("echo " + armvolts + " > " + "/sys/class/misc/customvoltage/arm_volt\n");
			}
			
			// uv_mv_table
			status = preferences.getString(c.getString(R.string.key_uvmvtable_pref), "-1");
			if(!status.equals("-1")) {
				command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table\n");
			}
			
			// int voltages
			status = preferences.getString(c.getString(R.string.key_max_int_volt), "-1");
			if(!status.equals("-1")) {
				String armvolts = preferences.getString(c.getString(R.string.key_int_volt_pref), "0");
				command.append("echo " + status + " > " + "/sys/class/misc/customvoltage/max_int_volt\n");
				command.append("echo " + armvolts + " > " + "/sys/class/misc/customvoltage/int_volt\n");
			}
		}
		// BLD
		int value = preferences.getInt(c.getString(R.string.key_bld_status), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/class/misc/backlightdimmer/enabled\n");
			value = preferences.getInt(c.getString(R.string.key_bld_delay), -1);
			if(value > -1)
				command.append("echo " + value + " > " + "/sys/class/misc/backlightdimmer/delay\n");
		}
		
		// BLN
		value = preferences.getInt(c.getString(R.string.key_bln_status), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/class/misc/backlightnotification/enabled\n");
		}

		// BLX
		value = preferences.getInt(c.getString(R.string.key_blx_charging_limit), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/class/misc/batterylifeextender/charging_limit\n");
		}
		
		// Deepidle
		status = preferences.getString(c.getString(R.string.key_deepidle_status), "-1");
		if(!status.equals("-1")) {
			command.append("echo" + status + " > " + "/sys/class/misc/deepidle/enabled\n");
		}
		
		// Touchwake
		value = preferences.getInt(c.getString(R.string.key_touchwake_status), -1);
		if(value > -1) {
			command.append("echo" + value + " > " + "/sys/class/misc/touchwake/enabled\n");
			value = preferences.getInt(c.getString(R.string.key_touchwake_delay), -1);
			if(value > -1)
				command.append("echo" + value + " > " + "/sys/class/misc/touchwake/delay\n");
		}
		
		// governor
		status = preferences.getString(c.getString(R.string.key_governor), "-1");
		if(!status.equals("-1")) {
			command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor\n");
		}
		
		// lazy screenoff max freq
		status = preferences.getString(c.getString(R.string.key_screenoff_maxfreq), "-1");
		if(!status.equals("-1")) {
			command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/lazy/screenoff_maxfreq\n");
		}
		
		// cmled
		value = preferences.getInt(c.getString(R.string.key_cmled_bltimeout), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/class/misc/notification/bl_timeout\n");
		}
		// cmled blink
		value = preferences.getInt(c.getString(R.string.key_cmled_blink), -1);
		if(value > -1) {
			// we must write blinktimeout before blink status
			// coz if we write blinktimeout it will reset blink status to enabled
			int timeout = preferences.getInt(c.getString(R.string.key_cmled_blinktimeout), -1);
			if(timeout > -1)
				command.append("echo " + timeout + ">" + "/sys/class/misc/notification/blinktimeout\n");
			
			command.append("echo " + value + " > " + "/sys/class/misc/notification/blink\n");
		}
		
		// io scheduler
		status = preferences.getString(c.getString(R.string.key_iosched), "-1");
		if(!status.equals("-1")) {
			String[] ioscheds = c.getResources().getStringArray(R.array.iosched_interfaces);
			for(String i: ioscheds) {
				command.append("echo " + status + " > " + i + "\n");	
			}
		}
		
		// Liveoc
		status = preferences.getString(c.getString(R.string.key_liveoc), "-1");
		if(!status.equals("-1") && !status.equals("100")) {
			int liveoc = Integer.parseInt(status);
			
			int minFreq = Integer.parseInt(preferences.getString(c.getString(R.string.key_min_cpufreq), "-1"));
			if(minFreq != -1) {
				minFreq = minFreq * 100 / liveoc;
				command.append("echo " + String.valueOf(minFreq) + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq\n");
			}
			
			int maxFreq = Integer.parseInt(preferences.getString(c.getString(R.string.key_max_cpufreq), "-1"));
			if(maxFreq != -1) {
				maxFreq = maxFreq * 100 / liveoc;
				command.append("echo " + String.valueOf(maxFreq) + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq\n");
			}
			
			command.append("echo " + status + " > " + "/sys/class/misc/liveoc/oc_value\n");
		}else {
			// cpu min freq
			status = preferences.getString(c.getString(R.string.key_min_cpufreq), "-1");
			if(!status.equals("-1")) {
				command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq\n");
			}
			
			// cpu max freq
			status = preferences.getString(c.getString(R.string.key_max_cpufreq), "-1");
			if(!status.equals("-1")) {
				command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq\n");
			}
		}
		SysCommand.getInstance().suRun(command.toString());
	}
}
