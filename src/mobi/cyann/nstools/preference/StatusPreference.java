/**
 * StatusPreference.java
 * Nov 24, 2011 9:00:58 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class StatusPreference extends BasePreference {
	private final static String LOG_TAG = "NSTools.StatusPreference";
	private int value = -1;
	
	public StatusPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		// init value
		value = getValue();
	}

	public StatusPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusPreference(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		// Sync the summary view
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
        	if(value == 0) {
        		summaryView.setText(R.string.status_off);
        	}else if(value == 1) {
        		summaryView.setText(R.string.status_on);
        	}else {
        		summaryView.setText(R.string.status_not_available);
        	}
        }
	}

	private void setValue(int newValue) {
		if(newValue != value) {
			value = newValue;
			persistInt(newValue);
			
			writeToInterface(String.valueOf(newValue));
			
			notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
		}
	}

	private int getValue() {
		int ret = -1;
		String str = readFromInterface();
		try {
			ret = Integer.parseInt(str);
		}catch(NumberFormatException ex) {
			
		}catch(Exception ex) {
			Log.e(LOG_TAG, "str:"+str, ex);
		}
		return ret;
	}
	
	public void reload() {
		setValue(getValue());
	}
	
	@Override
	public boolean isEnabled() {
		return (value > -1) && super.isEnabled();
	}

	@Override
	protected void onClick() {
		 super.onClick();
	        
		int newValue = -1;
		if(value == 0) {
			newValue = 1;
		}else if(value == 1) {
			newValue = 0;
		}
        if (!callChangeListener(newValue)) {
            return;
        }
        setValue(newValue);
	}

	@Override
	public boolean shouldDisableDependents() {
		return (value != 1) || super.shouldDisableDependents();
	}
}
