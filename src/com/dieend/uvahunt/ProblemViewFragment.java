package com.dieend.uvahunt;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Problem;

public class ProblemViewFragment extends BaseFragment{

	boolean isSearchable = true;
	public void setSearchable(boolean b) {
		isSearchable = b;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_problem_view, container, false);
		final WebView webview = (WebView)view.findViewById(R.id.webview);
		webview.setFocusableInTouchMode(true); 
		webview.getSettings().setAppCachePath(getActivity().getCacheDir().getAbsolutePath()); 
		webview.getSettings().setAppCacheEnabled(true); 
		webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		final ProgressBar progress = (ProgressBar)view.findViewById(R.id.progress_bar);
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url,
					Bitmap favicon) {
				view.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
				super.onPageStarted(view, url, favicon);
			}
			@Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progress.setVisibility(View.GONE);
				view.setVisibility(View.VISIBLE);
            }
        });
		webview.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && isSearchable) {
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
		view.findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
				webview.reload();
				webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
			}
		});
		return view;
	}

	public void loadProblem(int number) {
		final int problemNumber = number;
		final int groupNumber = problemNumber / 100;
		final Problem p = DBManager.$().getProblemsByNum(number);
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				WebView view = (WebView)(getView().findViewById(R.id.webview));
				view.requestFocus();
				view.loadUrl(String.format("http://uva.onlinejudge.org/external/%d/%d.html", groupNumber, problemNumber));
				getView().findViewById(R.id.webview_container).setVisibility(View.VISIBLE);
				getView().findViewById(R.id.problem_number).setVisibility(View.GONE);
				getView().findViewById(R.id.search_button).setVisibility(View.GONE);
				((TextView)(getView().findViewById(R.id.text_ac))).setText("AC: " + p.getNumOfAccepted());
				((TextView)(getView().findViewById(R.id.text_ce))).setText("CE: " + p.getNumOfCompileError());
				((TextView)(getView().findViewById(R.id.text_ml))).setText("ML: " + p.getNumOfMemoryLimitError());
				((TextView)(getView().findViewById(R.id.text_pe))).setText("PE: " + p.getNumOfPresentationError());
				((TextView)(getView().findViewById(R.id.text_re))).setText("RE: " + p.getNumOfRuntimeError());
				((TextView)(getView().findViewById(R.id.text_tl))).setText("TL: " + p.getNumOfTimeLimitError());
				((TextView)(getView().findViewById(R.id.text_wa))).setText("WA: " + p.getNumOfWrongAnswer());
				((TextView)(getView().findViewById(R.id.text_dacu))).setText("DACU: " + p.getDacu());
				((TextView)(getView().findViewById(R.id.text_level))).setText("Level: " + p.getLevel());
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
		parentView.findViewById(R.id.webview_container).setVisibility(View.GONE);
		parentView.findViewById(R.id.problem_number).setVisibility(View.VISIBLE);
		parentView.findViewById(R.id.search_button).setVisibility(View.VISIBLE);
	}
}
