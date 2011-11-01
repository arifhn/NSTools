package mobi.cyann.nstools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author arif
 *
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, OnBootCompleteService.class));
	}
	
}
