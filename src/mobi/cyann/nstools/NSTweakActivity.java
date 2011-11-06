package mobi.cyann.nstools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.EditText;

public class NSTweakActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
	private final static String LOG_TAG = "NSTools.NSTweakActivity";
	
	private SharedPreferences preferences;
	
	private boolean dirtyPreferences = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// execute our reader script to get values for each tweak
		SysCommand.getInstance().suRun(getString(R.string.NS_TWEAK_SCRIPT));

		// reload preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		// set 'dirty' flag
		dirtyPreferences = false;
		
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
		
		// Deepidle status & stas
		p = findPreference(getString(R.string.key_deepidle_status));
		p.setOnPreferenceClickListener(this);
		p = findPreference(getString(R.string.key_deepidle_stats));
		p.setOnPreferenceClickListener(this);
		p = findPreference(getString(R.string.key_deepidle_reset_stats));
		p.setOnPreferenceClickListener(this);
				
		// Screendimmer status
		p = findPreference(getString(R.string.key_screendimmer_status));
		p.setOnPreferenceClickListener(this);

		// Touchwake status
		p = findPreference(getString(R.string.key_touchwake_status));
		p.setOnPreferenceClickListener(this);
		

		// Set EditTextPreference so only numbers are allowed for input
		// and then set onPreferenceChangeListener to this activity
		// -------------------------------------------------------------
		
		// BLD delay
		p = findPreference(getString(R.string.key_bld_delay));
		EditText editText = ((EditTextPreference)p).getEditText();
		editText.setKeyListener(DigitsKeyListener.getInstance(false,true));
		p.setOnPreferenceChangeListener(this);

		// BLX charging limit
		p = findPreference(getString(R.string.key_blx_charging_limit));
		editText = ((EditTextPreference)p).getEditText();
		editText.setKeyListener(DigitsKeyListener.getInstance(false,true));
		p.setOnPreferenceChangeListener(this);

		// Liveoc oc value
		p = findPreference(getString(R.string.key_liveoc));
		editText = ((EditTextPreference)p).getEditText();
		editText.setKeyListener(DigitsKeyListener.getInstance(false,true));
		p.setOnPreferenceChangeListener(this);
		
		// Screendimmer delay
		p = findPreference(getString(R.string.key_screendimmer_delay));
		editText = ((EditTextPreference)p).getEditText();
		editText.setKeyListener(DigitsKeyListener.getInstance(false,true));
		p.setOnPreferenceChangeListener(this);

		// Touchwake delay
		p = findPreference(getString(R.string.key_touchwake_delay));
		editText = ((EditTextPreference)p).getEditText();
		editText.setKeyListener(DigitsKeyListener.getInstance(false,true));
		p.setOnPreferenceChangeListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// reload preferences
		reloadPreferences();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// set 'dirty' flag to true (just in case the values changed from other process)
		// so we can reload it later onResume
		dirtyPreferences = true;
	}

	private void reloadPreferences() {
		if(dirtyPreferences) {
			// execute our reader script once again if 'dirty' flag is true
			SysCommand.getInstance().suRun(getString(R.string.NS_TWEAK_SCRIPT));
			// reload preferences
			preferences = PreferenceManager.getDefaultSharedPreferences(this);
		}
		// setup display for each preference
		updateDisplay(getString(R.string.key_bld_status), getString(R.string.key_bld_delay));
		updateDisplay(getString(R.string.key_bln_status));
		updateDisplay(getString(R.string.key_screendimmer_status), getString(R.string.key_screendimmer_delay));
		updateDisplay(getString(R.string.key_touchwake_status), getString(R.string.key_touchwake_delay));
		
		// update display for Deepidle
		Preference pref = findPreference(getString(R.string.key_deepidle_status));
		String value = preferences.getString(getString(R.string.key_deepidle_status), "-1");
		if(value.equals("1")) {
			pref.setEnabled(true);
			pref.setSummary(getString(R.string.status_on));
			findPreference(getString(R.string.key_deepidle_stats)).setEnabled(true);
		}else if(value.equals("0")) {
			pref.setEnabled(true);
			pref.setSummary(getString(R.string.status_off));
			findPreference(getString(R.string.key_deepidle_stats)).setEnabled(true);
		}else {
			pref.setEnabled(false);
			pref.setSummary(getString(R.string.status_not_available));
			findPreference(getString(R.string.key_deepidle_stats)).setEnabled(false);
		}
		
		// update display for BLX
		pref = findPreference(getString(R.string.key_blx_charging_limit));
		value = preferences.getString(getString(R.string.key_blx_charging_limit), "-1");
		if(value.equals("-1")) {
			pref.setEnabled(false);
			pref.setSummary(getString(R.string.status_not_available));
		}else {
			pref.setEnabled(true);
			pref.setSummary(value);
		}
		
		// update display for Liveoc
		pref = findPreference(getString(R.string.key_liveoc));
		value = preferences.getString(getString(R.string.key_liveoc), "-1");
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
			}else if(preference.getKey().equals(getString(R.string.key_deepidle_status))) {
				toggleTweakStatus(preference.getKey(), "/sys/class/misc/deepidle/enabled");
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_deepidle_stats))) {
				// display dialog
				int n = SysCommand.getInstance().suRun("cat", "/sys/class/misc/deepidle/idle_stats");
				StringBuilder str = new StringBuilder();
				for(int i = 0; i < n; ++i) {
					str.append(SysCommand.getInstance().getLastResult(i));
					str.append("\n");
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.label_deepidle_stats));
				builder.setMessage(str.toString());
				builder.setPositiveButton(getString(R.string.label_ok), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_deepidle_reset_stats))) {
				// create new message dialog with two button yes and no
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.label_deepidle_reset_stats));
				builder.setMessage(getString(R.string.msg_confirm_reset_deepidle_stats));
				builder.setPositiveButton(getString(R.string.label_yes), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// echo 1 to deepidle reset_stats
						if(SysCommand.getInstance().suRun("echo", "1", ">", "/sys/class/misc/deepidle/reset_stats") < 0) {
							Log.d(LOG_TAG, "failed to reset deepidle stats");
							SysCommand.getInstance().logLastError(LOG_TAG);
						}
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(getString(R.string.label_no), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();						
					}
				});
				builder.show();
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_screendimmer_status))) {
				toggleTweakStatus(preference.getKey(), "/sys/class/misc/screendimmer/enabled");
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_touchwake_status))) {
				toggleTweakStatus(preference.getKey(), "/sys/class/misc/touchwake/enabled");
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
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_blx_charging_limit))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/batterylifeextender/charging_limit");
			setPreference(getString(R.string.key_blx_charging_limit), newValue.toString());
			reloadPreferences();
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_liveoc))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/liveoc/oc_value");
			setPreference(getString(R.string.key_liveoc), newValue.toString());
			reloadPreferences();
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_screendimmer_delay))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/screendimmer/delay");
			setPreference(getString(R.string.key_screendimmer_delay), newValue.toString());
			reloadPreferences();
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_touchwake_delay))) {
			SysCommand.getInstance().suRun("echo", newValue.toString(), ">", "/sys/class/misc/touchwake/delay");
			setPreference(getString(R.string.key_touchwake_delay), newValue.toString());
			reloadPreferences();
			return true;
		}
		return false;
	}
	
	
}
