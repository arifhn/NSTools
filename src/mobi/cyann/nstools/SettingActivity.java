/**
 * SettingActivity.java
 * Nov 5, 2011 3:12:07 PM
 */
package mobi.cyann.nstools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * @author arif
 *
 */
public class SettingActivity extends PreferenceActivity implements OnPreferenceClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.setting);
		
		findPreference(getString(R.string.key_about)).setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(getString(R.string.key_about))) {
			Intent browse = new Intent();
			browse.setAction(Intent.ACTION_VIEW);
			browse.setData(Uri.parse(getString(R.string.nstools_thread_url)));
			startActivity(browse);
		}
		return false;
	}
	
}
