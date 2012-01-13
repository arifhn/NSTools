/**
 * SeekbarDialog.java
 * Nov 22, 2011 11:58:02 AM
 */
package mobi.cyann.nstools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class SeekbarDialog extends AlertDialog implements OnSeekBarChangeListener, View.OnClickListener {
	private SeekBar seekbar;
	private TextView textValue;
	private EditText editValue;
	
	private String key;
	private int min;
	private int max;
	private int step;
	private int value;
	private String metrics;
	
	private DialogInterface.OnClickListener okButtonListener;
	private DialogInterface.OnClickListener cancelButtonListener;
	
	private InputMethodManager inputMethodManager;
	
	public SeekbarDialog(Context context, OnClickListener ok, OnClickListener cancel) {
		super(context);
		
		okButtonListener = ok;
		cancelButtonListener = cancel;
		min = 0;
		max = 100;
		step = 1;
		value = 0;
		
		inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final View customView = getLayoutInflater().inflate(R.layout.seekbar_dialog, null);
		
		setIcon(0);
		setView(customView);
		setButton(BUTTON_POSITIVE, getContext().getString(R.string.label_ok), okButtonListener);
		setButton(BUTTON_NEGATIVE, getContext().getString(R.string.label_cancel), cancelButtonListener);
		
		super.onCreate(savedInstanceState);
		
		seekbar = (SeekBar)customView.findViewById(R.id.seekbar);
		seekbar.setOnSeekBarChangeListener(this);

		editValue = (EditText)customView.findViewById(R.id.editValue);
		editValue.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				String val = editValue.getText().toString();
				if(val.length() > 0) {
					value = Integer.parseInt(val);
					if(value < min) {
						value = min;
					}else if(value > max) {
						value = max;
					}
					resetValues();
				}
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					closeEditText();
				}
				return false;
			}
		});
		
		// set onclick to text (show soft keyboard)
		textValue = (TextView)customView.findViewById(R.id.textValue);
		textValue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // open EditText
				editValue.setText(String.valueOf(value));
				editValue.setVisibility(View.VISIBLE);
				textValue.setVisibility(View.GONE);
				editValue.requestFocus();
				
				inputMethodManager.showSoftInput(editValue, InputMethodManager.SHOW_FORCED);
			}
		});

		// set on click to '-' button
		customView.findViewById(R.id.valueMin).setOnClickListener(this);
		
		// set on click to '+' button
		customView.findViewById(R.id.valuePlus).setOnClickListener(this);

		resetValues();
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.valueMin) {
			value -= step;
		}else {
			value += step;
		}
		editValue.setText(String.valueOf(value));
		resetValues();
	}
	
	private void closeEditText() {
		inputMethodManager.hideSoftInputFromWindow(editValue.getWindowToken(), 0);
		editValue.setVisibility(View.GONE);
		textValue.setVisibility(View.VISIBLE);
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

	@Override
	public void dismiss() {
		closeEditText();
		super.dismiss();
	}
	
	
}
