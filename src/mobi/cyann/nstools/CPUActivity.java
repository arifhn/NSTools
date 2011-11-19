/**
 * CPUActivity.java
 * Nov 12, 2011 10:49:10 AM
 */
package mobi.cyann.nstools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
		
		// governor
		findPreference(getString(R.string.key_governor)).setOnPreferenceChangeListener(this);
		// scaling min/max freq
		findPreference(getString(R.string.key_min_cpufreq)).setOnPreferenceChangeListener(this);
		findPreference(getString(R.string.key_max_cpufreq)).setOnPreferenceChangeListener(this);
		// Liveoc oc value
		findPreference(getString(R.string.key_liveoc)).setOnPreferenceChangeListener(this);
		// deepidle status
		findPreference(getString(R.string.key_deepidle_status)).setOnPreferenceClickListener(this);
		// deepidle stats
		findPreference(getString(R.string.key_deepidle_stats)).setOnPreferenceClickListener(this);
		// lazy screen off
		findPreference(getString(R.string.key_screenoff_maxfreq)).setOnPreferenceClickListener(this);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		SysCommand sc = SysCommand.getInstance();
		int n = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
		if(n >= 0) {
			String temp = sc.getLastResult(0);
			availableGovernors = temp.split(" ");
		}
		
		reloadFrequencies();
	}

	private void reloadFrequencies() {
		SysCommand sc = SysCommand.getInstance();
		int n = -1;//sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
		if(n >= 0) {
			String temp = sc.getLastResult(0);
			availableFreqeuncies = temp.split(" ");
			availableFreqInMhz = new String[availableFreqeuncies.length];
			for(int i = 0; i < availableFreqeuncies.length; ++i) {
				availableFreqInMhz[i] = toMHzString(availableFreqeuncies[i]);
			}
		}else {
			// try read available frequencies from time_in_state
			n = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
			if(n >= 0) {
				availableFreqeuncies = new String[n];
				availableFreqInMhz = new String[n];
				for(int i = 0; i < n; ++i) {
					String line = sc.getLastResult(i);
					String parts[] = line.split(" ");
					availableFreqeuncies[i] = parts[0];
					availableFreqInMhz[i] = toMHzString(parts[0]);
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
		
		// update display for Liveoc
		p = findPreference(getString(R.string.key_liveoc));
		value = preferences.getString(getString(R.string.key_liveoc), "-1");
		if(value.equals("-1")) {
			p.setEnabled(false);
			p.setSummary(getString(R.string.status_not_available));
		}else {
			p.setEnabled(true);
			p.setSummary(value);
		}
		
		// update display for Deepidle
		p = findPreference(getString(R.string.key_deepidle_status));
		value = preferences.getString(getString(R.string.key_deepidle_status), "-1");
		if(value.equals("1")) {
			p.setEnabled(true);
			p.setSummary(getString(R.string.status_on));
			findPreference(getString(R.string.key_deepidle_stats)).setEnabled(true);
		}else if(value.equals("0")) {
			p.setEnabled(true);
			p.setSummary(getString(R.string.status_off));
			findPreference(getString(R.string.key_deepidle_stats)).setEnabled(true);
		}else {
			p.setEnabled(false);
			p.setSummary(getString(R.string.status_not_available));
			findPreference(getString(R.string.key_deepidle_stats)).setEnabled(false);
		}
	}
	
	private void setPreference(String key, String value) {
		Editor ed = preferences.edit();
		ed.putString(key, value);
		ed.commit();
	}
	
	private void showIdleStatsDialog() {
		// display dialog
		final int timeText[] = {R.id.time1, R.id.time2, R.id.time3};
		final int avgText[] = {R.id.avg1, R.id.avg2, R.id.avg3};
		
		final View content = getLayoutInflater().inflate(R.layout.idle_stats_dialog, null);
		
		Pattern time = Pattern.compile("([0-9]+)ms");
		Pattern average = Pattern.compile("\\(([0-9]+)ms\\)");
		
		SysCommand.getInstance().suRun("cat", "/sys/class/misc/deepidle/idle_stats");
		for(int i = 0; i < 3; ++i) {
			String line = SysCommand.getInstance().getLastResult(i + 2);
			Log.d(LOG_TAG, line);
			Matcher m = time.matcher(line);
			if(m.find())
				((TextView)content.findViewById(timeText[i])).setText(m.group(1));
			m = average.matcher(line);
			if(m.find())
				((TextView)content.findViewById(avgText[i])).setText(m.group(1));
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.label_deepidle_stats));
		builder.setView(content);
		builder.setPositiveButton(getString(R.string.label_reset), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(SysCommand.getInstance().suRun("echo", "1", ">", "/sys/class/misc/deepidle/reset_stats") < 0) {
					Log.d(LOG_TAG, "failed to reset deepidle stats");
					SysCommand.getInstance().logLastError(LOG_TAG);
				}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.label_close), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	private void toggleTweakStatus(String keyStatus, String deviceString) {
		String status = preferences.getString(keyStatus, "-1");
		if(status.equals("1")) {
			// disable tweak
			if(SysCommand.getInstance().suRun("echo", "0", ">", deviceString) >= 0) {
				setPreference(keyStatus, "0");
			}else {
				Log.e(LOG_TAG, "failed to set 0 for " + keyStatus);
				SysCommand.getInstance().logLastError(LOG_TAG);
			}
		}else if(status.equals("0")) {
			// enable tweak
			if(SysCommand.getInstance().suRun("echo", "1", ">", deviceString) >= 0) {
				setPreference(keyStatus, "1");
			}else {
				Log.e(LOG_TAG, "failed to set 1 for " + keyStatus);
				SysCommand.getInstance().logLastError(LOG_TAG);
			}
		}
		updateDisplay();
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		boolean ret = false;
		if(preference.getKey().equals(getString(R.string.key_screenoff_maxfreq))) {
			toggleTweakStatus(preference.getKey(), "/sys/devices/system/cpu/cpu0/cpufreq/lazy/screenoff_maxfreq");
			ret = true;
		}else if(preference.getKey().equals(getString(R.string.key_deepidle_status))) {
			toggleTweakStatus(preference.getKey(), "/sys/class/misc/deepidle/enabled");
			ret = true;
		}else if(preference.getKey().equals(getString(R.string.key_deepidle_stats))) {
			showIdleStatsDialog();
			ret = true;
		}
		return ret;
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
		}else if(preference.getKey().equals(getString(R.string.key_liveoc))) {
			if(!preferences.getString(preference.getKey(), "-1").equals(newValue)) {
				SysCommand sc = SysCommand.getInstance();
				
				sc.suRun("echo", newValue.toString(), ">", "/sys/class/misc/liveoc/oc_value");
				setPreference(getString(R.string.key_liveoc), newValue.toString());
				
				// reload min freq
				sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
				setPreference(getString(R.string.key_min_cpufreq), sc.getLastResult(0));
				// reload max freq
				sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
				setPreference(getString(R.string.key_max_cpufreq), sc.getLastResult(0));
				
				reloadFrequencies();
				updateDisplay();
			}
		}
		return false;
	}
}
