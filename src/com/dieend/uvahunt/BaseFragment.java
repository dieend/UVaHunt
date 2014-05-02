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
