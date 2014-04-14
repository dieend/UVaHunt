package com.dieend.uvahunt.service;

import java.util.Map;

import com.dieend.uvahunt.model.Submission;
import com.dieend.uvahunt.model.User;

public interface UhuntServiceDelegate {
	public void profileReady(User userdata);
	public void submissionReady();
	public void serviceReady(boolean liveUpdateActive);
	public void submissionArrival(Map<Integer, Submission> submissions);
	
}
