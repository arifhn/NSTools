package mobi.cyann.nstools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {
	private final static String LOG_TAG = "NSTools.MainActivity";
	
	private boolean onCreate = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// extract our scripts 
		extractScripts();
		
		// reload tweak
		reloadTweak();
		// flag oncreate
		onCreate = true;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main);
		
		TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
		Resources res = getResources();

		TabSpec tab1 = tabHost.newTabSpec("tid1");
		tab1.setIndicator(getString(R.string.ns_tweak), res.getDrawable(R.drawable.ic_tab_tweaks));
		tab1.setContent(new Intent(this, NSTweakActivity.class));
		tabHost.addTab(tab1);
		
		TabSpec tab2 = tabHost.newTabSpec("tid2");
		tab2.setIndicator(getString(R.string.label_cpu_tweak), res.getDrawable(R.drawable.ic_tab_cpu));
		tab2.setContent(new Intent(this, CPUActivity.class));
		tabHost.addTab(tab2);
		
		TabSpec tab3 = tabHost.newTabSpec("tid3");
		tab3.setIndicator(getString(R.string.voltage_control), res.getDrawable(R.drawable.ic_tab_voltages));
		tab3.setContent(new Intent(this, VoltageControlActivity.class));
		tabHost.addTab(tab3);
		
		TabSpec tab4 = tabHost.newTabSpec("tid4");
		tab4.setIndicator(getString(R.string.label_setting), res.getDrawable(R.drawable.ic_tab_settings));
		tab4.setContent(new Intent(this, SettingActivity.class));
		tabHost.addTab(tab4);
	}

    @Override
	protected void onResume() {
		super.onResume();
		if(!onCreate) {
			reloadTweak();
		}
		onCreate = false;
	}

    private void reloadTweak() {
		SysCommand sc = SysCommand.getInstance();
		
		// execute our preload script to get values from sys interface
		sc.suRun(getString(R.string.PRELOAD_SCRIPT));
		
		PreloadValues.getInstance().reload();
		
		// save kernel version
		if(sc.suRun("cat", "/proc/version") > 0) {
			String kernel = sc.getLastResult(0);
			
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			Editor ed = pref.edit();
			ed.putString(getString(R.string.key_kernel_version), kernel);
			ed.commit();
		}
    }
    
	private void copyAsset(String srcPath, String dstPath) throws IOException {
		AssetManager assetManager = getApplicationContext().getAssets();
    	InputStream is = assetManager.open(srcPath);
    	FileOutputStream fos = new FileOutputStream(dstPath, false);
    	byte[] buffer = new byte[100];
    	int n = -1;
    	do {
    		n = is.read(buffer);
    		if(n != -1) {
    			fos.write(buffer, 0, n);
    		}
    	}while(n != -1);
    	fos.flush();
    	fos.close();
    	is.close();
    }
    
	private void extractScripts() {
		String scriptDir = getString(R.string.SCRIPT_DIR);
		String scriptVersion = getString(R.string.SCRIPT_VERSION);
		
		String scriptVersionTagFile = scriptDir + scriptVersion;
		// first check script version (in the future we can change SCRIPT_VERSION constant to overwrite existing scripts)
		if(!new File(scriptVersionTagFile).exists()) {
			try {
				SysCommand sc = SysCommand.getInstance();
				// clean old script dir
				int r = sc.run("rm", "-r", scriptDir);
				if(r < 0) {
					Log.e(LOG_TAG, sc.getLastError(0));
				}
				// create script dir
				new File(scriptDir).mkdir();
				// copy all scripts
				String[] scripts = getResources().getStringArray(R.array.scripts);
				for(String f: scripts) {
					copyAsset(f, scriptDir + f);
					r = SysCommand.getInstance().run("chmod", "0755", scriptDir + f);
					if(r < 0) {
						Log.e(LOG_TAG, sc.getLastError(0));
					}
				}
				// mark script version
				FileWriter fw = new FileWriter(scriptVersionTagFile);
				fw.write(scriptVersion);
				fw.close();

				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				pref.edit().clear().commit();
			}catch(IOException e) {
				Log.e(LOG_TAG, "failed to extract scripts", e);
			}
		}
	}
}
