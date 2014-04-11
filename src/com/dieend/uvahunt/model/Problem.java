package com.dieend.uvahunt.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Problem {
	int id;
	int number;
	String title;
	int dacu;
	int bestRuntime;
	int runtimeLimit;
	int bestMemory;
	int numOfRuntimeError;
	int numOfOutputLimitError;
	int numOfTimeLimitError;
	int numOfMemoryLimitError;
	int numOfWrongAnswer;
	int numOfPresentationError;
	int numOfAccepted;
	boolean solved;

	Problem(int id,
		int number,
		String title,
		int dacu,
		int bestRuntime,
		int bestMemory,
		int numOfRuntimeError,
		int numOfOutputLimitError,
		int numOfTimeLimitError,
		int numOfMemoryLimitError,
		int numOfWrongAnswer,
		int numOfPresentationError,
		int numOfAccepted,
		int runtimeLimit,
		boolean solved) {
		this.id = id;
		this.number = number;
		this.title = title;
		this.dacu = dacu;
		this.bestRuntime = bestRuntime;
		this.runtimeLimit = runtimeLimit;
		this.bestMemory = bestMemory;
		this.numOfRuntimeError = numOfRuntimeError;
		this.numOfOutputLimitError = numOfOutputLimitError;
		this.numOfTimeLimitError = numOfTimeLimitError;
		this.numOfMemoryLimitError = numOfMemoryLimitError;
		this.numOfWrongAnswer = numOfWrongAnswer;
		this.numOfPresentationError = numOfPresentationError;
		this.numOfAccepted = numOfAccepted;
		this.solved = solved;
	}
	public int getId() {
		return id;
	}
	public int getNumber() {
		return number;
	}
	public String getTitle() {
		return title;
	}
	public int getDacu() {
		return dacu;
	}
	public int getBestRuntime() {
		return bestRuntime;
	}
	public int getRuntimeLimit() {
		return runtimeLimit;
	}
	public int getBestMemory() {
		return bestMemory;
	}
	public int getNumOfRuntimeError() {
		return numOfRuntimeError;
	}
	public int getNumOfOutputLimitError() {
		return numOfOutputLimitError;
	}
	public int getNumOfTimeLimitError() {
		return numOfTimeLimitError;
	}
	public int getNumOfMemoryLimitError() {
		return numOfMemoryLimitError;
	}
	public int getNumOfWrongAnswer() {
		return numOfWrongAnswer;
	}
	public int getNumOfPresentationError() {
		return numOfPresentationError;
	}
	public int getNumOfAccepted() {
		return numOfAccepted;
	}
	public boolean isSolved() {
		return solved;
	}
	public static boolean isSolved(int pid) {
		return solveds.contains(pid);
	}
	private static Set<Integer> solveds = new TreeSet<Integer>();
	public static Set<Integer> solvedProblems() {
		return Collections.unmodifiableSet(solveds);
	}
	public static void populateSolvedProblem(String json) {
		try {
			JSONArray array = new JSONArray(json);
			JSONObject obj = array.getJSONObject(0);
			array = obj.getJSONArray("solved");
			for (int j=0; j<array.length(); j++) {
				int a = array.getInt(j);
				for (int i = 0; i<32; i++) {
					int ii = (1 << i);
					if ((ii & a) == ii) {
						solveds.add(j*32 + i); 
					}
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
