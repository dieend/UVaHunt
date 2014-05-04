package com.dieend.uvahunt;

import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.dieend.uvahunt.callback.LiveUpdaterHandler;
import com.dieend.uvahunt.callback.ProblemViewer;
import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.view.adapter.LatestSubmissionAdapter;

public class LatestSubmissionFragment extends BaseFragment{

	Map<Integer, Submission> data = new TreeMap<Integer, Submission>();
	LatestSubmissionAdapter adapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_latest_submission, container, false);
		ListView lv = (ListView) v.findViewById(R.id.submission_container);
		lv.setAdapter(adapter);

		ToggleButton button = (ToggleButton) v.findViewById(R.id.live_submission_button);
		button.setOnClickListener(toggleButtonListener);
		((ToggleButton) v.findViewById(R.id.submission_user_dependency)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					adapter.setOnlyMeSubmission(true);
				} else {
					adapter.setOnlyMeSubmission(false);
				}
			}
		});
		return v;
	}
	public void setToggleState(final boolean status) {
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				((ToggleButton) getView().findViewById(R.id.live_submission_button)).setChecked(status);
			}
		});
	}
	public void updateSubmission(final Map<Integer, Submission> submissions) {
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				if (submissions.size() != 0) {
					for (Submission s: submissions.values()) {
						data.put(s.getId(), s);
					}
					adapter.notifyDataSetChanged();
				}
			}
		});

	}

	ProblemViewer viewer;
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		viewer = (ProblemViewer) activity;
		adapter = new LatestSubmissionAdapter(activity, data, ((UvaHuntActivity)activity).uid);
		toggleButtonListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToggleButton tv = (ToggleButton)v;
				if (!tv.isChecked()) {
					((LiveUpdaterHandler) activity).disableLiveUpdate();
				} else {
					((LiveUpdaterHandler) activity).enableLiveUpdate();
				}
			}
		};
		data.clear();
	}
	@Override
	public void onDetach() {
		super.onDetach();
		adapter.notifyDataSetInvalidated();
		adapter = null;
		viewer = null;
	}
	private OnClickListener toggleButtonListener;
}
