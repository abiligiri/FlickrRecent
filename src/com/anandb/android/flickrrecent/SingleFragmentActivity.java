package com.anandb.android.flickrrecent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends FragmentActivity {
	protected abstract Fragment createFragment();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);
		
		FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentById(R.id.fragmentContainer);
		
		if (f == null) {
			f = createFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, f).commit();
		}
	}
}
