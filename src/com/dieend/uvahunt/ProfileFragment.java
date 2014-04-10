package com.dieend.uvahunt;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Problem;
import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.model.User;

public class ProfileFragment extends Fragment{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Integer i : Problem.solvedProblems()) {
			if (!first) {
				sb.append(' ');
			}
			first = false;
			sb.append(DBManager.$().getProblemsById(i).getNumber());
		}
		((TextView)root.findViewById(R.id.solved_problems)).setText(sb.toString());
		first = true;
		for (Submission s: user.getSubmissions()) {			
			if (!Problem.isSolved(s.getProblemId())) {
				if (!first) {
					sb.append(' ');
				}
				first = false;
				sb.append(DBManager.$().getProblemsById(s.getProblemId()).getNumber());
			}
		}
		
		return root;
	}
	
}
