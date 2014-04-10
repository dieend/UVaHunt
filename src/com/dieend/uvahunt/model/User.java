package com.dieend.uvahunt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable{

	private static final long serialVersionUID = 1L;
	public User(String json) throws JSONException {
		JSONArray arr = new JSONArray(json);
		JSONObject obj = arr.getJSONObject(0);
		rank = obj.getInt("rank");
		uid = obj.getInt("userid");
		name = obj.getString("name");
		username = obj.getString("username");
		ACAll = obj.getInt("ac");
		NOS = obj.getInt("nos");
		JSONArray activity = obj.getJSONArray("activity");
		AC2d = activity.getInt(0);
		AC7d = activity.getInt(1);
		AC31d = activity.getInt(2);
		AC3M = activity.getInt(3);
		AC1Y = activity.getInt(4);
		submissions = new ArrayList<Submission>();
	}
	public int getRank() {
		return rank;
	}
	public int getUid() {
		return uid;
	}
	public String getName() {
		return name;
	}
	public String getUsername() {
		return username;
	}
	public int getACAll() {
		return ACAll;
	}
	public int getNOS() {
		return NOS;
	}
	public int getAC2d() {
		return AC2d;
	}
	public int getAC7d() {
		return AC7d;
	}
	public int getAC31d() {
		return AC31d;
	}
	public int getAC3M() {
		return AC3M;
	}
	public int getAC1Y() {
		return AC1Y;
	}
	public void addSubmissions(Submission submission) {
		submissions.add(submission);
	}
	public List<Submission> getSubmissions() {
		return Collections.unmodifiableList(submissions);
	}
	private int rank;
	private int uid;
	private String name;
	private String username;
	private int ACAll;
	private int NOS;
	private int AC2d;
	private int AC7d;
	private int AC31d;
	private int AC3M;
	private int AC1Y;
	private List<Submission> submissions;
}
