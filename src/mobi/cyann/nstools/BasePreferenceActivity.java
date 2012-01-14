/**
 * BasePreferenceActivity.java
 * Nov 25, 2011 10:05:19 PM
 */
package mobi.cyann.nstools;

import java.util.ArrayList;
import java.util.List;

import mobi.cyann.nstools.preference.BasePreference;
import mobi.cyann.nstools.preference.RemovablePreferenceCategory;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

/**
 * @author arif
 *
 */
public class BasePreferenceActivity extends PreferenceActivity {
	private final static String ROOT = "root";
	
	@Override
	protected void onResume() {
		List<Preference> list = new ArrayList<Preference>();
		list.add(findPreference(ROOT));
		while(list.size() > 0) {
			Preference p = list.remove(0);
			if(p instanceof PreferenceGroup) {
				int n = ((PreferenceGroup) p).getPreferenceCount();
				for(int i = 0; i < n; ++i)
					list.add(((PreferenceGroup) p).getPreference(i));
			}else if(p instanceof BasePreference) {
				// call on resume
				((BasePreference) p).onResume();
			}
		}
		
		super.onResume();
	}

	@Override
	public void addPreferencesFromResource(int preferencesResId) {
		super.addPreferencesFromResource(preferencesResId);
		
		List<Preference> removeThem = new ArrayList<Preference>();
		PreferenceScreen root = (PreferenceScreen)findPreference(ROOT);
		for(int i = 0; i < root.getPreferenceCount(); ++i) {
			Preference p = root.getPreference(i);
			if(p instanceof RemovablePreferenceCategory && ((RemovablePreferenceCategory) p).canBeRemoved()) {
				removeThem.add(p);
			}
		}
		for(Preference removeIt: removeThem) {
			root.removePreference(removeIt);
		}
	}
}
