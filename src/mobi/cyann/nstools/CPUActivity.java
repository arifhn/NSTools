/**
 * CPUActivity.java
 * Nov 12, 2011 10:49:10 AM
 */
package mobi.cyann.nstools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.cyann.nstools.preference.BasePreference;
import mobi.cyann.nstools.preference.BasePreference.OnPreferenceChangedListener;
import mobi.cyann.nstools.preference.IntegerPreference;
import mobi.cyann.nstools.preference.ListPreference;
import mobi.cyann.nstools.preference.LulzactiveScreenOffPreference;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class CPUActivity extends BasePreferenceActivity implements OnPreferenceChangedListener, OnPreferenceClickListener {
	private final static String LOG_TAG = "NSTools.CPUActivity";
	
	private ListPreference governor;
	private ListPreference minFreq;
	private ListPreference maxFreq;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// set preference layout
		addPreferencesFromResource(R.xml.cpu);
		
		// deepidle stats
		Preference p = findPreference(getString(R.string.key_deepidle_stats));
		if(p != null) {
			p.setOnPreferenceClickListener(this);
		}
		
		p = findPreference(getString(R.string.key_liveoc));
		if(p != null) {
			((BasePreference)p).setChangedListener(this);
		}
		
		governor = (ListPreference)findPreference(getString(R.string.key_governor));
		minFreq = (ListPreference)findPreference(getString(R.string.key_min_cpufreq));
		maxFreq = (ListPreference)findPreference(getString(R.string.key_max_cpufreq));
		
		reloadGovernors();
		reloadFrequencies();
	}
	
	private void reloadGovernors() {
		String availableGovernors[] = null;
		String temp = PreloadValues.getInstance().getString("key_available_governor");
		if(temp != null) {
			availableGovernors = temp.split(" ");
		}
		governor.setListLabels(availableGovernors);
		governor.setListValues(availableGovernors);
	}

	private void reloadFrequencies() {
		SysCommand sc = SysCommand.getInstance();
		Integer availableFreqs[] = null;
		String availableFreqsStr[] = null;
		int n = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
		if(n > 0) {
			String temp = sc.getLastResult(0);
			String f[] = temp.split(" ");
			availableFreqs = new Integer[f.length];
			availableFreqsStr = new String[f.length];
			for(int i = 0; i < f.length; ++i) {
				availableFreqs[i] = Integer.parseInt(f[i]);
				availableFreqsStr[i] = (availableFreqs[i] / 1000) + " MHz";
			}
		}else {
			// try read available frequencies from time_in_state
			n = sc.suRun("cat", "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
			if(n >= 0) {
				availableFreqs = new Integer[n];
				availableFreqsStr = new String[n];
				for(int i = 0; i < n; ++i) {
					String line = sc.getLastResult(i);
					String parts[] = line.split(" ");
					availableFreqs[i] = Integer.parseInt(parts[0]);
					availableFreqsStr[i] = (availableFreqs[i] / 1000) + " MHz";
				}
			}
		}
		minFreq.setListValues(availableFreqs);
		minFreq.setListLabels(availableFreqsStr);
		minFreq.reload();
		maxFreq.setListValues(availableFreqs);
		maxFreq.setListLabels(availableFreqsStr);
		maxFreq.reload();
		
		// setup lulzactive min/max step
		if(findPreference(getString(R.string.key_lulzactive_pump_up_step)) != null) {
			IntegerPreference p = (IntegerPreference)findPreference(getString(R.string.key_lulzactive_pump_up_step));
			p.setMaxValue(availableFreqs.length - 1);
			
			p = (IntegerPreference)findPreference(getString(R.string.key_lulzactive_pump_down_step));
			p.setMaxValue(availableFreqs.length - 1);
			
			p = (IntegerPreference)findPreference(getString(R.string.key_lulzactive_screen_off_min_step));
			p.setMaxValue(availableFreqs.length - 1);
			((LulzactiveScreenOffPreference)p).setAvailableFrequencies(availableFreqsStr);
		}
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
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		boolean ret = false;
		if(preference.getKey().equals(getString(R.string.key_deepidle_stats))) {
			showIdleStatsDialog();
			ret = true;
		}
		return ret;
	}

	@Override
	public void onPreferenceChanged(Preference preference) {
		if(preference.getKey().equals(getString(R.string.key_liveoc))) {
			reloadFrequencies();
		}
	}
}
