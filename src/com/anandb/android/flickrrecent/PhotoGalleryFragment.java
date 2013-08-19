package com.anandb.android.flickrrecent;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.anandb.android.flickrrecent.FlickrFetcher.FlickrPhoto;

public class PhotoGalleryFragment extends Fragment {
	private GridView mPhotoGrid;
	private ArrayList<FlickrPhoto> mItems;
	private int offset = 0;
	private ThumbnailDownloader<ImageView> mThumbnailDownloader;
	private FlickrPhotoAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		mItems = new ArrayList<FlickrFetcher.FlickrPhoto>();
		mAdapter = new FlickrPhotoAdapter(mItems);
		new FetchItemsTask().execute(offset);
		
		mThumbnailDownloader = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailDownloader.setListener(new ThumbnailDownloader.Listner<ImageView>() {
			@Override
			public void onThumbnailDownloaded(ImageView imageView, Bitmap image) {
				if (isVisible()) {
					imageView.setImageBitmap(image);
				}
				
			}
		});
		mThumbnailDownloader.start();
		mThumbnailDownloader.getLooper();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailDownloader.quit();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_gallery, container, false);
		
		mPhotoGrid = (GridView)v.findViewById(R.id.photo_grid);
		mPhotoGrid.setAdapter(mAdapter);
		return v;
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		mThumbnailDownloader.clearQueue();
	}
	
	private void setupAdapter() {
		if (mItems == null || getActivity() == null)
			return;
		mAdapter.notifyDataSetChanged();		
	}
	
	private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<FlickrPhoto>> {
		@Override
		protected ArrayList<FlickrPhoto> doInBackground(Integer... params) {
			Integer offset = params[0];
			return new FlickrFetcher().fetchItems(offset.intValue());
		}
		
		@Override
		protected void onPostExecute(ArrayList<FlickrPhoto> result) {
			if (mItems == null) {
				mItems = result;
			} else {
				mItems.addAll(result);
			}
			
			setupAdapter();
		}
	}
	

	private class FlickrPhotoAdapter extends ArrayAdapter<FlickrPhoto> {
		public FlickrPhotoAdapter(ArrayList<FlickrPhoto> items) {
			super(getActivity(), android.R.layout.simple_gallery_item, items);
		}
		
		@Override
		public FlickrPhoto getItem(int position) {
			if (position == (mItems.size() - 1)) {
				offset += mItems.size();
				new FetchItemsTask().execute(offset);
			}
			return super.getItem(position);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				ImageView imageView = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
				convertView = imageView;
			}
			
			((ImageView)convertView).setImageResource(android.R.drawable.gallery_thumb);
			FlickrPhoto photo = getItem(position);
			mThumbnailDownloader.queueThumbnail((ImageView)convertView, photo.getUrl());
			return convertView;
		}
	}
}
