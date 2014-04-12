package com.dieend.uvahunt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ProblemViewFragment extends BaseFragment{
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
		View view = inflater.inflate(R.layout.fragment_problem_view, container, false);
		return view;
	}
	
	public void loadProblem(int number) {
		final int problemNumber = number;
		final int groupNumber = problemNumber / 100;
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				WebView view = (WebView)(getView().findViewById(R.id.webview));
				view.loadUrl(String.format("http://uva.onlinejudge.org/external/%d/%d.html", groupNumber, problemNumber));
			}
		});
		
		
	}
}
