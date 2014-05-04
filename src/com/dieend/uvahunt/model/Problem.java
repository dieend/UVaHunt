package com.dieend.uvahunt.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class Problem {
	int id;
	int number;
	String title;
	int dacu;
	int bestRuntime;
	int runtimeLimit;
	int bestMemory;
	int numOfCompileError;
	int numOfRuntimeError;
	int numOfOutputLimitError;
	int numOfTimeLimitError;
	int numOfMemoryLimitError;
	int numOfWrongAnswer;
	int numOfPresentationError;
	int numOfAccepted;
	Problem(int id,
		int number,
		String title,
		int dacu,
		int bestRuntime,
		int runtimeLimit,
		int bestMemory,
		int numOfCompileError,
		int numOfRuntimeError,
		int numOfOutputLimitError,
		int numOfTimeLimitError,
		int numOfMemoryLimitError,
		int numOfWrongAnswer,
		int numOfPresentationError,
		int numOfAccepted) {
		this.id = id;
		this.number = number;
		this.title = title;
		this.dacu = dacu;
		this.bestRuntime = bestRuntime;
		this.runtimeLimit = runtimeLimit;
		this.bestMemory = bestMemory;
		this.numOfCompileError = numOfCompileError;
		this.numOfRuntimeError = numOfRuntimeError;
		this.numOfOutputLimitError = numOfOutputLimitError;
		this.numOfTimeLimitError = numOfTimeLimitError;
		this.numOfMemoryLimitError = numOfMemoryLimitError;
		this.numOfWrongAnswer = numOfWrongAnswer;
		this.numOfPresentationError = numOfPresentationError;
		this.numOfAccepted = numOfAccepted;
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
	public int getNumOfCompileError() {
		return numOfCompileError;
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
		return isSolved(id);
	}
	public boolean isTried() {
		return trieds.contains(id);
	}
	public int getLevel() {
		return 10 - ((int)Math.floor(Math.min(10, Math.log(dacu > 0? dacu : 1))));
	}
	
	public static boolean isSolved(int pid) {
		return solveds.contains(pid);
	}
	private static Set<Integer> solveds = new TreeSet<Integer>();
	private static Set<Integer> trieds = new TreeSet<Integer>();

	public static Set<Integer> solvedProblems() {
		return Collections.unmodifiableSet(solveds);
	}
	public static Set<Integer> triedProblems() {
		return Collections.unmodifiableSet(trieds);
	}
	public static void solve(int id) {
		solveds.add(id);
		trieds.remove(id);
	}
	public static void tried(int id) {
		if (!solveds.contains(id)) {
			trieds.add(id);
		}
	}
	public static void reset() {
		solveds.clear();
		trieds.clear();
	}
	
}
