package com.dieend.uvahunt;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;

public class ProblemViewFragment extends BaseFragment{
	public static ProblemViewFragment newInstance(int problemId) {
		ProblemViewFragment ret = new ProblemViewFragment();
		Bundle args = new Bundle();
		args.putInt("problemNumber", problemId);
		ret.setArguments(args);
		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_problem_view, container, false);
		View webview = view.findViewById(R.id.webview); 
		webview.setFocusableInTouchMode(true);
		webview.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					resetVisibility(view);
					return true;
				}
				return false;
			}
		});
		view.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchProblem(v);
			}
		});
		return view;
	}
	
	@Override
	public boolean onBackPressed() {
//		if (getView() != null) {
//			if (getView().findViewById(R.id.webview).getVisibility() == View.VISIBLE) {
//				resetVisibility(getView());
//				return true;
//			}
//		}
		return false;
	}

	public void loadProblem(int number) {
		final int problemNumber = number;
		final int groupNumber = problemNumber / 100;
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				WebView view = (WebView)(getView().findViewById(R.id.webview));
				view.setVisibility(View.VISIBLE);
				view.requestFocus();
				getView().findViewById(R.id.problem_number).setVisibility(View.GONE);
				getView().findViewById(R.id.search_button).setVisibility(View.GONE);
				view.loadUrl(String.format("http://uva.onlinejudge.org/external/%d/%d.html", groupNumber, problemNumber));
			}
		});	
	}
	private void searchProblem(View v) {
		if (getView() != null) {
			EditText et = (EditText)getView().findViewById(R.id.problem_number);
			loadProblem(Integer.parseInt(et.getText().toString()));
		} else {
			Log.w(UvaHuntActivity.TAG, "trying to search problem, but getView returns null");
		}
	}
	private void resetVisibility(View parentView) {
		parentView.findViewById(R.id.webview).setVisibility(View.GONE);
		parentView.findViewById(R.id.problem_number).setVisibility(View.VISIBLE);
		parentView.findViewById(R.id.search_button).setVisibility(View.VISIBLE);
	}
}
