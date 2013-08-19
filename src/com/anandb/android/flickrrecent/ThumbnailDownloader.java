package com.anandb.android.flickrrecent;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ThumbnailDownloader<Token> extends HandlerThread {
	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD = 0;
	
	Handler mHandler;
	Map <Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
	Handler mResponseHandler;
	Listner<Token> mListner;
	ImageCache mImageCache;
	
	public interface Listner<Token> {
		void onThumbnailDownloaded(Token token, Bitmap image);
	}
	
	public ThumbnailDownloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
		mImageCache = new ImageCache();
	}
	
	public void setListener(Listner<Token> listener) {
		mListner = listener;
	}
	
	public void queueThumbnail(Token token, String url) {
		requestMap.put(token, url);
		mHandler.obtainMessage(MESSAGE_DOWNLOAD, token)
				.sendToTarget();
	}
	
	public void clearQueue () {
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		requestMap.clear();
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_DOWNLOAD) {
					@SuppressWarnings("unchecked")
					Token token = (Token)msg.obj;
					Log.d(TAG, "Got a request for URL" + requestMap.get(token));
					handleRequest(token);
				}
			}
		};
	}
	
	private void handleRequest(final Token token) {
		try {
			final String url = requestMap.get(token);
			
			if (url == null)
				return;
			
			Bitmap bitmap = mImageCache.get(url);
			
			if (bitmap == null) {
				Log.d(TAG, "Downloading thumbnail for url " + url);
				byte[] imageBytes = new FlickrFetcher().getUrlBytes(url);
				bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
				mImageCache.put(url, bitmap);
			}
			
			final Bitmap image = bitmap;
			
			mResponseHandler.post(new Runnable() {
				
				@Override
				public void run() {
					if (requestMap.get(token) != url)
						return;
					
					requestMap.remove(token);
					mListner.onThumbnailDownloaded(token, image);
				}
			});
			
		} catch (IOException e) {
			Log.e(TAG, "Error downloading image", e);
		}
	}
	
	private class ImageCache extends LruCache<String, Bitmap> {
		public ImageCache() {
			super(200);
		}		
	}
}
