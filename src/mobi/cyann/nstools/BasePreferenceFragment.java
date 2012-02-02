/**
 * BasePreferenceFragment.java
 * Nov 25, 2011 10:05:19 PM
 */
package mobi.cyann.nstools;

import java.util.ArrayList;
import java.util.List;

import mobi.cyann.nstools.PreferenceListFragment.OnPreferenceAttachedListener;
import mobi.cyann.nstools.preference.BasePreference;
import mobi.cyann.nstools.preference.RemovablePreferenceCategory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

/**
 * @author arif
 *
 */
public class BasePreferenceFragment extends PreferenceListFragment implements OnPreferenceAttachedListener {
	private final static String ROOT = "root";
	
	private ReloadListener reloadListener;
	
	public BasePreferenceFragment(int id) {
		super(id);
		setOnPreferenceAttachedListener(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reloadListener = new ReloadListener();
		getActivity().registerReceiver(reloadListener, new IntentFilter("mobi.cyann.nstools.RELOAD"));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
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
				((BasePreference<?>) p).onResume();
			}
		}
	}
    
	@Override
	public void onPreferenceAttached(PreferenceScreen rootPreference, int xmlId) {
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
	
	private class ReloadListener extends BroadcastReceiver {
		@Override
        public void onReceive(Context context, Intent intent) {
        	List<Preference> list = new ArrayList<Preference>();
    		list.add(findPreference(ROOT));
    		while(list.size() > 0) {
    			Preference p = list.remove(0);
    			if(p instanceof PreferenceGroup) {
    				int n = ((PreferenceGroup) p).getPreferenceCount();
    				for(int i = 0; i < n; ++i)
    					list.add(((PreferenceGroup) p).getPreference(i));
    			}else if(p instanceof BasePreference) {
    				// reload from PreloadValues
    				((BasePreference<?>) p).reload(true);
    			}
    		}
        }
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(reloadListener);
	}
}
