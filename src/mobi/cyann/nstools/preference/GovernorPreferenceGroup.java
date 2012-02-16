/**
 * GovernorPreferenceGroup.java
 * Jan 14, 2012 8:39:01 AM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.PreloadValues;
import mobi.cyann.nstools.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author arif
 *
 */
public class GovernorPreferenceGroup extends RemovablePreferenceCategory {
	private String governor;
	private static View blankView;
	
	public GovernorPreferenceGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public GovernorPreferenceGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GovernorPreferenceGroup(Context context) {
        this(context, null);
    }
    
    private void init(Context context, AttributeSet attrs) {
    	TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_GovernorPreferenceGroup, 0, 0);
		governor = a.getString(R.styleable.mobi_cyann_nstools_preference_GovernorPreferenceGroup_governor);
		a.recycle();
		if(blankView == null) {
			blankView = new FrameLayout(context);
		}
    }
    
    @Override
    public boolean canBeRemoved() {
    	String availableGovernor = PreloadValues.getInstance().getString("key_available_governor");
    	return availableGovernor == null || !availableGovernor.contains(governor);
    }

	private boolean isVisible() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
		String currentGovernor = pref.getString(getContext().getString(R.string.key_governor), "");
		return governor.equals(currentGovernor);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		if(isVisible()) {
			return super.onCreateView(parent);
		}else {
			return blankView;
		}
	}

	@Override
	public void onDependencyChanged(Preference dependency,
			boolean disableDependent) {
		
		boolean visible = isVisible();
		int count = getPreferenceCount();
    	for(int i = 0; i < count; ++i) {
    		Preference p = getPreference(i);
    		if(p instanceof BasePreference) {
    			BasePreference<?> bp = ((BasePreference<?>) p);
    			if(visible) {
    				// show child
    				bp.setVisible(true);
    				// reload values from interface
    				bp.reload(false);
    			}else {
    				// hide child
    				bp.setVisible(false);	
    			}
    		}
    	}
		super.onDependencyChanged(dependency, disableDependent);
	}
}
