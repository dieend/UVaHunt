package com.dieend.uvahunt.service;

import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
			if (time - sp.getLong(KEY_LAST_UPDATE, 0L) > (1000L * 60L * 60L * 12L)) {
				sp.edit().putLong(KEY_LAST_UPDATE, time);
				Log.i(UvaHuntActivity.TAG, "updating problem database");
			} else {
				send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY));
				return;
			}
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
		        problemDetail = result;
		        send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY));
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    }
		    
		}
	}
	private class ProblemSolvedTask implements Runnable {
		String urls[] = new String[3];
		String uid;
		public ProblemSolvedTask(String uid) {
			this.uid = uid;
			this.urls[0] = "http://uhunt.felix-halim.net/api/solved-bits/" + this.uid;
			this.urls[1] = String.format("http://uhunt.felix-halim.net/api/ranklist/%s/0/0", uid);
			this.urls[2] = String.format("http://uhunt.felix-halim.net/api/subs-user-last/%s", uid);
		}
		@Override
		public void run() {
			try {
				String[] results = new String[2];
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
			            	results[i-1] = Utility.convertStreamToString(instream);
			            	Log.i(UvaHuntActivity.TAG, "result of " + urls[i] + ": " + results[i-1]);
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
	private class ProblemToDatabaseTask implements Runnable {
		@Override
		public void run() {
			if (problemDetail != null) {
				DBManager.$().populateProblem(problemDetail);
			} else {
				DBManager.$().queryAllProblem();
			}
			send(Message.obtain(null, MSG_PROBLEM_DB_READY));
		}
	}
	@Override
	public void onStartService() {
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
		case MSG_REQUEST_POPULATE_PROBLEM_DB:
			new Thread(new ProblemToDatabaseTask()).start();
			break;
		}
	}
	public static final int MSG_READY = 1;
	public static final int MSG_DETAIL_PROBLEM_READY = 2;
	public static final int MSG_PROFILE_READY = 3;
	public static final int MSG_REQUEST_PROFILE = 4;
	public static final int MSG_REQUEST_POPULATE_PROBLEM_DB = 5;
	public static final int MSG_PROBLEM_DB_READY = 6;
	private String problemDetail;

}
