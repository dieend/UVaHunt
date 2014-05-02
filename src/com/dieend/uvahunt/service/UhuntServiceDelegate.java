package com.dieend.uvahunt.service;

import java.util.List;
import java.util.Map;

import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.model.User;
import com.dieend.uvahunt.model.UserRank;

public interface UhuntServiceDelegate {
	public void profileReady(User userdata);
	public void submissionReady();
	public void serviceReady(boolean liveUpdateActive);
	public void submissionArrival(Map<Integer, Submission> submissions);
	public void rankReady(List<UserRank> ranks);
	public void failed(String reason);
}
