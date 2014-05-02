package com.dieend.uvahunt.model;

public class UserRank {
	int rank;
	String name;
	String username;
	int ac;
	int nos;
	public UserRank(int rank, String name, String username, int ac, int nos) {
		super();
		this.rank = rank;
		this.name = name;
		this.username = username;
		this.ac = ac;
		this.nos = nos;
	}
	public int getRank() {
		return rank;
	}
	public String getName() {
		return name;
	}
	public String getUsername() {
		return username;
	}
	public int getAc() {
		return ac;
	}
	public int getNos() {
		return nos;
	}
}
