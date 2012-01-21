/**
 * IntegerPreference.java
 * Nov 26, 2011 9:27:54 AM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.R;
import mobi.cyann.nstools.SeekbarDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class IntegerPreference extends StatusPreference implements DialogInterface.OnClickListener {
	//private final static String LOG_TAG = "NSTools.IntegerPreference";
	
	private Context context;
	private int minValue, maxValue, step;
	private String metrics;
	
	public IntegerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_IntegerPreference, defStyle, 0);
		minValue = a.getInt(R.styleable.mobi_cyann_nstools_preference_IntegerPreference_minValue, 0);
		maxValue = a.getInt(R.styleable.mobi_cyann_nstools_preference_IntegerPreference_maxValue, 100);
		step = a.getInt(R.styleable.mobi_cyann_nstools_preference_IntegerPreference_step, 1);
		metrics = a.getString(R.styleable.mobi_cyann_nstools_preference_IntegerPreference_metrics);
		a.recycle();
		
		this.context = context;
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
		// Sync the summary view
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
        	if(value < 0) {
        		summaryView.setText(R.string.status_not_available);
        	}else if(metrics != null) {
        		summaryView.setText(value + " " + metrics);
        	}else {
        		summaryView.setText(String.valueOf(value));
        	}
        }
	}
	
	@Override
	protected void onClick() {
		SeekbarDialog dialog = new SeekbarDialog(context, this, this);
		
		dialog.setMin(minValue);
		dialog.setMax(maxValue);
		dialog.setStep(step);
		dialog.setTitle(getTitle());
		dialog.setMetrics(metrics);
		
		dialog.setValue(value);
		dialog.show();
	}
	
	@Override
	public void onClick(DialogInterface d, int which) {
		if(which == DialogInterface.BUTTON_POSITIVE) {
			int newValue = ((SeekbarDialog)d).getValue();
			if (!callChangeListener(newValue)) {
	            return;
	        }
	        writeValue(newValue, true);
		}
	}
	
	public void setMetrics(String metrics) {
		this.metrics = metrics;
	}
	
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}
	
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	
	public void setStep(int step) {
		this.step = step;
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
	}
}
