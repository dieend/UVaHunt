package com.dieend.uvahunt;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dieend.uvahunt.tools.Utility;

import android.R.color;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CPFragment extends BaseFragment {

	ListView listChapter;
	ArrayAdapter<String> chapterAdapter;
	ListView listSubChapter;
	ArrayAdapter<String> subChapterAdapter;
	ListView listSubSubChapter;
	ArrayAdapter<String> subSubChapterAdapter;
	int selectedEdition = 2;
	int selectedChapter = -1;
	int selectedSubChapter = -1; 
	Button[] buttons = new Button[3];
	JSONArray[] cp = new JSONArray[3];
	
	public static CPFragment newInstance(Context ctx){
		CPFragment ret = new CPFragment();
		try {
			InputStream is = ctx.getAssets().open("cp1.json");
			ret.cp[0] = new JSONArray(Utility.convertStreamToString(is));
			is.close();
			
			is = ctx.getAssets().open("cp2.json");
			ret.cp[1] = new JSONArray(Utility.convertStreamToString(is));
			is.close();
			
			is = ctx.getAssets().open("cp3.json");
			ret.cp[2] = new JSONArray(Utility.convertStreamToString(is));
			is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_cp, container, false);
		chapterAdapter = new ArrayAdapter<String>(getActivity(), R.layout.listview_item_cp, R.id.text1);
		subChapterAdapter = new ArrayAdapter<String>(getActivity(), R.layout.listview_item_cp, R.id.text1);
		subSubChapterAdapter = new ArrayAdapter<String>(getActivity(), R.layout.listview_item_cp, R.id.text1);
		
		listChapter = (ListView) v.findViewById(R.id.list_chapter);
		listChapter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listChapter.setAdapter(chapterAdapter);
		listChapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				try {
					if (selectedChapter != position) {
						listChapter.setItemChecked(position, true);
						selectedChapter = position;
						subChapterAdapter.clear();
						subSubChapterAdapter.clear();
						JSONArray arr = cp[selectedEdition].getJSONObject(position).getJSONArray("arr");
						subChapterAdapter.setNotifyOnChange(false);
						for (int i=0; i<arr.length(); i++) {
							subChapterAdapter.add(arr.getJSONObject(i).getString("title"));
						}
						subChapterAdapter.notifyDataSetChanged();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		listSubChapter = (ListView) v.findViewById(R.id.list_subchapter);
		listSubChapter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listSubChapter.setAdapter(subChapterAdapter);
		listSubChapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				try {
					if (selectedSubChapter != position) {
						listSubChapter.setItemChecked(position, true);
						selectedSubChapter = position;
						subSubChapterAdapter.clear();
						JSONArray arr = cp[selectedEdition].getJSONObject(selectedChapter).getJSONArray("arr").getJSONObject(position).getJSONArray("arr");
						subSubChapterAdapter.setNotifyOnChange(false);
						for (int i=0; i<arr.length(); i++) {
							subSubChapterAdapter.add(arr.getJSONArray(i).getString(0));
						}
						subSubChapterAdapter.notifyDataSetChanged();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
		listSubSubChapter = (ListView) v.findViewById(R.id.list_subsubchapter);
		listSubSubChapter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listSubSubChapter.setAdapter(subSubChapterAdapter);
		buttons[0] = (Button) v.findViewById(R.id.button_cp1);
		buttons[0].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedEdition = 0;
				updateState();
			}
		});
		buttons[1] = (Button) v.findViewById(R.id.button_cp2);
		buttons[1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedEdition = 1;
				updateState();
			}
		});
		buttons[2] = (Button) v.findViewById(R.id.button_cp3);
		buttons[2].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedEdition = 2;
				updateState();
			}
		});
		
		updateState();
		return v;
	}
	private void updateState() {
		try {
			chapterAdapter.clear();
			subChapterAdapter.clear();
			subSubChapterAdapter.clear();
			chapterAdapter.setNotifyOnChange(false);
			
			for (int i=0; i<cp[selectedEdition].length(); i++) {
				JSONObject obj = cp[selectedEdition].getJSONObject(i);
				chapterAdapter.add(obj.getString("title"));
			}
			chapterAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (int i=0; i<3; i++) {
			if (selectedEdition == i) {
				buttons[i].setBackgroundResource(color.holo_blue_dark);
			} else {
				buttons[i].setBackgroundResource(0);
			}
		}
	}

}
