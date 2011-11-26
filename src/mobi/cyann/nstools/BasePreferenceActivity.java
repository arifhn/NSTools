/**
 * BasePreferenceActivity.java
 * Nov 25, 2011 10:05:19 PM
 */
package mobi.cyann.nstools;

import java.util.ArrayList;
import java.util.List;

import mobi.cyann.nstools.preference.BasePreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;

/**
 * @author arif
 *
 */
public class BasePreferenceActivity extends PreferenceActivity {
	@Override
	protected void onResume() {
		List<Preference> list = new ArrayList<Preference>();
		list.add(getPreferenceScreen());
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
}
