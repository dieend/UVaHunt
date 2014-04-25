package com.dieend.uvahunt;

import android.app.Activity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.dieend.uvahunt.callback.ProblemViewer;
import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Problem;
import com.dieend.uvahunt.model.User;

public class ProfileFragment extends BaseFragment{

	@Override
	public void onDestroyView() {
		Log.i(UvaHuntActivity.TAG, "Destroying View");
		super.onDestroyView();
	}

	private ClickableSpan onClickViewProblem(final int number, final String title) {
		return new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				Log.d(UvaHuntActivity.TAG, "clicking span text	");
				viewer.showProblem(number, title);
			}
		};
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_profile, container, false);
		return root;
	}

	public void updateProfile(final User user) {
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				View root = getView();
				String username = user.getUsername();
				int uid = user.getUid();
				String name = user.getName();
				((TextView)root.findViewById(R.id.username)).setText(String.format("%s (%s - %d)", name, username, uid));
				((TextView)root.findViewById(R.id.num_solved)).setText(String.format("%d", user.getACAll()));
				((TextView)root.findViewById(R.id.num_submission)).setText(String.format("%d", user.getNOS()));
			}
		});
	}
	
	public void updateSubmission() {
		executeWhenViewReady(new ViewTask() {
			@Override
			public void run() {
				View root = getView(); 
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
					position += number.length();
				}
			
				((TextView)root.findViewById(R.id.solved_problems)).setMovementMethod(LinkMovementMethod.getInstance());
				((TextView)root.findViewById(R.id.solved_problems)).setText(sb, BufferType.SPANNABLE);
				
		
				first = true;
				position = 0;
				SpannableStringBuilder sb2 = new SpannableStringBuilder();
				int count = 0;
				for (Integer i: Problem.triedProblems()) {
					if (!first) {
						sb2.append(' ');
						position++;
					}
					count++;
					first = false;
					Problem problem = DBManager.$().getProblemsById(i);
					String number = "" + problem.getNumber();
					String probTitle = problem.getTitle();
					sb2.append(number);
					sb2.setSpan(onClickViewProblem(problem.getNumber(), probTitle), position, position + number.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					position += number.length();
				}
				((TextView)root.findViewById(R.id.failed_problems)).setMovementMethod(LinkMovementMethod.getInstance());
				((TextView)root.findViewById(R.id.failed_problems)).setText(sb2, BufferType.SPANNABLE);
				((TextView)root.findViewById(R.id.fail_solve_num)).setText(String.valueOf(count));
			}
		});
		
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
		Log.i(UvaHuntActivity.TAG, "Detaching from activity");
		viewer = null;
	}

	
}
