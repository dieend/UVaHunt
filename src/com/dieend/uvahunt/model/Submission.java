package com.dieend.uvahunt.model;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;

public class Submission implements Serializable {
	private static final long serialVersionUID = 1L;
	int id;
	int problemId;
	int verdict;
	int runtime;
	int submitTime;
	int lang;
	int rank;
	public Submission(JSONArray arr) throws JSONException {
		this(arr.getInt(0), arr.getInt(1), arr.getInt(2), arr.getInt(3), 
				arr.getInt(4), arr.getInt(5), arr.getInt(6));
	}
	public Submission(int id, int problemId, int verdict, int runtime,
			int submitTime, int lang, int rank) {
		super();
		this.id = id;
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
	public int getProblemId() {
		return problemId;
	}
	public int getVerdict() {
		return verdict;
	}
	public String getVerdictReadable() {
		return convertVerdict(verdict);
	}
	public int getRuntime() {
		return runtime;
	}
	public int getSubmitTime() {
		return submitTime;
	}
	public int getLang() {
		return lang;
	}
	public String getLangReadable() {
		return convertLang(lang);
	}
	public int getRank() {
		return rank;
	}
	public boolean isAccepted() {
		return verdict == 90;
	}
	public static String convertVerdict(int verdictId) {
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
	public static String convertLang(int langId) {
		switch (langId) {
		case 1 : return "ANSI C";
		case 2 : return "Java";
		case 3 : return "C++";
		case 4 : return "Pascal";
		case 5 : return "C++11";
		default: return String.valueOf(langId);
		}
	}
}
