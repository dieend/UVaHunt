package com.dieend.uvahunt.service.base;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class AbstractService extends Service {
    static final int MSG_REGISTER_CLIENT = 9991;
    static final int MSG_UNREGISTER_CLIENT = 9992;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    final Messenger mMessenger = new Messenger(new MessageHandler(this)); // Target we publish for clients to send messages to IncomingHandler.
    private static class MessageHandler extends Handler { // Handler of incoming messages from clients.
    	AbstractService service;
    	MessageHandler(AbstractService service){
    		this.service = service;
    	}
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
            	Log.i("MyService", "Client registered: "+msg.replyTo);
                service.mClients.add(msg.replyTo);
                service.onRegisteredService();
                break;
            case MSG_UNREGISTER_CLIENT:
            	Log.i("MyService", "Client un-registered: "+msg.replyTo);
                service.mClients.remove(msg.replyTo);
                service.onUnregisteredService();
                break;            
            default:
                //super.handleMessage(msg);
            	service.onReceiveMessage(msg);
            }
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        onStartService();
        
        Log.i("MyService", "Service Started.");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService", "Received start id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        onStopService();
        
        Log.i("MyService", "Service Stopped.");
    }    
    static String MsgString(Message msg) {
    	return String.format(Locale.getDefault(), "{what=%d,obj=%s}", msg.what, msg);
    }
    protected void send(Message msg) {
   	 for (int i=mClients.size()-1; i>=0; i--) {
            try {
            	Log.i("MyService", "Sending message to clients: "+ MsgString(msg));
               mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
            	Log.e("MyService", "Client is dead. Removing from list: "+i);
            	mClients.remove(i);
            }
        }    	
   }
   
    protected Boolean registered = false;
    public abstract void onStartService();
    public abstract void onRegisteredService();
    public abstract void onUnregisteredService();
    public abstract void onStopService();
    public abstract void onReceiveMessage(Message msg);

}