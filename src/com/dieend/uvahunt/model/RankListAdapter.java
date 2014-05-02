package com.dieend.uvahunt.model;

import java.util.List;

import com.dieend.uvahunt.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RankListAdapter extends BaseAdapter{

	private List<UserRank> ranks;
	LayoutInflater inflater;
	public RankListAdapter(Context ctx, List<UserRank> ranks) {
		this.ranks = ranks;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		return ranks.size();
	}

	@Override
	public Object getItem(int position) {
		return ranks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			rowView = inflater.inflate(R.layout.listview_item_ranklist_user, null);
			ViewHolder tag = new ViewHolder();
			tag.textIdentity = (TextView)rowView.findViewById(R.id.text_identity);
			tag.textSolve = (TextView)rowView.findViewById(R.id.text_solve);
			tag.textRank = (TextView)rowView.findViewById(R.id.text_rank);
			tag.textSubmit = (TextView)rowView.findViewById(R.id.text_submit);
			tag.background = (RelativeLayout)rowView.findViewById(R.id.background);
			rowView.setTag(tag);
		}
		ViewHolder tag = (ViewHolder)rowView.getTag();
		UserRank rowData = ranks.get(position);
		tag.textIdentity.setText(String.format("%s - (%s)", rowData.name, rowData.username)); 
		tag.textSolve.setText(String.format("Solved: %d", rowData.ac));
		tag.textSubmit.setText(String.format("Submissions: %d", rowData.nos));
		tag.textRank.setText(String.format("%d", rowData.rank));
		if (position == 10) { // TODO change hardcoded value
			tag.background.setBackgroundColor(Color.GREEN);
		} else {
			tag.background.setBackgroundColor(Color.WHITE);
		}
		return rowView;
	}

	public void reset(List<UserRank> ranks) {
		this.ranks.clear();
		this.ranks.addAll(ranks);
		notifyDataSetChanged();
	}
	private static class ViewHolder {
		RelativeLayout background;
		TextView textIdentity;
		TextView textSolve;
		TextView textSubmit;
		TextView textRank;
	}
}
