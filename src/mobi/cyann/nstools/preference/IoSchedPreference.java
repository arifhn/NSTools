/**
 * IoSchedPreference.java
 * Nov 27, 2011 10:03:52 AM
 */
package mobi.cyann.nstools.preference;

import java.util.Arrays;

import mobi.cyann.nstools.PreloadValues;
import mobi.cyann.nstools.R;
import mobi.cyann.nstools.SysCommand;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class IoSchedPreference extends BasePreference<String> implements DialogInterface.OnClickListener {
	private final static String LOG_TAG = "NSTools.IOSchedPreference";
	private String value;
	private String schedValues[];
	private CharSequence writeToInterfaces[];
	
	public IoSchedPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_IoSchedPreference, defStyle, 0);
		writeToInterfaces = a.getTextArray(R.styleable.mobi_cyann_nstools_preference_IoSchedPreference_writeToInterface);
		a.recycle();
	}

	public IoSchedPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IoSchedPreference(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		// Sync the summary view
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
        	if(value == null) {
        		summaryView.setText(R.string.status_not_available);
        	}else {
        		summaryView.setText(value);
        	}
        }
	}

	@Override
	protected void writeToInterface(String value) {
		// echo to interfacePath
		if(writeToInterfaces != null) {
			SysCommand sc = SysCommand.getInstance();
			for(CharSequence i: writeToInterfaces) {
				if(sc.writeSysfs(i.toString(), value) >= 0) {
					Log.d(LOG_TAG, "WOK");
				}else {
					Log.e(LOG_TAG, "WER:" + sc.getLastError(0));
				}
			}
		}
	}

	@Override
	protected void writeValue(String newValue, boolean writeInterface) {
		if(newValue == null) {
			return;
		}
		if(writeInterface && value != null && !newValue.equals(value)) {
			writeToInterface(newValue);
			newValue = readValue();
		}
		if(!newValue.equals(value)) {
			value = newValue;
			persistString(newValue);
			
			notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
		}
	}

	@Override
	protected String readValue() {
		return parseValue(readFromInterface());
	}
	
	@Override
	protected String readPreloadValue() {
		String read = PreloadValues.getInstance().getString(getKey());
		if(read == null) { // if the value was not found in preload, we try to read it from interface
			return readValue();
		}else {
			return parseValue(read);
		}
	}
	
	private String parseValue(String tmp) {
		String ret = null;
		if(tmp != null && !tmp.equals("-1")) {
			schedValues = tmp.split(" ");
			int idx = 0;
			for(int i = 0; i < schedValues.length; ++i) {
				if(schedValues[i].startsWith("[")) {
					schedValues[i] = schedValues[i].substring(1, schedValues[i].length() - 1);
					idx = i;
					break;
				}
			}
			ret = schedValues[idx];
			Arrays.sort(schedValues);
		}
		return ret;
	}
	
	@Override
	public boolean isEnabled() {
		return (value != null) && super.isEnabled();
	}

	@Override
	protected void onClick() {
		// show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(schedValues, Arrays.binarySearch(schedValues, value), this);
        builder.setTitle(getTitle());
        builder.setNegativeButton(R.string.label_cancel, this);
        builder.create().show();
	}
	
	@Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return  null;
    }
    
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	if(restoreValue) {
    		value = getPersistedString(null);
    	}
		writeValue(readPreloadValue(), false);
    }
    
	@Override
	public boolean shouldDisableDependents() {
		return (value == null) || super.shouldDisableDependents();
	}
	
	@Override
	public void onClick(DialogInterface d, int which) {
		if(which != DialogInterface.BUTTON_NEGATIVE) {			
			String newValue = schedValues[which];
			if (!callChangeListener(newValue)) {
	            return;
	        }
	        writeValue(newValue, true);
		}
		d.dismiss();
	}
	
	@Override
	public boolean isAvailable() {
		return value != null;
	}
}
