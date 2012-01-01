/**
 * SettingActivity.java
 * Nov 5, 2011 3:12:07 PM
 */
package mobi.cyann.nstools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * @author arif
 *
 */
public class SettingActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.setting);
		
		findPreference(getString(R.string.key_about)).setOnPreferenceClickListener(this);
		findPreference(getString(R.string.key_load_settings)).setOnPreferenceChangeListener(this);
		findPreference(getString(R.string.key_save_settings)).setOnPreferenceClickListener(this);
		findPreference(getString(R.string.key_save_settings)).setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(getString(R.string.key_about))) {
			Intent browse = new Intent();
			browse.setAction(Intent.ACTION_VIEW);
			browse.setData(Uri.parse(getString(R.string.nstools_thread_url)));
			startActivity(browse);
		}else if(preference.getKey().equals(getString(R.string.key_save_settings))) {
			((EditTextPreference)preference).getEditText().setText("");
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(getString(R.string.key_load_settings))) {
			if(newValue != null && newValue.toString().length() > 0) {
				SettingsManager.loadSettings(this, newValue.toString());
				MainActivity.restart(this);
			}
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_save_settings))) {
			if(newValue != null && newValue.toString().length() > 0) {
				if(SettingsManager.saveSettings(this, newValue.toString())) {
					Toast.makeText(this, getString(R.string.save_success_message), Toast.LENGTH_LONG).show();
				}else {
					Toast.makeText(this, getString(R.string.save_failed_message), Toast.LENGTH_LONG).show();
				}
			}
			return true;
		}
		return false;
	}
}
