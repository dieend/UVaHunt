package com.dieend.uvahunt;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dieend.uvahunt.callback.ProblemViewer;
import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Problem;
import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.model.User;

public class ProfileFragment extends Fragment{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	private ClickableSpan onClickViewProblem(int number, String title) {
		final int value = number;
		final String problemTitle = title;
		return new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				viewer.showProblem(value, problemTitle);
			}
		};
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_profile, container, false);
		User user = (User) getArguments().getSerializable("user");
		String username = user.getUsername();
		int uid = user.getUid();
		String name = user.getName();
		((TextView)root.findViewById(R.id.username)).setText(String.format("%s (%s - %d)", name, username, uid));
		((TextView)root.findViewById(R.id.num_solved)).setText(String.format("%d", user.getACAll()));
		((TextView)root.findViewById(R.id.num_submission)).setText(String.format("%d", user.getNOS()));
		SpannableStringBuilder sb = new SpannableStringBuilder();
		boolean first = true;
		int position = 0;
		for (Integer i : Problem.solvedProblems()) {
			if (!first) {
				sb.append(' ');
				position++;
			}
			first = false;
			Problem problem = DBManager.$().getProblemsById(i);
			String number = "" + problem.getNumber();
			String probTitle = problem.getTitle();
			sb.append(number);
			sb.setSpan(onClickViewProblem(problem.getNumber(), probTitle), position, position + number.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		((TextView)root.findViewById(R.id.solved_problems)).setText(sb);
		

		first = true;
		position = 0;
		sb.clear();
		sb.clearSpans();
		int count = 0;
		for (Submission s: user.getSubmissions()) {			
			if (!Problem.isSolved(s.getProblemId())) {
				if (!first) {
					sb.append(' ');
					position++;
				}
				count++;
				first = false;
				Problem problem = DBManager.$().getProblemsById(s.getProblemId());
				String number = "" + problem.getNumber();
				String probTitle = problem.getTitle();
				sb.append(number);
				sb.setSpan(onClickViewProblem(problem.getNumber(), probTitle), position, position + number.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		((TextView)root.findViewById(R.id.failed_problems)).setText(sb);
		((TextView)root.findViewById(R.id.fail_solve_num)).setText(count);
		return root;
	}

	ProblemViewer viewer;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		viewer = (ProblemViewer) activity;
	}
	@Override
	public void onDetach() {
		super.onDetach();
		viewer = null;
	}
	
}
