/**
 * SettingsManager.java
 * Nov 27, 2011 11:19:28 AM
 */
package mobi.cyann.nstools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author arif
 *
 */
public class SettingsManager {
	private final static String LOG_TAG = "NSTools.SettingsManager";
	
	public final static int SUCCESS = 0;
	public final static int ERR_SET_ON_BOOT_FALSE = -1;
	public final static int ERR_DIFFERENT_KERNEL = -2;
	
	private static int writeToInterface(Context c, String sharedPreferenceName, boolean onBoot, boolean skipKernelChecking) {
		SharedPreferences preferences = null;
		if(sharedPreferenceName != null) {
			preferences = c.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE);
		}else {
			preferences = PreferenceManager.getDefaultSharedPreferences(c);
		}
		
		// check 'set on boot' preference
		boolean restore = preferences.getBoolean(c.getString(R.string.key_restore_on_boot), true);
		if(onBoot && !restore) {
			return ERR_SET_ON_BOOT_FALSE;
		}

		if(!skipKernelChecking) {
			// now check current kernel version with saved value
			restore = false;
			SysCommand sysCommand = SysCommand.getInstance();
			if(sysCommand.suRun("cat", "/proc/version") > 0) {
				String kernel = sysCommand.getLastResult(0);
				String savedKernelVersion = preferences.getString(c.getString(R.string.key_kernel_version), null);
				if(kernel.equals(savedKernelVersion)) {
					restore = true;
				}
			}
			if(!restore) {
				return ERR_DIFFERENT_KERNEL;
			}
		}
		StringBuilder command = new StringBuilder();
		
		String status = null;
		int value = -1;
		if(!preferences.getBoolean(c.getString(R.string.key_default_voltage), true)) {
			// restore voltage setting if and only if key_default_voltage is false
			
			// customvoltage
			// -----------------
			// arm voltages
			value = preferences.getInt(c.getString(R.string.key_max_arm_volt), -1);
			if(value > -1) {
				String armvolts = preferences.getString(c.getString(R.string.key_arm_volt_pref), "0");
				command.append("echo " + value + " > " + "/sys/class/misc/customvoltage/max_arm_volt\n");
				command.append("echo " + armvolts + " > " + "/sys/class/misc/customvoltage/arm_volt\n");
			}else {
				// uv_mv_table
				status = preferences.getString(c.getString(R.string.key_uvmvtable_pref), "-1");
				if(!status.equals("-1")) {
					command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table\n");
				}
			}
			// int voltages
			value = preferences.getInt(c.getString(R.string.key_max_int_volt), -1);
			if(value > -1) {
				String armvolts = preferences.getString(c.getString(R.string.key_int_volt_pref), "0");
				command.append("echo " + value + " > " + "/sys/class/misc/customvoltage/max_int_volt\n");
				command.append("echo " + armvolts + " > " + "/sys/class/misc/customvoltage/int_volt\n");
			}
		}
		// BLD
		value = preferences.getInt(c.getString(R.string.key_bld_status), -1);
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
			value = preferences.getInt(c.getString(R.string.key_bln_blink), -1);
			if(value > -1)
				command.append("echo " + value + " > " + "/sys/class/misc/backlightnotification/in_kernel_blink\n");
		}

		// BLX
		value = preferences.getInt(c.getString(R.string.key_blx_charging_limit), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/class/misc/batterylifeextender/charging_limit\n");
		}
		
		// Deepidle
		value = preferences.getInt(c.getString(R.string.key_deepidle_status), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/class/misc/deepidle/enabled\n");
		}
		
		// Touchwake
		value = preferences.getInt(c.getString(R.string.key_touchwake_status), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/class/misc/touchwake/enabled\n");
			value = preferences.getInt(c.getString(R.string.key_touchwake_delay), -1);
			if(value > -1)
				command.append("echo " + value + " > " + "/sys/class/misc/touchwake/delay\n");
		}
		
		// governor
		status = preferences.getString(c.getString(R.string.key_governor), "-1");
		if(!status.equals("-1")) {
			command.append("echo " + status + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor\n");
		}
		
		// lazy screenoff max freq
		value = preferences.getInt(c.getString(R.string.key_screenoff_maxfreq), -1);
		if(value > -1) {
			command.append("echo " + value + " > " + "/sys/devices/system/cpu/cpufreq/lazy/screenoff_maxfreq\n");
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
		value = preferences.getInt(c.getString(R.string.key_liveoc), -1);
		if(value > -1 && value != 100) {
			// cpu minfreq
			int minFreq = preferences.getInt(c.getString(R.string.key_min_cpufreq), -1);
			if(minFreq > -1) {
				minFreq = minFreq * 100 / value;
				command.append("echo " + minFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq\n");
			}
			// cpu maxfreq
			int maxFreq = preferences.getInt(c.getString(R.string.key_max_cpufreq), -1);
			if(maxFreq > -1) {
				maxFreq = maxFreq * 100 / value;
				command.append("echo " + maxFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq\n");
			}
			// liveoc
			command.append("echo " + value + " > " + "/sys/class/misc/liveoc/oc_value\n");
		}else {
			// cpu minfreq
			int minFreq = preferences.getInt(c.getString(R.string.key_min_cpufreq), -1);
			if(minFreq > -1) {
				command.append("echo " + minFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq\n");
			}
			
			// cpu maxfreq
			int maxFreq = preferences.getInt(c.getString(R.string.key_max_cpufreq), -1);
			if(maxFreq > -1) {
				command.append("echo " + maxFreq + " > " + "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq\n");
			}
		}
		SysCommand.getInstance().suRun(command.toString());
		
		return SUCCESS;
	}
	
	public static void saveSettings(Context c, String preferenceName) {
		File source = new File(c.getString(R.string.DEFAULT_PREFERENCE_FILE));
		File destDir = new File(c.getString(R.string.SETTINGS_DIR));
		if(!destDir.exists())
			destDir.mkdirs(); // create dir if not exists
		File destination = new File(destDir, preferenceName);
		// copy file
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(destination);
			
			byte buffer[] = new byte [1024];
			int count = 0;
			do {
				count = is.read(buffer);
				if(count > 0)
					os.write(buffer, 0, count);
			}while(count > 0);
			os.flush();
		}catch(IOException ex) {
			Log.e(LOG_TAG, "", ex);
		}finally {
			try {
				if(is != null)
					is.close();	
				if(os != null)
					os.close();
			}catch(IOException e) {}
		}
	}
	
	/**
	 * 
	 * @param c
	 * @param preferenceName
	 * @return
	 */
	public static int loadSettings(Context c, String preferenceName) {
		StringBuilder preferencePath = new StringBuilder(c.getString(R.string.SETTINGS_DIR));
		preferencePath.append(preferenceName);
		return writeToInterface(c, preferencePath.toString(), false, false);
	}
	
	/**
	 * call this method to force-load settings
	 * 
	 * @param c
	 * @param preferenceName
	 * @return
	 */
	public static int forceLoadSettings(Context c, String preferenceName) {
		StringBuilder preferencePath = new StringBuilder(c.getString(R.string.SETTINGS_DIR));
		preferencePath.append(preferenceName);
		// skipKernelChecking = true (we want to force load the settings)
		return writeToInterface(c, preferencePath.toString(), false, true);
	}
	
	/**
	 * this method called on boot completed
	 * 
	 * @param c
	 * @return
	 */
	public static int loadSettingsOnBoot(Context c) {
		// onBoot = true, skipKernelChecking = false
		return writeToInterface(c, null, true, false);
	}
}
