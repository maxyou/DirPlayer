package com.maxproj.android.dirplayer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}

class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	final static String DTAG = "DirPlayer";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    
    /**
     * 注意有时没必要监听
     * 	1、设置会自动保存
     * 	2、可以在切换回主activity时产生影响
     * 	3、可以在settingActivity结束时统一产生影响
     */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getString(R.string.setting1_key)))
        {
            // Set summary to be the user-description for the selected value
            Preference prefShowActionBar = findPreference(key);
            boolean b = sharedPreferences.getBoolean(key, false);
			if(b == true){ //true: 显示顶部标题条
            	prefShowActionBar.setSummary(getString(R.string.seting1_summ));
            	Log.d(DTAG,"setting: seting1_summ");
            }else{
            	prefShowActionBar.setSummary(getString(R.string.seting1_summ2));
            	Log.d(DTAG,"setting: seting1_summ2");
            }
            
        }
	}

}
