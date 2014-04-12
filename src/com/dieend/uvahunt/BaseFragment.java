package com.dieend.uvahunt;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public abstract class BaseFragment extends Fragment{
	protected static interface ViewTask {
		public void run();
	}
//	protected class WaitViewTask extends AsyncTask<ViewTask, Void, Void> {
//		ViewTask[] r;
//		@Override
//		protected Void doInBackground(ViewTask... params) {
//			r = params;
//			while (getView() == null) {
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException ex) {
//					ex.printStackTrace();
//				}
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			super.onPostExecute(result);
//			for (ViewTask run : r) {
//				run.run();
//			}
//		}
//		
//		
//	}
	protected void executeWhenViewReady(ViewTask v) {
		View view = getView();
		if (view == null) {
			taskQueue.add(v);
			//new WaitViewTask().execute();
		} else {
			v.run();
		}
	}
	Queue<ViewTask> taskQueue = new LinkedList<BaseFragment.ViewTask>();
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		while (!taskQueue.isEmpty()) {
			taskQueue.poll().run();
		}
	}
}
