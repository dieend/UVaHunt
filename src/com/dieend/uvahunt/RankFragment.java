package com.dieend.uvahunt;

import java.util.ArrayList;
import java.util.List;

import com.dieend.uvahunt.model.UserRank;
import com.dieend.uvahunt.view.adapter.RankListAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RankFragment extends BaseFragment {
	RankListAdapter adapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ListView v = new ListView(getActivity());
		adapter = new RankListAdapter(getActivity(), new ArrayList<UserRank>());
		v.setAdapter(adapter);
		return v;
	}

	public void updateRank(List<UserRank> ranks) {
		adapter.reset(ranks);
	}
}
