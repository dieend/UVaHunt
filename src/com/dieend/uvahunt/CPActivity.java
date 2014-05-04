package com.dieend.uvahunt;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.ListView;

import com.dieend.uvahunt.view.adapter.CPProblemAdapter;

public class CPActivity extends FragmentActivity{

	int edition;
	int chapter;
	int subchapter;
	int subsubchapter;
	JSONArray detail;
	ProblemViewFragment fragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Intent intent = getIntent();
			edition = intent.getIntExtra("edition", -1);
			chapter = intent.getIntExtra("chapter", -1);
			subchapter = intent.getIntExtra("sub_chapter", -1);
			subsubchapter = intent.getIntExtra("sub_sub_chapter", -1);
			detail = new JSONArray(intent.getStringExtra("detail"));
			CPProblemAdapter adapter = new CPProblemAdapter(this, R.layout.listview_item_cp_sub_sub_chapter);
			for (int i=1; i<detail.length(); i++) {
				adapter.add(detail.getInt(i));
			}
			setContentView(R.layout.activity_cp);
			setTitle(detail.getString(0));
			ListView detailView = (ListView)findViewById(R.id.detail_sub_sub_chapter);
			fragment = new ProblemViewFragment();
			fragment.setSearchable(false);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, (Fragment)fragment).commit();
			findViewById(R.id.container).setVisibility(View.INVISIBLE);
			detailView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			detailView.setAdapter(adapter);
			detailView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					((Checkable)view).setChecked(true);
					findViewById(R.id.container).setVisibility(View.VISIBLE);
					int number = (Integer) parent.getItemAtPosition(position);
					number = Math.abs(number);
					fragment.loadProblem(number);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
