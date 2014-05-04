package com.dieend.uvahunt.view.adapter;

import com.dieend.uvahunt.R;
import com.dieend.uvahunt.model.DBManager;
import com.dieend.uvahunt.model.Problem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CPProblemAdapter extends ArrayAdapter<Integer>{
	int layoutResource;
	public CPProblemAdapter(Context context, int resource) {
		super(context, resource);
		layoutResource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(layoutResource, parent, false);
			ViewHolder tag = new ViewHolder();
			tag.important = (ImageView) rowView.findViewById(R.id.important);
			tag.status = (ImageView) rowView.findViewById(R.id.status);
			tag.problem_name = (TextView) rowView.findViewById(R.id.problem_name);
			rowView.setTag(tag);
		}
		Integer problemNumber = getItem(position);
		Problem p = DBManager.$().getProblemsByNum(Math.abs(problemNumber));
		ViewHolder tag = (ViewHolder)rowView.getTag(); 
		tag.problem_name.setText(p.getTitle());
		if (p.isTried()) {
			tag.status.setVisibility(View.VISIBLE);
			if (p.isSolved()) {
				tag.status.setImageResource(R.drawable.ic_checkmark);
			} else {
				tag.status.setImageResource(R.drawable.ic_cross);
			}
		} else {
			tag.status.setVisibility(View.INVISIBLE);
		}
		if (problemNumber < 0) {
			tag.important.setVisibility(View.VISIBLE);
		} else {
			tag.important.setVisibility(View.INVISIBLE);
		}
		return rowView;
	}

	private static class ViewHolder {
		ImageView important;
		ImageView status;
		TextView problem_name;
	}
	
}
