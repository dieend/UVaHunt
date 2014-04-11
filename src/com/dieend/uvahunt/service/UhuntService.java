package com.dieend.uvahunt.service;

import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.dieend.uvahunt.UvaHuntActivity;
import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Problem;
import com.dieend.uvahunt.service.base.AbstractService;
import com.dieend.uvahunt.tools.Utility;

public class UhuntService extends AbstractService {
	private static final String KEY_LAST_UPDATE = "last_problem_update";
	private class ProblemDetailTask implements Runnable {
		
		String url;
		public ProblemDetailTask() {
			this.url = "http://uhunt.felix-halim.net/api/p";
		}
		@Override
		public void run() {
			SharedPreferences sp = getApplicationContext().getSharedPreferences(UvaHuntActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
			long time = new Date().getTime();
			Log.d(UvaHuntActivity.TAG, "last update" + sp.getLong(KEY_LAST_UPDATE, 0L) + ", now: "+ time);
			if (time - sp.getLong(KEY_LAST_UPDATE, 0L) > (1000L * 60L * 60L * 12L)) {
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
				sp.edit().putLong(KEY_LAST_UPDATE, time).commit();
		        send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY), true);
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    	if (sp.getLong(KEY_LAST_UPDATE, 0L) != 0L) {
		    		DBManager.$().queryAllProblem();
		    		send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY), true);
		    	}
		    }
		    
		}
	}
	private class ProblemSolvedTask implements Runnable {
		String urls[] = new String[2];
		
		public ProblemSolvedTask(String uid) {
			this.urls[0] = "http://uhunt.felix-halim.net/api/solved-bits/" + uid;
			this.urls[1] = String.format("http://uhunt.felix-halim.net/api/ranklist/%s/0/0", uid);
//			this.urls[2] = String.format("http://uhunt.felix-halim.net/api/subs-user-last/%s", uid);
		}
		@Override
		public void run() {
			try {
				String results = "";
				for (int i=0;i<urls.length; i++) {
					HttpClient client = new DefaultHttpClient();
				    HttpGet request = new HttpGet(urls[i]);
				    Log.i(UvaHuntActivity.TAG, "connecting: " + urls[i]);
				    HttpResponse response;
			        response = client.execute(request);         
			        HttpEntity entity = response.getEntity();
			        if (entity != null) {
			            InputStream instream = entity.getContent();
			            if (i>0) {
			            	results = Utility.convertStreamToString(instream);
			            	Log.i(UvaHuntActivity.TAG, "result of " + urls[i] + ": " + results);
			            } else {
			            	String res = Utility.convertStreamToString(instream);
			            	Problem.populateSolvedProblem(res);
			            	Log.i(UvaHuntActivity.TAG, "result of " + urls[i] + ": " + res);
			            }
			            instream.close();
			        }
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
		new Thread(new ProblemDetailTask()).start();
	}

	@Override
	public void onRegisteredService() {
		send(Message.obtain(null, MSG_READY));
	}

	@Override
	public void onUnregisteredService() {
	}

	@Override
	public void onStopService() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onReceiveMessage(Message msg) {
		switch (msg.what) {
		case MSG_REQUEST_PROFILE:
			new Thread(new ProblemSolvedTask((String)msg.obj)).start();
			break;
		}
	}
	public static final int MSG_READY = 1;
	public static final int MSG_DETAIL_PROBLEM_READY = 2;
	public static final int MSG_PROFILE_READY = 3;
	public static final int MSG_REQUEST_PROFILE = 4;

}
