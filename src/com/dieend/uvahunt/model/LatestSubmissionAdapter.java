package com.dieend.uvahunt.model;

import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.dieend.uvahunt.R;
import com.dieend.uvahunt.callback.ProblemViewer;

public class LatestSubmissionAdapter extends BaseAdapter {

	LayoutInflater inflater;
	ProblemViewer viewer = new ProblemViewer() {
		@Override
		public void showProblem(int problemNumber, String problemTitles) {
		}
	};
	
	Map<Integer, Submission> data;
	Submission[] values;
	boolean alldata;
	int size;
	int uid;
	public LatestSubmissionAdapter(Context context, Map<Integer, Submission> objects, int uid) {
		super();
		data = objects;
		size = data.size();
		this.uid = uid;
		values = new Submission[0];
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (context instanceof ProblemViewer) {
			viewer = (ProblemViewer) context;
		}
	}
	private ClickableSpan onClickViewProblem(final int number, final String title) {
		return new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				viewer.showProblem(number, title);
			}
		};
	}
	public void setOnlyMeSubmission(boolean b) {
		if (alldata!=b) {
			alldata = b;
			notifyDataSetChanged();
		}
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		position = size - 1 - position;
		View rowView = convertView;
		if (rowView == null) {
			rowView = inflater.inflate(R.layout.listview_item_submission, null);
			ViewHolder holder = new ViewHolder();
			holder.user_background = (LinearLayout)rowView.findViewById(R.id.user_background);
			holder.submitTime = (TextView)rowView.findViewById(R.id.submit_time);
			holder.language = (TextView)rowView.findViewById(R.id.language);
			holder.verdict =(TextView)rowView.findViewById(R.id.verdict);
			holder.problem = (TextView)rowView.findViewById(R.id.problem);
			holder.problem.setMovementMethod(LinkMovementMethod.getInstance());
			holder.discuss = (TextView)rowView.findViewById(R.id.discuss);
			holder.executionTime = (TextView)rowView.findViewById(R.id.execution_time);
			holder.bestExecutionTime = (TextView)rowView.findViewById(R.id.best_execution_time);
			holder.rank = (TextView)rowView.findViewById(R.id.rank);
			holder.background = (RelativeLayout)rowView.findViewById(R.id.background);
			rowView.setTag(holder);
		}
		ViewHolder holder = (ViewHolder) rowView.getTag();
		Submission submission = values[position];
		if (submission.uid != uid && submission.uid != -1) {
			holder.user_background.setBackgroundColor(Color.GRAY);
		} else {
			holder.user_background.setBackgroundColor(Color.WHITE);
		}
		Problem problem = DBManager.$().getProblemsById(submission.getProblemId());
		
		holder.background.setBackgroundColor(Color.parseColor(Submission.verdictToColor(submission)));
		holder.submitTime.setText(Submission.getReadableTime(submission));
		holder.language.setText(Submission.getReadableLang(submission));
		holder.verdict.setText(Submission.getReadableVerdict(submission));
		SpannableString st = new SpannableString(problem.getTitle());
		st.setSpan(onClickViewProblem(problem.id, problem.title), 0, problem.title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		holder.problem.setText(st, BufferType.SPANNABLE);
		holder.executionTime.setText(String.format("%.3f", submission.runtime / 1000.0));
		holder.bestExecutionTime.setText(String.format("%.3f", problem.getBestRuntime() / 1000.0));
		holder.rank.setText("" + submission.rank);
		return rowView;
	}

	
	private static class ViewHolder {
		public TextView submitTime;
		public TextView language;
		public TextView verdict;
		public TextView problem;
		// TODO add link to forum discussion
		public TextView discuss;
		public TextView executionTime;
		public TextView bestExecutionTime;
		public TextView rank;
		public RelativeLayout background;
		public LinearLayout user_background;
	}

	
	@Override
	public void notifyDataSetChanged() {
		values = data.values().toArray(values);
		if (!alldata) {
			int j = 0;
			for (int i=0; i<values.length; i++) {
				if (values[i].uid == uid || values[i].uid == -1) {
					values[j] = values[i];
					j++;
				}
			}
			size = j;
		} else {
			size = data.size();
		}
		super.notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		return size;
	}
	@Override
	public Object getItem(int position) {
		return values[position];
	}
	@Override
	public long getItemId(int position) {
		return R.layout.listview_item_submission;
	}
}
