package com.dieend.uvahunt.service;

import java.util.Map;

import org.json.JSONException;

import android.os.Handler;
import android.os.Message;

import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.model.User;

public class UhuntServiceHandler extends Handler {
		UhuntServiceDelegate delegate;
    	private String userdata;
        
    	public UhuntServiceHandler(UhuntServiceDelegate main) {
    		delegate = main;
    	}
    	private boolean submissionReady = false;
    	private boolean problemReady = false;
		@Override
		public void handleMessage(Message msg) {
				switch (msg.what) {
				case UhuntService.MSG_PROFILE_READY:
					userdata = (String)msg.obj;
					try {
						delegate.profileReady(new User(userdata));
					} catch (JSONException ex) {
						ex.printStackTrace();
					}
					break;
				case UhuntService.MSG_DETAIL_PROBLEM_READY:
					problemReady = true;
					if (submissionReady) delegate.submissionReady();
					break;
				case UhuntService.MSG_READY:
					delegate.serviceReady((Boolean)msg.obj);
					break;
				case UhuntService.MSG_DETAIL_SUBMISSION_READY:
					submissionReady = true;
					if (problemReady) delegate.submissionReady();
					break;
				case UhuntService.MSG_NEW_EVENT:
					Map<Integer, Submission> data = (Map<Integer, Submission>) msg.obj;
					delegate.submissionArrival(data);
					break;
				}
			super.handleMessage(msg);
		}
}
