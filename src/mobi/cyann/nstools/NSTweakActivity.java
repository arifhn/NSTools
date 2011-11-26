package mobi.cyann.nstools;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

public class NSTweakActivity extends BasePreferenceActivity implements OnPreferenceChangeListener {
	//private final static String LOG_TAG = "NSTools.NSTweakActivity";
	
	private SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// set preference layout
		addPreferencesFromResource(R.xml.ns_tweak);
		
		// Set onPreferenceClickListener to this class
		// --------------------------------------------
		
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
		
		// update iosched
		Preference pref = findPreference(getString(R.string.key_iosched));
		String value = preferences.getString(getString(R.string.key_iosched), "-1");
		if(value.equals("-1")) {
			pref.setEnabled(false);
			pref.setSummary(getString(R.string.status_not_available));
		}else {
			pref.setEnabled(true);
			pref.setSummary(value);
		}
	}
	
	private void setPreference(String key, String value) {
		Editor ed = preferences.edit();
		ed.putString(key, value);
		ed.commit();
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(getString(R.string.key_iosched))) {
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
