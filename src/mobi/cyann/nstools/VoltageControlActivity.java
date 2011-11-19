/**
 * VoltageControlActivity.java
 * Nov 6, 2011 7:27:58 PM
 */
package mobi.cyann.nstools;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

/**
 * @author arif
 *
 */
public class VoltageControlActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	private final static String LOG_TAG = "NSTools.VoltageControlActivity";
	
	private SharedPreferences preferences;
	
	private List<String> armVoltages;
	private List<String> intVoltages;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.voltage);

		armVoltages = new ArrayList<String>();
		intVoltages = new ArrayList<String>();
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		readVoltages(getString(R.string.key_max_arm_volt), getString(R.string.key_arm_volt_pref), "armvolt_", "/sys/class/misc/customvoltage/arm_volt", armVoltages);
		readVoltages(getString(R.string.key_max_int_volt), getString(R.string.key_int_volt_pref), "intvolt_", "/sys/class/misc/customvoltage/int_volt", intVoltages);
		
		if(preferences.getString(getString(R.string.key_max_arm_volt), "-1").equals("-1")) {
			// if we can't get customvoltage mod, then try to read UV_mV_table
			readUvmvTable();
		}
		findPreference(getString(R.string.key_default_voltage)).setOnPreferenceChangeListener(this);
	}
	
	private void showWarningDialog() {
		if(!preferences.getBoolean(getString(R.string.key_dont_show_volt_warning), false)) {
			final View content = getLayoutInflater().inflate(R.layout.warning_dialog, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(content);
			builder.setTitle(getString(R.string.label_warning));
			builder.setPositiveButton(getString(R.string.label_ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean val = ((CheckBox)content.findViewById(R.id.checkboxWarning)).isChecked();
					Editor ed = preferences.edit();
					ed.putBoolean(getString(R.string.key_dont_show_volt_warning), val);
					ed.commit();
					dialog.dismiss();
				}
			});
			builder.show();
		}
	}
	
	private void readUvmvTable() {
		boolean enabled = !preferences.getBoolean(getString(R.string.key_default_voltage), true);
		PreferenceCategory c = (PreferenceCategory)findPreference(getString(R.string.key_arm_volt_pref));
		SysCommand sc = SysCommand.getInstance();
		int count = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
		for(int i = 0; i < count; ++i) {
			String line = sc.getLastResult(i);
			String parts[] = line.split(":");
			final String volt = parts[1].substring(1, parts[1].length()-3);

			Log.d(LOG_TAG, line);
			
			EditTextPreference ed = new EditTextPreference(this);
			ed.setKey("uvmvtable_" + i);
			ed.setTitle(parts[0]);
			ed.setDialogTitle(parts[0]);
			ed.setSummary(parts[1]);
			ed.setText(volt);
			ed.setOnPreferenceChangeListener(this);
			ed.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
			ed.setEnabled(enabled);
			c.addPreference(ed);
			
			armVoltages.add(volt);
		}
		saveVoltages(getString(R.string.key_uvmvtable_pref), armVoltages, null);
	}

	private void readVoltages(String maxKey, String catKey, String voltPrefix, String voltDevice, List<String>voltList) {
		// read max arm volt
		EditTextPreference p = (EditTextPreference)findPreference(maxKey);
		String volt = preferences.getString(maxKey, "-1");
		boolean enabled = !preferences.getBoolean(getString(R.string.key_default_voltage), true);
		if(!volt.equals("-1")) {
			// Max volt
			p.setText(volt);
			p.setSummary(volt + " mV");
			p.setOnPreferenceChangeListener(this);
			p.setEnabled(enabled);
			
			PreferenceCategory c = (PreferenceCategory)findPreference(catKey);
			
			while(c.getPreferenceCount() > 1) { // clear all preference except the first one
				Preference tp = c.getPreference(1);
				c.removePreference(tp);
			}
			
			SysCommand sc = SysCommand.getInstance();
			int count = sc.suRun("cat", voltDevice);
			for(int i = 0; i < count; ++i) {
				String line = sc.getLastResult(i);
				String parts[] = line.split(":");
				volt = parts[1].substring(1, parts[1].length()-3);
	
				Log.d(LOG_TAG, line);
				
				EditTextPreference ed = new EditTextPreference(this);
				ed.setKey(voltPrefix + i);
				ed.setTitle(parts[0]);
				ed.setDialogTitle(parts[0]);
				ed.setSummary(parts[1]);
				ed.setText(volt);
				ed.setOnPreferenceChangeListener(this);
				ed.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				ed.setEnabled(enabled);
				c.addPreference(ed);
				
				voltList.add(volt);
			}
			saveVoltages(catKey, voltList, null);
		}else {
			// disable it
			p.setEnabled(false);
			p.setSummary(getString(R.string.status_not_available));
			saveVoltage(maxKey, "-1", null);
		}
	}
	
	private void saveVoltage(String key, String value, String deviceString) {
		if(deviceString != null) {
			SysCommand.getInstance().suRun("echo", value, ">", deviceString);
		}
		// save to xml pref
		Editor ed = preferences.edit();
		ed.putString(key, value);
		ed.commit();
	}

	private void saveVoltages(String key, List<String> voltageList, String deviceString) {
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < voltageList.size(); ++i) {
			s.append(voltageList.get(i));
			s.append(" ");
		}
		Log.d(LOG_TAG, "voltages:" + s.toString());
		if(deviceString != null) {
			SysCommand.getInstance().suRun("echo", "\""+s.toString()+"\"", ">", deviceString);
		}
		// save to xml pref
		Editor ed = preferences.edit();
		ed.putString(key, s.toString());
		ed.commit();
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().startsWith("armvolt_")) {
			String parts[] = preference.getKey().split("_");
			int i = Integer.parseInt(parts[1]);
			armVoltages.set(i, newValue.toString());
			preference.setSummary(newValue + " mV");
			((EditTextPreference)preference).setText(newValue.toString());
			saveVoltages(getString(R.string.key_arm_volt_pref), armVoltages, "/sys/class/misc/customvoltage/arm_volt");
		}else if(preference.getKey().startsWith("uvmvtable_")) {
			String parts[] = preference.getKey().split("_");
			int i = Integer.parseInt(parts[1]);
			armVoltages.set(i, newValue.toString());
			preference.setSummary(newValue + " mV");
			((EditTextPreference)preference).setText(newValue.toString());
			saveVoltages(getString(R.string.key_uvmvtable_pref), armVoltages, "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
		}else if(preference.getKey().equals(getString(R.string.key_max_arm_volt))) {
			preference.setSummary(newValue + " mV");
			((EditTextPreference)preference).setText(newValue.toString());
			saveVoltage(getString(R.string.key_max_arm_volt), newValue.toString(), "/sys/class/misc/customvoltage/max_arm_volt");
		}else if(preference.getKey().startsWith("intvolt_")) {
			String parts[] = preference.getKey().split("_");
			int i = Integer.parseInt(parts[1]);
			intVoltages.set(i, newValue.toString());
			preference.setSummary(newValue + " mV");
			((EditTextPreference)preference).setText(newValue.toString());
			saveVoltages(getString(R.string.key_int_volt_pref), intVoltages, "/sys/class/misc/customvoltage/int_volt");
		}else if(preference.getKey().equals(getString(R.string.key_max_int_volt))) {
			preference.setSummary(newValue + " mV");
			((EditTextPreference)preference).setText(newValue.toString());
			saveVoltage(getString(R.string.key_max_int_volt), newValue.toString(), "/sys/class/misc/customvoltage/max_int_volt");
		}else if(preference.getKey().equals(getString(R.string.key_default_voltage))) {
			boolean enabled = !Boolean.parseBoolean(newValue.toString());
			PreferenceCategory c = (PreferenceCategory)findPreference(getString(R.string.key_arm_volt_pref));
			for(int i = 0; i < c.getPreferenceCount(); ++i) {
				c.getPreference(i).setEnabled(enabled);
			}
			c = (PreferenceCategory)findPreference(getString(R.string.key_int_volt_pref));
			for(int i = 0; i < c.getPreferenceCount(); ++i) {
				c.getPreference(i).setEnabled(enabled);
			}
			if(enabled) {
				showWarningDialog();
			}
			return true;
		}
		return false;
	}
}
