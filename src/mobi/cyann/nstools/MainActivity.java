package mobi.cyann.nstools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {
	private final static String LOG_TAG = "NSTools.MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// call extract 
		extractScripts();
		
		setContentView(R.layout.main);
		
		TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
		
		TabSpec tab1 = tabHost.newTabSpec("tid1");
		tab1.setIndicator(getString(R.string.ns_tweak));
		tab1.setContent(new Intent(this, NSTweakActivity.class));
		tabHost.addTab(tab1);
		//tabHost.addTab(tab2);
		//tabHost.addTab(tab3);
	}

    private void copyAsset(String srcPath, String dstPath) throws IOException {
		AssetManager assetManager = getApplicationContext().getAssets();
    	InputStream is = assetManager.open(srcPath);
    	FileOutputStream fos = new FileOutputStream(dstPath);
    	byte[] buffer = new byte[100];
    	int n = -1;
    	do {
    		n = is.read(buffer);
    		if(n != -1) {
    			fos.write(buffer);
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
				// clean old script dir
				SysCommand.getInstance().run("rm", "-r", scriptDir);
				// create script dir
				new File(scriptDir).mkdir();
				// copy all scripts
				String[] scripts = getResources().getStringArray(R.array.scripts);
				for(String f: scripts) {
					copyAsset(f, scriptDir + f);
				}
				SysCommand.getInstance().run("chmod", "+x", scriptDir + "*");
				// mark script version
				FileWriter fw = new FileWriter(scriptVersionTagFile);
				fw.write(scriptVersion);
				fw.close();
			}catch(IOException e) {
				Log.e(LOG_TAG, "failed to extract scripts", e);
			}
		}
	}
}
