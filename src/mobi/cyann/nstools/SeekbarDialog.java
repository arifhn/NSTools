/**
 * SeekbarDialog.java
 * Nov 22, 2011 11:58:02 AM
 */
package mobi.cyann.nstools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class SeekbarDialog extends AlertDialog implements OnSeekBarChangeListener {
	private SeekBar seekbar;
	private TextView textValue;
	
	private String key;
	private int min;
	private int max;
	private int step;
	private int value;
	private String metrics;
	
	private DialogInterface.OnClickListener okButtonListener;
	private DialogInterface.OnClickListener cancelButtonListener;
	
	public SeekbarDialog(Context context, OnClickListener ok, OnClickListener cancel) {
		super(context);
		
		okButtonListener = ok;
		cancelButtonListener = cancel;
		min = 0;
		max = 100;
		step = 1;
		value = 0;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View customView = getLayoutInflater().inflate(R.layout.seekbar_dialog, null);
		
		setIcon(0);
		setView(customView);
		setButton(BUTTON_POSITIVE, getContext().getString(R.string.label_ok), okButtonListener);
		setButton(BUTTON_NEGATIVE, getContext().getString(R.string.label_cancel), cancelButtonListener);
		
		super.onCreate(savedInstanceState);
		
		seekbar = (SeekBar)customView.findViewById(R.id.seekbar);
		seekbar.setOnSeekBarChangeListener(this);

		textValue = (TextView)customView.findViewById(R.id.textValue);
		
		resetValues();
	}
	
	private void resetValues() {
		int seekbarValue = (value - min) / step;
		int seekbarMax = (max - min) / step;
		if(seekbar != null && textValue != null) {
			seekbar.setMax(seekbarMax);
			seekbar.setProgress(seekbarValue);
			if(metrics != null) {
				textValue.setText(value + " " + metrics);
			}else {
				textValue.setText(String.valueOf(value));	
			}
		}
	}
	
	public String getMetrics() {
		return metrics;
	}

	public void setMetrics(String metrics) {
		this.metrics = metrics;
	}
	
	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
		resetValues();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
		resetValues();
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
		resetValues();
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
		resetValues();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		value = progress * step + min;
		if(metrics != null) {
			textValue.setText(value + " " + metrics);
		}else {
			textValue.setText(String.valueOf(value));	
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	
	
}
