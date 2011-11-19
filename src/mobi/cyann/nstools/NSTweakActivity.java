package mobi.cyann.nstools;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class NSTweakActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
	private final static String LOG_TAG = "NSTools.NSTweakActivity";
	
	private SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// set preference layout
		addPreferencesFromResource(R.xml.ns_tweak);
		
		// Set onPreferenceClickListener to this class
		// --------------------------------------------
		
		// BLD status
		Preference p = findPreference(getString(R.string.key_bld_status));
		p.setOnPreferenceClickListener(this);
		
		// BLN status
		p = findPreference(getString(R.string.key_bln_status));
		p.setOnPreferenceClickListener(this);
		
		// Touchwake status
		p = findPreference(getString(R.string.key_touchwake_status));
		p.setOnPreferenceClickListener(this);
		
		// BLD delay
		p = findPreference(getString(R.string.key_bld_delay));
		p.setOnPreferenceChangeListener(this);

		// BLX charging limit
		p = findPreference(getString(R.string.key_blx_charging_limit));
		p.setOnPreferenceChangeListener(this);

		// Touchwake delay
		p = findPreference(getString(R.string.key_touchwake_delay));
		p.setOnPreferenceChangeListener(this);
		
		// CMLed
		p = findPreference(getString(R.string.key_cmled_bltimeout));
		p.setOnPreferenceChangeListener(this);
		
		p = findPreference(getString(R.string.key_cmled_blink));
		p.setOnPreferenceClickListener(this);
		
		p = findPreference(getString(R.string.key_cmled_blinktimeout));
		p.setOnPreferenceChangeListener(this);
		
		// load scheduler
		int n = SysCommand.getInstance().suRun("cat", "/sys/block/mmcblk0/queue/scheduler");
		if(n > 0) {
			String tmp = SysCommand.getInstance().getLastResult(0);
			String sched[] = tmp.split(" ");
			int idx = 0;
			for(int i = 0; i < sched.length; ++i) {
				if(sched[i].startsWith("[")) {
					sched[i] = sched[i].substring(1, sched[i].length() - 1);
					idx = i;
					break;
				}
			}
			ListPreference l = (ListPreference)findPreference(getString(R.string.key_iosched));
			l.setEntries(sched);
			l.setEntryValues(sched);
			l.setValueIndex(idx);
			l.setOnPreferenceChangeListener(this);
			preferences = PreferenceManager.getDefaultSharedPreferences(this);
			setPreference(getString(R.string.key_iosched), sched[idx]);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// reload preferences
		reloadPreferences();
	}

	private void reloadPreferences() {
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		// setup display for each preference
		updateDisplay(getString(R.string.key_bld_status), getString(R.string.key_bld_delay));
		updateDisplay(getString(R.string.key_bln_status));
		updateDisplay(getString(R.string.key_touchwake_status), getString(R.string.key_touchwake_delay));
				
		// update display for BLX
		Preference pref = findPreference(getString(R.string.key_blx_charging_limit));
		String value = preferences.getString(getString(R.string.key_blx_charging_limit), "-1");
		if(value.equals("-1")) {
			pref.setEnabled(false);
			pref.setSummary(getString(R.string.status_not_available));
		}else {
			pref.setEnabled(true);
			pref.setSummary(value);
		}
		// cmled blink+timeout
		updateDisplay(getString(R.string.key_cmled_blink), getString(R.string.key_cmled_blinktimeout));		
		// update display for cmled timeout
		pref = findPreference(getString(R.string.key_cmled_bltimeout));
		value = preferences.getString(getString(R.string.key_cmled_bltimeout), "-1");
		if(value.equals("-1")) {
			pref.setEnabled(false);
			pref.setSummary(getString(R.string.status_not_available));
		}else {
			pref.setEnabled(true);
			pref.setSummary(value);
		}
		
		// update iosched
		pref = findPreference(getString(R.string.key_iosched));
		value = preferences.getString(getString(R.string.key_iosched), "-1");
		if(value.equals("-1")) {
			pref.setEnabled(false);
			pref.setSummary(getString(R.string.status_not_available));
		}else {
			pref.setEnabled(true);
			pref.setSummary(value);
		}
	}
	
	/**
	 * update display for specified tweak
	 * for tweak that has status and delay properties
	 * (ie. BLD, screendimmer, touchwake)
	 * 
	 * @param keyStatus
	 * @param keyDelay
	 */
	private void updateDisplay(String keyStatus, String keyDelay) {
		final Preference prefStatus = findPreference(keyStatus);
		final Preference prefDelay = findPreference(keyDelay);
		String status = preferences.getString(keyStatus, "-1");
		String delay = preferences.getString(keyDelay, "0");
		if(status.equals("1")) {
			prefStatus.setSummary(getString(R.string.status_on));
			prefDelay.setSummary(delay);
			prefDelay.setEnabled(true);
		}else if(status.equals("0")){
			prefStatus.setSummary(getString(R.string.status_off));
			prefDelay.setSummary(delay);
			prefDelay.setEnabled(false);
		}else {
			prefStatus.setSummary(getString(R.string.status_not_available));
			prefStatus.setEnabled(false);
			prefDelay.setSummary(delay);
			prefDelay.setEnabled(false);
		}
	}
	
	/**
	 * update display for specified tweak
	 * for tweak that only has status property
	 * (ie. BLN, deepidle)
	 * 
	 * @param keyStatus
	 */
	private void updateDisplay(String keyStatus) {
		final Preference prefStatus = findPreference(keyStatus);
		String status = preferences.getString(keyStatus, "-1");
		if(status.equals("1")) {
			prefStatus.setSummary(getString(R.string.status_on));
		}else if(status.equals("0")){
			prefStatus.setSummary(getString(R.string.status_off));
		}else {
			prefStatus.setSummary(getString(R.string.status_not_available));
			prefStatus.setEnabled(false);
		}
	}
	
	/**
	 * toggle tweak on/off
	 * 
	 * @param keyStatus
	 * @param deviceString
	 */
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
		reloadPreferences();
	}

	
	private void setPreference(String key, String value) {
		Editor ed = preferences.edit();
		ed.putString(key, value);
		ed.commit();
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		boolean ret = false;
		
		if(preference.isEnabled()) {
			if(preference.getKey().equals(getString(R.string.key_bld_status))) {
				toggleTweakStatus(preference.getKey(), "/sys/class/misc/backlightdimmer/enabled");
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_bln_status))) {
				toggleTweakStatus(preference.getKey(), "/sys/class/misc/backlightnotification/enabled");
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_touchwake_status))) {
				toggleTweakStatus(preference.getKey(), "/sys/class/misc/touchwake/enabled");
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_cmled_blink))) {
				toggleTweakStatus(preference.getKey(), "/sys/class/misc/notification/blink");
				ret = true;
			}
		}
		return ret;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(getString(R.string.key_bld_delay))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/backlightdimmer/delay");
			setPreference(getString(R.string.key_bld_delay), newValue.toString());
			reloadPreferences();
		}else if(preference.getKey().equals(getString(R.string.key_blx_charging_limit))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/batterylifeextender/charging_limit");
			setPreference(getString(R.string.key_blx_charging_limit), newValue.toString());
			reloadPreferences();
		}else if(preference.getKey().equals(getString(R.string.key_touchwake_delay))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/touchwake/delay");
			setPreference(getString(R.string.key_touchwake_delay), newValue.toString());
			reloadPreferences();
		}else if(preference.getKey().equals(getString(R.string.key_cmled_bltimeout))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/notification/bl_timeout");
			setPreference(getString(R.string.key_cmled_bltimeout), newValue.toString());
			reloadPreferences();
		}else if(preference.getKey().equals(getString(R.string.key_cmled_blinktimeout))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/notification/blinktimeout");
			setPreference(getString(R.string.key_cmled_blinktimeout), newValue.toString());
			reloadPreferences();
		}else if(preference.getKey().equals(getString(R.string.key_iosched))) {
			if(!preferences.getString(preference.getKey(), "-1").equals(newValue)) {
				// /system and /data
				SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/block/mmcblk0/queue/scheduler");
				// /cache
				SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/block/mtdblock4/queue/scheduler");
				setPreference(getString(R.string.key_iosched), newValue.toString());
				reloadPreferences();
			}
		}
		return false;
	}
	
	
}
