package com.dieend.uvahunt.service;

import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.dieend.uvahunt.UvaHuntActivity;
import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.service.base.AbstractService;
import com.dieend.uvahunt.tools.Utility;

public class UhuntService extends AbstractService {
	private static final String KEY_PROB_LAST_UPDATE = "last_problem_update";
	private static final String KEY_SUB_LAST_UPDATE = "last_sub_update";
	public static final String KEY_PROB_SYNC_FREQ = "problem_sync_frequency";
	public static final Long DEFAULT_PROB_SYNC_FREQ = 1000L * 60L * 60L * 12L;
	public static final String KEY_SUB_SYNC_FREQ = "submission_sync_frequency";
	public static final Long DEFAULT_SUB_SYNC_FREQ = 1000L * 60L * 60L * 12L;

	private class PrepareProblemDetailTask implements Runnable {		
		String url;
		public PrepareProblemDetailTask() {
			this.url = "http://uhunt.felix-halim.net/api/p";
		}
		@Override
		public void run() {
			SharedPreferences sp = getApplicationContext().getSharedPreferences(UvaHuntActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
			long time = new Date().getTime();
			
			if (time - sp.getLong(KEY_PROB_LAST_UPDATE, 0L) > (sp.getLong(KEY_PROB_SYNC_FREQ, DEFAULT_PROB_SYNC_FREQ))) {
				Log.i(UvaHuntActivity.TAG, "updating problem database");
			} else {
				if (DBManager.$().queryAllProblem()) {
					send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY), true);
					return;
				} else {
					Log.i(UvaHuntActivity.TAG, "updating problem database");
				}
			}
			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
			Log.i(UvaHuntActivity.TAG, "connecting: " + url);
		    HttpGet request = new HttpGet(url);
		    HttpResponse response;
		    String result = null;
		    try {
		        response = client.execute(request);         
		        HttpEntity entity = response.getEntity();

		        if (entity != null) {
		            InputStream instream = entity.getContent();
		            result = Utility.convertStreamToString(instream);
		            instream.close();
		        }
	        	
				DBManager.$().populateProblem(result);
				sp.edit().putLong(KEY_PROB_LAST_UPDATE, time).commit();
		        send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY), true);
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    	if (sp.getLong(KEY_PROB_LAST_UPDATE, 0L) != 0L) {
		    		DBManager.$().queryAllProblem();
		    		send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY), true);
		    	}
		    	// TODO: retry download problem
		    }
		    
		}
	}
	private class PrepareSubmissionTask implements Runnable {
		String url;
		private boolean updateAll = false;
		public PrepareSubmissionTask(int uid) {
			this.url = String.format("http://uhunt.felix-halim.net/api/subs-user/%d/", uid);
		}
		@Override
		public void run() {
			SharedPreferences sp = getApplicationContext().getSharedPreferences(UvaHuntActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
			long time = new Date().getTime();
			if (time - sp.getLong(KEY_SUB_LAST_UPDATE, 0L) > (sp.getLong(KEY_SUB_SYNC_FREQ, DEFAULT_SUB_SYNC_FREQ))) {
				Log.i(UvaHuntActivity.TAG, "updating all user submission database");
				updateAll = true;
			}
			int lastId = 0;
			Submission lastSubmission = DBManager.$().getLastSubmission();
			if (lastSubmission == null) {
				updateAll = true;
			}
			if (!updateAll) {
				lastId = lastSubmission.getId();
			}
			url += lastId;
			HttpClient client = new DefaultHttpClient();
//			HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
			Log.i(UvaHuntActivity.TAG, "connecting: " + url);
		    HttpGet request = new HttpGet(url);
		    HttpResponse response;
		    String result = null;
			try {
		        response = client.execute(request);         
		        HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream instream = entity.getContent();
		            result = Utility.convertStreamToString(instream);
		            instream.close();
		        }
	        	
				DBManager.$().populateSubmission(result);
				sp.edit().putLong(KEY_SUB_LAST_UPDATE, time).commit();
		        send(Message.obtain(null, MSG_DETAIL_SUBMISSION_READY));
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    	if (sp.getLong(KEY_SUB_LAST_UPDATE, 0L) != 0L) {
		    		DBManager.$().queryAllProblem();
		    		send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY));
		    	}
		    }	
		}
	}
	private class LiveSubmissionTask implements Runnable {
		long eventId;
		public LiveSubmissionTask(int eventId) {
			this.eventId = eventId;
		}
		@Override
		public void run() {
			String url = "http://uhunt.felix-halim.net/api/poll/"+eventId;
			HttpClient client = new DefaultHttpClient();
			Log.i(UvaHuntActivity.TAG, "connecting: " + url);
		    HttpGet request = new HttpGet(url);
		    HttpResponse response;
		    String result = null;
			try {
		        response = client.execute(request);         
		        HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream instream = entity.getContent();
		            result = Utility.convertStreamToString(instream);
		            instream.close();
		        }
		        JSONArray arr = new JSONArray(result);
		        send(Message.obtain(null, MSG_NEW_EVENT, DBManager.$().updateSubmissionFromLiveSubmission(arr, uid)));
		        eventId = arr.getJSONObject(arr.length()-1).getLong("id");
		        if (liveUpdate) {
		        	new Thread(this).start();
		        }
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    }	
		}
	}
	private class PrepareProfileTask implements Runnable {
		String url;
		
		public PrepareProfileTask(int uid) {
			this.url = String.format("http://uhunt.felix-halim.net/api/ranklist/%d/0/0", uid);
		}
		@Override
		public void run() {
			try {
				String results = "";
				HttpClient client = new DefaultHttpClient();
			    HttpGet request = new HttpGet(url);
			    Log.i(UvaHuntActivity.TAG, "connecting: " + url);
			    HttpResponse response;
		        response = client.execute(request);         
		        HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream instream = entity.getContent();
		            	results = Utility.convertStreamToString(instream);
		            	Log.i(UvaHuntActivity.TAG, "result of " + url + ": " + results);
		            
		            instream.close();
		        }
		        send(Message.obtain(null, MSG_PROFILE_READY, results));
			} catch (Exception e1) {
		    	e1.printStackTrace();
		    }
		}
	}
	
	@Override
	public void onStartService() {
		DBManager.prepare(getApplicationContext());
		new Thread(new PrepareProblemDetailTask()).start();
	}

	@Override
	public void onRegisteredService() {
		send(Message.obtain(null, MSG_READY, liveUpdate));
	}

	@Override
	public void onUnregisteredService() {
	}

	@Override
	public void onStopService() {
		DBManager.close();
	}

	@Override
	public void onReceiveMessage(Message msg) {
		switch (msg.what) {
		case MSG_REQUEST_PROFILE:
			uid = msg.arg1;
			new Thread(new PrepareSubmissionTask(uid)).start();
			new Thread(new PrepareProfileTask(uid)).start();
			break;
		case MSG_ENABLE_LIVE_UPDATER:
			liveUpdate = true;
			new Thread(new LiveSubmissionTask(0)).start();
			break;
		case MSG_DISABLE_LIVE_UPDATER:
			liveUpdate = false;
			break;
		}
	}
	public static final int MSG_READY = 1;
	public static final int MSG_DETAIL_PROBLEM_READY = 2;
	public static final int MSG_PROFILE_READY = 3;
	public static final int MSG_REQUEST_PROFILE = 4;
	public static final int MSG_DETAIL_SUBMISSION_READY = 5;
	public static final int MSG_NEW_EVENT = 6;
	public static final int MSG_ENABLE_LIVE_UPDATER = 7;
	public static final int MSG_DISABLE_LIVE_UPDATER = 8;
	private int uid;
	private boolean liveUpdate = false;
}
