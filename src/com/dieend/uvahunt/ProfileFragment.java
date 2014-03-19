package com.dieend.uvahunt;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProfileFragment extends Fragment{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_profile, container, false);
		String username = savedInstanceState.getString("username");
		String uid = savedInstanceState.getString("uid");
		String name = savedInstanceState.getString("name");
		((TextView)root.findViewById(R.id.username)).setText(String.format("%s (%s - %s)", name, username, uid));
		return root;
	}
	
}
