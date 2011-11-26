/**
 * IntegerPreference.java
 * Nov 26, 2011 9:27:54 AM
 */
package mobi.cyann.nstools.preference;

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
public class IntegerPreference extends BasePreference {
	private final static String LOG_TAG = "NSTools.IntegerPreference";
	
	protected int value = -1;
	
	public IntegerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public IntegerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IntegerPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		Log.d(LOG_TAG, "onBindView");
		// Sync the summary view
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
        	summaryView.setText(String.valueOf(value));
        }
	}
	
	protected void setValue(int newValue) {
		if(newValue != value) {
			value = newValue;
			persistInt(newValue);
			
			writeToInterface(String.valueOf(newValue));
			
			notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
		}
	}

	protected int getValue() {
		int ret = -1;
		String str = readFromInterface();
		try {
			ret = Integer.parseInt(str);
		}catch(NullPointerException ex) {
			
		}catch(Exception ex) {
			Log.e(LOG_TAG, "str:"+str, ex);
		}
		return ret;
	}
	
	public void reload() {
		setValue(getValue());
	}
	
	@Override
	protected void onClick() {
		super.onClick();
	        
		// create dialog
	}
	
	@Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
		Object ret = getValue();
		Log.d(LOG_TAG, "defaultValue=" + ret);
		return ret;
    }
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		
		Log.d(LOG_TAG, "onSetInitialValue");
		if(restorePersistedValue) {
			// TODO: remove try-catch block
			// currently we need this for upgrading from old nstools version
			try {
				value = getPersistedInt(-1);
			}catch(Exception ex) {
				value = Integer.parseInt(getPersistedString("-1"));
			}
		}
		setValue(getValue());
	}
}
