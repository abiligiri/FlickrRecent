package com.anandb.android.flickrrecent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;

public class FlickrFetcher {
	private static final String API_KEY = "3ded9007c40780aaf3844ae011f659f2";
	private static final String SERVER = "http://query.yahooapis.com/v1/public/yql";
	private static final String TAG = "FlickrFetcher";
	
	public byte[] getUrlBytes(String urlSpec) throws IOException {
		URL url =  new URL(urlSpec);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			InputStream input = connection.getInputStream();
		
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;
			
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = input.read(buffer)) > 0) {
				output.write(buffer, 0, bytesRead);
			}
		
			output.close();
			return output.toByteArray();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			connection.disconnect();
		}
		
		return null;
	}
	
	private String getUrl(String url) throws IOException {
		return new String(getUrlBytes(url));
	}
	
	private String getQuery(int page, int nItems) {
		StringBuilder sb = new StringBuilder("select * from flickr.photos.recent(");
		sb.append(page);
		sb.append(",");
		sb.append(nItems);
		sb.append(") where api_key='3ded9007c40780aaf3844ae011f659f2'");
		
		return sb.toString();
	}
	
	public ArrayList<FlickrPhoto> fetchItems(int offset) {
		String query = getQuery(offset, 100);
		String url = Uri.parse(SERVER).buildUpon()
				.appendQueryParameter("q", query)
				.appendQueryParameter("format", "json").build().toString();
		Log.d(TAG, "fetching for query " + query);
		try {
			String response = getUrl(url);
			JSONObject jsonResponse = new JSONObject(response);
			JSONObject results = jsonResponse.getJSONObject("query").getJSONObject("results");
			JSONArray photos = results.getJSONArray("photo");
			
			ArrayList<FlickrPhoto> flickrPhotos = new ArrayList<FlickrPhoto>();
			for (int i = 0; i < photos.length(); i++) {
				flickrPhotos.add(new FlickrPhoto(photos.getJSONObject(i)));
			}
			
			return flickrPhotos;
		} catch (Exception e) {
			
		}
		return null;
	}
	
	public class FlickrPhoto {
		private static final String ID_KEY = "id", OWNER_KEY = "owner", SECRET_KEY = "secret", SERVER_KEY = "server", TITLE_KEY = "title",
				FARM_KEY="farm";
		private String mID, mOwner, mSecret, mServer, mTitle, mFarm;
				
		public FlickrPhoto(JSONObject json) throws JSONException {
			mID = json.getString(ID_KEY);
			mOwner = json.getString(OWNER_KEY);
			mSecret = json.getString(SECRET_KEY);
			mServer = json.getString(SERVER_KEY);
			mTitle = json.getString(TITLE_KEY);
			mFarm = json.getString(FARM_KEY);
		}
		
		public String toString() {
			return mTitle;
		}
		
		public String getUrl() {
			// http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg
			return "http://farm" + mFarm + ".staticflickr.com/" + mServer + "/" + mID + "_" + mSecret + "_s.jpg";
		}
	}
}
