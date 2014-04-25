package com.dieend.uvahunt.model;

import java.io.Serializable;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;

import android.text.format.DateUtils;

public class Submission implements Serializable, Comparable<Submission>{
	private static final long serialVersionUID = 1L;
	int id;
	int uid;
	int problemId;
	int verdict;
	int runtime;
	long submitTime;
	int lang;
	int rank;
	public Submission(JSONArray arr, int uid) throws JSONException {
		this(arr.getInt(0), uid, arr.getInt(1), arr.getInt(2), arr.getInt(3), 
				arr.getInt(4), arr.getInt(5), arr.getInt(6));
	}
	public Submission(int id, int uid, int problemId, int verdict, int runtime,
			long submitTime, int lang, int rank) {
		super();
		this.id = id;
		this.uid = uid;
		this.problemId = problemId;
		this.verdict = verdict;
		this.runtime = runtime;
		this.submitTime = submitTime;
		this.lang = lang;
		this.rank = rank;
	}
	public int getId() {
		return id;
	}
	public int getUid() {
		return uid;
	}
	public int getProblemId() {
		return problemId;
	}
	public int getVerdict() {
		return verdict;
	}
	public String getVerdictReadable() {
		return getReadableVerdict(this);
	}
	public int getRuntime() {
		return runtime;
	}
	public long getSubmitTime() {
		return submitTime;
	}
	
	public int getLang() {
		return lang;
	}
	public String getLangReadable() {
		return getReadableLang(this);
	}
	public int getRank() {
		return rank;
	}
	public boolean isAccepted() {
		return verdict == 90;
	}
	public static String getReadableVerdict(Submission s) {
		int verdictId = s.getVerdict();
		switch (verdictId) {
		case 10 : return "Submission error";
		case 15 : return "Can't be judged";
		case 0	: // 0 also in queue
		case 20 : return "In queue";
		case 30 : return "Compile error";
		case 35 : return "Restricted function";
		case 40 : return "Runtime error";
		case 45 : return "Output limit";
		case 50 : return "Time limit";
		case 60 : return "Memory limit";
		case 70 : return "Wrong answer";
		case 80 : return "PresentationE";
		case 90 : return "Accepted";
		default: return String.valueOf(verdictId);
		}
	}
	public static String getReadableTime(Submission s) {
		return (String) DateUtils.getRelativeTimeSpanString(System.currentTimeMillis(),
				s.submitTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
	}
	public static String verdictToColor(Submission s) {
		int verdict = s.getVerdict();
		switch (verdict) {
			case 10 : return "#088A85";
			case 15 : return "#088A85";
			case 0	: // 0 also in queue
			case 20 : return "#585858";
			case 30 : return "#A9E2F3";
			case 35 : return "#000000";
			case 40 : return "#A9E2F3";
			case 45 : return "#F4FA58";
			case 50 : return "#0000FF";
			case 60 : return "#C8FE2E";
			case 70 : return "#FE2E2E";
			case 80 : return "#9FF781";
			case 90 : return "#01DF01";
			default: return "#000000";
		}
		
	}
	public static String getReadableLang(Submission s) {
		int langId = s.lang;
		switch (langId) {
		case 1 : return "ANSI C";
		case 2 : return "Java";
		case 3 : return "C++";
		case 4 : return "Pascal";
		case 5 : return "C++11";
		default: return String.valueOf(langId);
		}
	}
	@Override
	public int compareTo(Submission another) {
		return id - another.id;
	}
	private static final Descending descender = new Descending();
	public static Comparator<Submission> descending() {
		return descender;
	}
	private static class Descending implements Comparator<Submission> {
		@Override
		public int compare(Submission lhs, Submission rhs) {
			return -1 * (lhs.compareTo(rhs));
		}
		
	}

}
