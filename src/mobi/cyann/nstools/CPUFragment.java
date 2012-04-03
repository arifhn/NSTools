/**
 * CPUFragment.java
 * Nov 12, 2011 10:49:10 AM
 */
package mobi.cyann.nstools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.cyann.nstools.preference.BasePreference.OnPreferenceChangedListener;
import mobi.cyann.nstools.preference.IntegerPreference;
import mobi.cyann.nstools.preference.ListPreference;
import mobi.cyann.nstools.preference.LulzactiveScreenOffPreference;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class CPUFragment extends BasePreferenceFragment implements OnPreferenceChangedListener, OnPreferenceClickListener {
	private final static String LOG_TAG = "NSTools.CPUActivity";
	
	private ListPreference governor;
	private ListPreference minFreq;
	private ListPreference maxFreq;
	private ListPreference liveocTargetLow;
	private ListPreference liveocTargetHigh;
	
	public CPUFragment() {
		super(R.layout.cpu);
	}
	
	@Override
	public void onPreferenceAttached(PreferenceScreen rootPreference, int xmlId) {
		super.onPreferenceAttached(rootPreference, xmlId);
		
		// deepidle stats
		Preference p = findPreference(getString(R.string.key_deepidle_stats));
		if(p != null) {
			p.setOnPreferenceClickListener(this);
		}
		
		p = findPreference(getString(R.string.key_liveoc));
		if(p != null) {
			((IntegerPreference)p).setChangedListener(this);
			liveocTargetLow = (ListPreference)findPreference(getString(R.string.key_liveoc_target_low));
			liveocTargetHigh = (ListPreference)findPreference(getString(R.string.key_liveoc_target_high));
			if(liveocTargetLow != null && liveocTargetHigh != null) {
				liveocTargetLow.setChangedListener(this);
				liveocTargetHigh.setChangedListener(this);
				reloadLiveocTarget();
			}
		}
		governor = (ListPreference)findPreference(getString(R.string.key_governor));
		if(governor != null) {
			reloadGovernors();
		}
		minFreq = (ListPreference)findPreference(getString(R.string.key_min_cpufreq));
		maxFreq = (ListPreference)findPreference(getString(R.string.key_max_cpufreq));
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

	private Integer[] readAvailableFrequencies() {
		SysCommand sc = SysCommand.getInstance();
		Integer availableFreqs[] = null;
		int n = sc.readSysfs("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
		if(n > 0) {
			String temp = sc.getLastResult(0);
			String f[] = temp.split(" ");
			availableFreqs = new Integer[f.length];
			for(int i = 0; i < f.length; ++i) {
				availableFreqs[i] = Integer.parseInt(f[i]);
			}
		}else {
			// try read available frequencies from time_in_state
			n = sc.readSysfs("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
			if(n >= 0) {
				availableFreqs = new Integer[n];
				for(int i = 0; i < n; ++i) {
					String line = sc.getLastResult(i);
					String parts[] = line.split(" ");
					availableFreqs[i] = Integer.parseInt(parts[0]);
				}
			}
		}
		return availableFreqs;
	}
	
	private void reloadLiveocTarget() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		int liveoc = preferences.getInt(getActivity().getString(R.string.key_liveoc), -1);
		SysCommand sc = SysCommand.getInstance();
		// we need to set liveoc to 100 before read the frequencies
		sc.writeSysfs("/sys/class/misc/liveoc/oc_value", "100");
		Integer targetFreqs[] = readAvailableFrequencies();
		String targetFreqsStr[] = new String[targetFreqs.length];
		for(int i = 0; i < targetFreqs.length; ++i) {
			targetFreqsStr[i] = (targetFreqs[i] / 1000) + " MHz";
		}
		liveocTargetLow.setListValues(targetFreqs);
		liveocTargetLow.setListLabels(targetFreqsStr);
		liveocTargetLow.reload(false);
		liveocTargetHigh.setListValues(targetFreqs);
		liveocTargetHigh.setListLabels(targetFreqsStr);
		liveocTargetHigh.reload(false);
		sc.writeSysfs("/sys/class/misc/liveoc/oc_value", "" + liveoc);
	}
	
	private void reloadFrequencies() {
		if(minFreq != null && maxFreq != null) {
			Integer availableFreqs[] = readAvailableFrequencies();
			String availableFreqsStr[] = new String[availableFreqs.length];
			for(int i = 0; i < availableFreqs.length; ++i) {
				availableFreqsStr[i] = (availableFreqs[i] / 1000) + " MHz";
			}
				
			minFreq.setListValues(availableFreqs);
			minFreq.setListLabels(availableFreqsStr);
			minFreq.reload(false);
			maxFreq.setListValues(availableFreqs);
			maxFreq.setListLabels(availableFreqsStr);
			maxFreq.reload(false);
			
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
	}
	
	private void showIdleStatsDialog() {
		// display dialog
		final int timeText[] = {R.id.time1, R.id.time2, R.id.time3};
		final int avgText[] = {R.id.avg1, R.id.avg2, R.id.avg3};
		
		final View content = getActivity().getLayoutInflater().inflate(R.layout.idle_stats_dialog, null);
		
		Pattern time = Pattern.compile("([0-9]+)ms");
		Pattern average = Pattern.compile("\\(([0-9]+)ms\\)");
		
		SysCommand.getInstance().readSysfs("/sys/class/misc/deepidle/idle_stats");
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
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.label_deepidle_stats));
		builder.setView(content);
		builder.setPositiveButton(getString(R.string.label_reset), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(SysCommand.getInstance().writeSysfs("/sys/class/misc/deepidle/reset_stats", "1") < 0) {
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
		if(preference.getKey().equals(getString(R.string.key_liveoc)) ||
			preference.getKey().equals(getString(R.string.key_liveoc_target_high)) ||
			preference.getKey().equals(getString(R.string.key_liveoc_target_low))) {
			
			reloadFrequencies();
		}
	}
}
