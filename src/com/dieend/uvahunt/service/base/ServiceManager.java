package com.dieend.uvahunt.service.base;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

public class ServiceManager {
	private Class<? extends AbstractService> mServiceClass;
	private Context mActivity;
    private boolean mIsBound;
    private Messenger mService = null;
    private Handler mIncomingHandler = null;
    private IncomingHandler defaultIncomingHandler = new IncomingHandler(this);
    private final Messenger mMessenger = new Messenger(defaultIncomingHandler);
    
    private static class IncomingHandler extends Handler {
    	ServiceManager manager;
    	SparseArray<Handler> customHandler = new SparseArray<Handler>();
    	public IncomingHandler(ServiceManager manager) {
			this.manager = manager;
		}
        @Override
        public void handleMessage(Message msg) {
        	Handler h = customHandler.get(msg.what, null);
        	if (h!= null) {
        		Log.i("ServiceHandler", "Incoming message. Passing to custom handler: "+ AbstractService.MsgString(msg));
        		h.handleMessage(msg);
        		customHandler.remove(msg.what);
        		h = null;
        	} else if (manager.mIncomingHandler != null) {
        		Log.i("ServiceHandler", "Incoming message. Passing to handler: "+ AbstractService.MsgString(msg));
        		manager.mIncomingHandler.handleMessage(msg);
        	}
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //textStatus.setText("Attached.");
            Log.i("ServiceHandler", "Attached.");
            try {
                Message msg = Message.obtain(null, AbstractService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            //textStatus.setText("Disconnected.");
            Log.i("ServiceHandler", "Disconnected.");
        }
    };
    
    public ServiceManager(Context context, Class<? extends AbstractService> serviceClass, Handler incomingHandler) {
    	this.mActivity = context;
    	this.mServiceClass = serviceClass;
    	this.mIncomingHandler = incomingHandler;
    	
    	if (isRunning()) {
    		doBindService();
    	}
    }

    public void start() {
    	doStartService();
    	doBindService();
    }
    
    public void stop() {
    	doUnbindService();
    	doStopService();    	
    }
    
    /**
     * Use with caution (only in Activity.onDestroy())! 
     */
    public void unbind() {
    	doUnbindService();
    }
    
    public boolean isRunning() {
    	ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
	    
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (mServiceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    
	    return false;
    }
    
    public void send(Message msg) throws RemoteException {
    	if (mIsBound) {
            if (mService != null) {
            	mService.send(msg);
            }
    	}
    }

    public void send(Message msg, Handler h) throws RemoteException {
    	if (mIsBound) {
            if (mService != null) {
            	defaultIncomingHandler.customHandler.append(msg.what, h);
            	mService.send(msg);
            }
    	}
    }
    private void doStartService() {
    	mActivity.startService(new Intent(mActivity, mServiceClass));    	
    }
    
    private void doStopService() {
    	mActivity.stopService(new Intent(mActivity, mServiceClass));
    }
    
    private void doBindService() {
    	mActivity.bindService(new Intent(mActivity, mServiceClass), mConnection, Context.BIND_AUTO_CREATE);
    	mIsBound = true;
    }
    
    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, AbstractService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            
            // Detach our existing connection.
            mActivity.unbindService(mConnection);
            mIsBound = false;
            //textStatus.setText("Unbinding.");
            Log.i("ServiceHandler", "Unbinding.");
        }
    }
}
