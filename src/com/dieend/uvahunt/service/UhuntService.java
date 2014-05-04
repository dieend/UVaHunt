package com.dieend.uvahunt.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.dieend.uvahunt.UvaHuntActivity;
import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.model.UserRank;
import com.dieend.uvahunt.service.base.AbstractService;
import com.dieend.uvahunt.tools.Utility;

public class UhuntService extends AbstractService {
	private static final String KEY_PROB_LAST_UPDATE = "last_problem_update";
	private static final String KEY_SUB_LAST_UPDATE = "last_sub_update";
	public static final String KEY_PROB_SYNC_FREQ = "problem_sync_frequency";
	public static final Long DEFAULT_PROB_SYNC_FREQ = 1000L * 60L * 60L * 12L;
	public static final String KEY_SUB_SYNC_FREQ = "submission_sync_frequency";
	public static final Long DEFAULT_SUB_SYNC_FREQ = 1000L * 60L * 60L * 12L;
	boolean problemReady = false;

	private class PrepareProblemDetailTask implements Runnable {		
		String url;
		public PrepareProblemDetailTask() {
			this.url = "http://uhunt.felix-halim.net/api/p";
		}
		@Override
		public void run() {
			SharedPreferences sp = getApplicationContext().getSharedPreferences(UvaHuntActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
			long time = new Date().getTime();
			
			if (time - sp.getLong(KEY_PROB_LAST_UPDATE, 0L) > (sp.getLong(KEY_PROB_SYNC_FREQ, DEFAULT_PROB_SYNC_FREQ)) 
					|| !DBManager.$().queryAllProblem()) {
				Log.i(UvaHuntActivity.TAG, "updating problem database");
			} else {
				send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY));
				return;
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
				problemReady = true;
		        send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY));
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    	if (sp.getLong(KEY_PROB_LAST_UPDATE, 0L) != 0L) {
		    		if (DBManager.$().queryAllProblem()) {
		    			send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY));
		    		} else {
		    			send(Message.obtain(null, MSG_FAILED, "Unstable Connection"));
		    		}
		    	}
		    	// TODO: retry download problem
		    }
		    
		}
	}
	private class PrepareSubmissionTask implements Runnable {
		String url;
		private boolean updateAll = false;
		public PrepareSubmissionTask() {
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
			Submission lastSubmission = DBManager.$().getLastSubmission(uid);
			if (lastSubmission == null) {
				updateAll = true;
			}
			if (!updateAll) {
				lastId = lastSubmission.getId();
				Log.i(UvaHuntActivity.TAG, "updating submission database after "+lastId);
			}
			url += lastId;
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

				DBManager.$().populateSubmission(result, uid);

				sp.edit().putLong(KEY_SUB_LAST_UPDATE, time).commit();
		        send(Message.obtain(null, MSG_DETAIL_SUBMISSION_READY));
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    	if (sp.getLong(KEY_SUB_LAST_UPDATE, 0L) != 0L) {
		    		DBManager.$().queryAllSubmission(-1);
		    		send(Message.obtain(null, MSG_DETAIL_SUBMISSION_READY));
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
		        if (arr.length() > 0) {
			        send(Message.obtain(null, MSG_NEW_EVENT, DBManager.$().updateSubmissionFromLiveSubmission(arr, uid)));
			        eventId = arr.getJSONObject(arr.length()-1).getLong("id");
		        }
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
		
		public PrepareProfileTask() {
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
				// TODO i18n
				e1.printStackTrace();
		    }
		}
	}
	
	private class ResetSubmissionTask implements Runnable {
		@Override
		public void run() {
			liveUpdate = false;
			uid = -1;
			DBManager.$().resetSubmission();
		}		
	}
	
	private class RankListTask implements Runnable {
		private String url = String.format("http://uhunt.felix-halim.net/api/ranklist/%d/10/10", uid);
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
		        JSONArray arr = new JSONArray(results);
		        List<UserRank> ranks = new ArrayList<UserRank>();
		        for (int i=0; i<arr.length(); i++) {
		        	JSONObject obj = arr.getJSONObject(i);
		        	ranks.add(new UserRank(obj.getInt("rank"), 
		        					obj.getString("name"), 
		        					obj.getString("username"), 
		        					obj.getInt("ac"), 
		        					obj.getInt("nos")));
		        }
		        send(Message.obtain(null, MSG_RANK_READY, ranks));
			} catch (Exception e1) {
				// TODO i18n
				e1.printStackTrace();
		    }
		}
		
	}
	
	@Override
	public void onStartService() {
		DBManager.prepare(getApplicationContext());
	}

	@Override
	public void onRegisteredService() {
		new Thread(new PrepareProblemDetailTask()).start();
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
			new Thread(new PrepareSubmissionTask()).start();
			new Thread(new PrepareProfileTask()).start();
			break;
		case MSG_ENABLE_LIVE_UPDATER:
			liveUpdate = true;
			new Thread(new LiveSubmissionTask(0)).start();
			break;
		case MSG_DISABLE_LIVE_UPDATER:
			liveUpdate = false;
			break;
		case MSG_RESET_SUBMISSION:
			new Thread(new ResetSubmissionTask()).start();
			break;
		case MSG_REQUEST_RANK:
			new Thread(new RankListTask()).start();
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
	public static final int MSG_RESET_SUBMISSION = 9;
	public static final int MSG_FAILED = 10;
	public static final int MSG_REQUEST_RANK = 11;
	public static final int MSG_RANK_READY = 12;
	private int uid;
	private boolean liveUpdate = false;
}
