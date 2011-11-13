/**
 * CPUActivity.java
 * Nov 12, 2011 10:49:10 AM
 */
package mobi.cyann.nstools;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;

/**
 * @author arif
 *
 */
public class CPUActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {
	private final static String LOG_TAG = "NSTools.CPUActivity";
	
	private SharedPreferences preferences;
	
	private String availableGovernors[];
	private String availableFreqeuncies[];
	private String availableFreqInMhz[];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// set preference layout
		addPreferencesFromResource(R.xml.cpu);
		
		// set prefernece change listener
		findPreference(getString(R.string.key_governor)).setOnPreferenceChangeListener(this);
		findPreference(getString(R.string.key_min_cpufreq)).setOnPreferenceChangeListener(this);
		findPreference(getString(R.string.key_max_cpufreq)).setOnPreferenceChangeListener(this);
		// set preference click listener
		findPreference(getString(R.string.key_screenoff_maxfreq)).setOnPreferenceClickListener(this);
		
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		SysCommand sc = SysCommand.getInstance();
		int n = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
		if(n >= 0) {
			String temp = sc.getLastResult(0);
			availableGovernors = temp.split(" ");
		}
		
		n = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
		if(n >= 0) {
			String temp = sc.getLastResult(0);
			availableFreqeuncies = temp.split(" ");
			availableFreqInMhz = new String[availableFreqeuncies.length];
			for(int i = 0; i < availableFreqeuncies.length; ++i) {
				availableFreqInMhz[i] = toMHzString(availableFreqeuncies[i]);
			}
		}else {
			// try read available frequencies from uv_mv_table
			n = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
			if(n >= 0) {
				availableFreqeuncies = new String[n];
				availableFreqInMhz = new String[n];
				for(int i = 0; i < n; ++i) {
					String line = sc.getLastResult(i);
					String parts[] = line.split("mhz");
					availableFreqeuncies[i] = String.valueOf(Integer.parseInt(parts[0]) * 1000);
					availableFreqInMhz[i] = parts[0] + " MHz";
				}
			}
		}
	}

	private String toMHzString(String freqStr) {
		int freq = Integer.parseInt(freqStr);
		return (freq/1000) + " MHz";
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateDisplay();
	}

	private void updateDisplay() {
		// update governor display
		String value = preferences.getString(getString(R.string.key_governor), "-1");
		ListPreference list = (ListPreference)findPreference(getString(R.string.key_governor));
		if(!value.equals("-1")) {
			if(availableGovernors != null && availableGovernors.length > 0) {
				list.setEntries(availableGovernors);
				list.setEntryValues(availableGovernors);
				list.setOnPreferenceChangeListener(this);
				list.setValue(value);
			}else {
				list.setEnabled(false);
			}
			list.setSummary(value);
		}else {
			list.setSummary(getString(R.string.status_not_available));
			list.setEnabled(false);
		}
		
		// update min freq display
		value = preferences.getString(getString(R.string.key_min_cpufreq), "-1");
		list = (ListPreference)findPreference(getString(R.string.key_min_cpufreq));
		if(!value.equals("-1")) {
			if(availableFreqeuncies != null && availableFreqeuncies.length > 0) {
				list.setEntries(availableFreqInMhz);
				list.setEntryValues(availableFreqeuncies);
				list.setOnPreferenceChangeListener(this);
				list.setValue(value);
			}else {
				list.setEnabled(false);
			}
			list.setSummary(toMHzString(value));
		}else {
			list.setSummary(getString(R.string.status_not_available));
			list.setEnabled(false);
		}
		
		// update max freq display
		value = preferences.getString(getString(R.string.key_max_cpufreq), "-1");
		list = (ListPreference)findPreference(getString(R.string.key_max_cpufreq));
		if(!value.equals("-1")) {
			if(availableFreqeuncies != null && availableFreqeuncies.length > 0) {
				list.setEntries(availableFreqInMhz);
				list.setEntryValues(availableFreqeuncies);
				list.setOnPreferenceChangeListener(this);
				list.setValue(value);
			}else {
				list.setEnabled(false);
			}
			list.setSummary(toMHzString(value));
		}else {
			list.setSummary(getString(R.string.status_not_available));
			list.setEnabled(false);
		}
		
		// update lazy screenoff maxfreq
		value = preferences.getString(getString(R.string.key_screenoff_maxfreq), "-1");
		Preference p = findPreference(getString(R.string.key_screenoff_maxfreq));
		if(value.equals("0")) {
			p.setSummary(getString(R.string.status_off));
			p.setEnabled(true);
		}else if(value.equals("1")) {
			p.setSummary(getString(R.string.status_on));
			p.setEnabled(true);
		}else {
			p.setSummary(getString(R.string.status_not_available));
			p.setEnabled(false);
		}
	}
	
	private void setPreference(String key, String value) {
		Editor ed = preferences.edit();
		ed.putString(key, value);
		ed.commit();
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(getString(R.string.key_screenoff_maxfreq))) {
			String keyStatus = preference.getKey();
			String status = preferences.getString(keyStatus, "-1");
			if(status.equals("1")) {
				// disable tweak
				if(SysCommand.getInstance().suRun("echo", "0", ">", "/sys/devices/system/cpu/cpu0/cpufreq/lazy/screenoff_maxfreq") >= 0) {
					setPreference(keyStatus, "0");
				}else {
					Log.e(LOG_TAG, "failed to set 0 for " + keyStatus);
					SysCommand.getInstance().logLastError(LOG_TAG);
				}
			}else if(status.equals("0")) {
				// enable tweak
				if(SysCommand.getInstance().suRun("echo", "1", ">", "/sys/devices/system/cpu/cpu0/cpufreq/lazy/screenoff_maxfreq") >= 0) {
					setPreference(keyStatus, "1");
				}else {
					Log.e(LOG_TAG, "failed to set 1 for " + keyStatus);
					SysCommand.getInstance().logLastError(LOG_TAG);
				}
			}
			updateDisplay();
		}
		return false;
	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(getString(R.string.key_governor))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
			setPreference(preference.getKey(), newValue.toString());
			if(!newValue.toString().equals("lazy")) {
				setPreference(getString(R.string.key_screenoff_maxfreq), "-1");
			}else {
				int n = SysCommand.getInstance().suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/lazy/screenoff_maxfreq");
				if(n >= 0) {
					setPreference(getString(R.string.key_screenoff_maxfreq), SysCommand.getInstance().getLastResult(0));
				}
			}
			updateDisplay();
		}else if(preference.getKey().equals(getString(R.string.key_min_cpufreq))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
			setPreference(preference.getKey(), newValue.toString());
			updateDisplay();
		}else if(preference.getKey().equals(getString(R.string.key_max_cpufreq))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
			setPreference(preference.getKey(), newValue.toString());
			updateDisplay();
		}
		return false;
	}
}
