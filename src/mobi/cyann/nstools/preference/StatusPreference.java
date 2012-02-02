/**
 * StatusPreference.java
 * Nov 24, 2011 9:00:58 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.PreloadValues;
import mobi.cyann.nstools.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class StatusPreference extends BasePreference<Integer> {
	private final static String LOG_TAG = "NSTools.StatusPreference";
	protected int value = -1;
	
	public StatusPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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

	@Override
	protected void writeValue(Integer newValue, boolean writeInterface) {
		if(writeInterface && value > -1 && newValue != value) {
			writeToInterface(String.valueOf(newValue));
			// re-read from interface (to detect error)
			newValue = readValue();
		}
		if(newValue != value) {
			value = newValue;
			persistInt(newValue);
			
			notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
		}
	}

	@Override
	protected Integer readValue() {
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
	
	@Override
	protected Integer readPreloadValue() {
		int ret = PreloadValues.getInstance().getInt(getKey());
		if(ret == -2) { // if the value was not found in preload, we try to read it from interface
			return readValue();
		}else {
			return ret;
		}
	}
	
	@Override
	public boolean isEnabled() {
		return (value > -1) && super.isEnabled();
	}

	@Override
	protected void onClick() {
		int newValue = -1;
		if(value == 0) {
			newValue = 1;
		}else if(value == 1) {
			newValue = 0;
		}
        if (!callChangeListener(newValue)) {
            return;
        }
        writeValue(newValue, true);
	}
	
	@Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return  null;
    }
    
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	if(restoreValue) {
    		value = getPersistedInt(-1);
    	}
		writeValue(readPreloadValue(), false);
    }
    
	@Override
	public boolean shouldDisableDependents() {
		return (value != 1) || super.shouldDisableDependents();
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public boolean isAvailable() {
		return value > -1;
	}
}
