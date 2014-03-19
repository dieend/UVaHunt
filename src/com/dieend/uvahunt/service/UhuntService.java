package com.dieend.uvahunt.service;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Message;

import com.dieend.uvahunt.model.Problem;
import com.dieend.uvahunt.service.base.AbstractService;
import com.dieend.uvahunt.tools.Utility;

public class UhuntService extends AbstractService {
 
	private class ProblemDetailTask implements Runnable {
		String url;
		public ProblemDetailTask() {
			this.url = "http://uhunt.felix-halim.net/api/p";
		}
		@Override
		public void run() {
			HttpClient client = new DefaultHttpClient();
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
		        send(Message.obtain(null, MSG_DETAIL_PROBLEM_READY, result));
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    }
		    
		}
	}
	private class ProblemSolvedTask implements Runnable {
		String urls[] = new String[2];
		String uid;
		public ProblemSolvedTask(String uid) {
			this.uid = uid;
			this.urls[0] = "http://uhunt.felix-halim.net/api/solved-bits/" + this.uid;
			this.urls[1] = String.format("http://uhunt.felix-halim.net/api/ranklist/%s/0/0", uid);
		}
		@Override
		public void run() {
			try {
				String[] results = new String[2];
				for (int i=0;i<urls.length; i++) {
					HttpClient client = new DefaultHttpClient();
				    HttpGet request = new HttpGet(urls[i]);
				    HttpResponse response;
			        response = client.execute(request);         
			        HttpEntity entity = response.getEntity();
			        if (entity != null) {
			            InputStream instream = entity.getContent();
			            results[i] = Utility.convertStreamToString(instream);
			            instream.close();
			        }
			    }
				Problem.populateSolvedProblem(results[0]);
		        send(Message.obtain(null, MSG_PROFILE_READY, results[1]));
			} catch (Exception e1) {
		    	e1.printStackTrace();
		    }
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
		}
	}
	public static final int MSG_READY = 1;
	public static final int MSG_DETAIL_PROBLEM_READY = 2;
	public static final int MSG_PROFILE_READY = 3;
	public static final int MSG_REQUEST_PROFILE = 4;

}
