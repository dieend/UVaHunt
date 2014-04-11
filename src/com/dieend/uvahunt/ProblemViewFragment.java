package com.dieend.uvahunt;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ProblemViewFragment extends Fragment{
	public static ProblemViewFragment newInstance(int problemId) {
		ProblemViewFragment ret = new ProblemViewFragment();
		Bundle args = new Bundle();
		args.putInt("problemNumber", problemId);
		ret.setArguments(args);
		return ret;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		WebView view = (WebView) inflater.inflate(R.layout.fragment_problem_view, container, false);
		if (getArguments() != null) {
			int problemNumber = getArguments().getInt("problemNumber");
			int groupNumber = problemNumber % 100;
			view.loadUrl(String.format("http://uva.onlinejudge.org/external/%d/%d.html", groupNumber, problemNumber));
		}
		return view;
	}
}
