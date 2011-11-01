package mobi.cyann.nstools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class NSTweakActivity extends PreferenceActivity implements OnPreferenceClickListener {
	private final static String LOG_TAG = "NSTools.NSTweakActivity";
	
	private Properties properties;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ns_tweak);
		
		Preference p = findPreference(getString(R.string.key_bld_status));
		p.setOnPreferenceClickListener(this);
		p = findPreference(getString(R.string.key_screen_dimmer_status));
		p.setOnPreferenceClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		try {
			// create prop file
			File propFile = new File(getString(R.string.NS_TWEAK_FILE));
			propFile.createNewFile();
			
			// run our data collector script
			SysCommand.getInstance().suRun(getString(R.string.NS_TWEAK_SCRIPT));
			
			if(properties == null) {
				properties = new Properties();
			}
			properties.load(new FileInputStream(getString(R.string.NS_TWEAK_FILE)));
		}catch(Exception e) {
			Log.e(LOG_TAG, "", e);
		}
		checkBLD();
		checkScreenDimmer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			properties.store(new FileOutputStream(getString(R.string.NS_TWEAK_FILE)), "");
		}catch(Exception e) {
			Log.e(LOG_TAG, "", e);
		}
	}

	private void checkBLD() {
		final Preference bldStatus = findPreference(getString(R.string.key_bld_status));
		final Preference bldDelay = findPreference(getString(R.string.key_bld_delay));
		String status = properties.getProperty("bld.status");
		String delay = properties.getProperty("bld.delay");
		if(status.equals("1")) {
			bldStatus.setTitle(getString(R.string.label_status, getString(R.string.status_on)));
			bldDelay.setTitle(getString(R.string.label_delay, delay));
			bldDelay.setEnabled(true);
		}else if(status.equals("0")){
			bldStatus.setTitle(getString(R.string.label_status, getString(R.string.status_off)));
			bldDelay.setTitle(getString(R.string.label_delay, delay));
			bldDelay.setEnabled(false);
		}else {
			bldStatus.setTitle(getString(R.string.label_status,getString(R.string.status_not_supported)));
			bldStatus.setEnabled(false);
			bldDelay.setTitle(getString(R.string.label_delay, "0"));
			bldDelay.setEnabled(false);
		}
	}

	private void checkScreenDimmer() {
		final Preference screenDimmerStatus = findPreference(getString(R.string.key_screen_dimmer_status));
		final Preference screenDimmerDelay = findPreference(getString(R.string.key_screen_dimmer_delay));
		String status = properties.getProperty("screendimmer.status");
		String delay = properties.getProperty("screendimmer.delay");
		if(status.equals("1")) {
			screenDimmerStatus.setTitle(getString(R.string.label_status, getString(R.string.status_on)));
			screenDimmerDelay.setTitle(getString(R.string.label_delay, delay));
			screenDimmerDelay.setEnabled(true);
		}else if(status.equals("0")){
			screenDimmerStatus.setTitle(getString(R.string.label_status, getString(R.string.status_off)));
			screenDimmerDelay.setTitle(getString(R.string.label_delay, delay));
			screenDimmerDelay.setEnabled(false);
		}else {
			screenDimmerStatus.setTitle(getString(R.string.label_status,getString(R.string.status_not_supported)));
			screenDimmerStatus.setEnabled(false);
			screenDimmerDelay.setTitle(getString(R.string.label_delay, "0"));
			screenDimmerDelay.setEnabled(false);
		}
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		boolean ret = false;
		
		if(preference.isEnabled()) {
			if(preference.getKey().equals(getString(R.string.key_bld_status))) {
				final Preference bldStatus = findPreference(getString(R.string.key_bld_status));
				final Preference bldDelay = findPreference(getString(R.string.key_bld_delay));
				if(properties.getProperty("bld.status").equals("1")) {
					// disable BLD
					SysCommand.getInstance().suRun("echo", "0", ">", "/sys/class/misc/backlightdimmer/enabled");
					bldStatus.setTitle(getString(R.string.label_status, getString(R.string.status_off)));
					bldDelay.setEnabled(false);
					properties.setProperty("bld.status", "0");
				}else if(properties.getProperty("bld.status").equals("0")) {
					// enable BLD
					SysCommand.getInstance().suRun("echo", "1", ">", "/sys/class/misc/backlightdimmer/enabled");
					bldStatus.setTitle(getString(R.string.label_status, getString(R.string.status_on)));
					bldDelay.setEnabled(true);
					properties.setProperty("bld.status", "1");
				}
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_bld_delay))) {
				
				ret = true;
			}else if(preference.getKey().equals(getString(R.string.key_screen_dimmer_status))) {
				final Preference screenDimmerStatus = findPreference(getString(R.string.key_screen_dimmer_status));
				final Preference screenDimmerDelay = findPreference(getString(R.string.key_screen_dimmer_delay));
				if(properties.getProperty("screendimmer.status").equals("1")) {
					// disable BLD
					SysCommand.getInstance().suRun("echo", "0", ">", "/sys/class/misc/screendimmer/enabled");
					screenDimmerStatus.setTitle(getString(R.string.label_status, getString(R.string.status_off)));
					screenDimmerDelay.setEnabled(false);
					properties.setProperty("screendimmer.status", "0");
				}else if(properties.getProperty("screendimmer.status").equals("0")) {
					// enable BLD
					SysCommand.getInstance().suRun("echo", "1", ">", "/sys/class/misc/screendimmer/enabled");
					screenDimmerStatus.setTitle(getString(R.string.label_status, getString(R.string.status_on)));
					screenDimmerDelay.setEnabled(true);
					properties.setProperty("screendimmer.status", "1");
				}
				ret = true;
			}
		}
		return ret;
	}
}
