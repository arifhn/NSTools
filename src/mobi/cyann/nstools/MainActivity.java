package mobi.cyann.nstools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import mobi.cyann.nstools.services.ObserverService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends FragmentActivity {
	private final static String LOG_TAG = "NSTools.MainActivity";
	
	private boolean onCreate = false;
	private TabHost tabHost;
	private TabsAdapter tabsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// start our ObserverService
		ObserverService.startService(this, false);
		
		// extract our scripts 
		extractScripts();
		
		// reload tweak
		reloadTweak();
		// flag oncreate
		onCreate = true;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main);
		
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
		tabHost.setup();
		
		ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
		
		tabsAdapter = new TabsAdapter(this, tabHost, viewPager);
		
		Resources res = getResources();
		
		TabSpec tab1 = tabHost.newTabSpec("nstweak");
		tab1.setIndicator(getString(R.string.ns_tweak), res.getDrawable(R.drawable.ic_tab_tweaks));
		//tab1.setContent(new Intent(this, NSTweakFragment.class));
		tabsAdapter.addTab(tab1, NSTweakFragment.class, null);
		
		TabSpec tab2 = tabHost.newTabSpec("cpu");
		tab2.setIndicator(getString(R.string.label_cpu_tweak), res.getDrawable(R.drawable.ic_tab_cpu));
		//tab2.setContent(new Intent(this, CPUFragment.class));
		tabsAdapter.addTab(tab2, CPUFragment.class, null);
		
		TabSpec tab3 = tabHost.newTabSpec("volt");
		tab3.setIndicator(getString(R.string.voltage_control), res.getDrawable(R.drawable.ic_tab_voltages));
		//tab3.setContent(new Intent(this, VoltageControlFragment.class));
		tabsAdapter.addTab(tab3, VoltageControlFragment.class, null);
		
		TabSpec tab4 = tabHost.newTabSpec("setting");
		tab4.setIndicator(getString(R.string.label_setting), res.getDrawable(R.drawable.ic_tab_settings));
		//tab4.setContent(new Intent(this, SettingFragment.class));
		tabsAdapter.addTab(tab4, SettingFragment.class, null);
		
		if (savedInstanceState != null) {
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", tabHost.getCurrentTabTag());
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
		if(sc.readSysfs("/proc/version") > 0) {
			String kernel = sc.getLastResult(0);
			
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			Editor ed = pref.edit();
			ed.putString(getString(R.string.key_kernel_version), kernel);
			ed.commit();
		}
		
		Intent i = new Intent();
		i.setAction("mobi.cyann.nstools.RELOAD");
		sendBroadcast(i);
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
	
	public static void restart(Context c) {
		Intent i = new Intent(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(i);
	}

	@Override
	protected void onDestroy() {
		Log.d(LOG_TAG, "onDestroy");
		SettingsManager.saveToInitd(this);
		super.onDestroy();
	}
}
