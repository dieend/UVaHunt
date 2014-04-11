package com.dieend.uvahunt.service;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

public class UhuntServiceHandler extends Handler {
		UhuntServiceDelegate delegate;
    	private String userdata;
    	private boolean problemReady = false;
    	private boolean profileReady = false;
    	private String uid;
        
    	public UhuntServiceHandler(UhuntServiceDelegate main, String uid) {
    		delegate = main;
    		this.uid = uid;
    	}
    	
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case UhuntService.MSG_PROFILE_READY:
					profileReady = true;
					userdata = (String)msg.obj;
					populateProblem();
					break;
				case UhuntService.MSG_DETAIL_PROBLEM_READY:
					problemReady = true;
					populateProblem();
					break;
				case UhuntService.MSG_READY:
					delegate.getServiceManager().send(Message.obtain(null, UhuntService.MSG_REQUEST_PROFILE, uid)); 
					break;
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
			super.handleMessage(msg);
		}
    	private void populateProblem() throws RemoteException{
    		if (problemReady && profileReady) {
    			delegate.uhuntReady(userdata);
			}
    	}
}
