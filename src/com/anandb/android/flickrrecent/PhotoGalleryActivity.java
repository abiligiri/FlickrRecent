package com.anandb.android.flickrrecent;

import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return new PhotoGalleryFragment();
	}
}
