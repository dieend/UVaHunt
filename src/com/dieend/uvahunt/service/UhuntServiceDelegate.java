package com.dieend.uvahunt.service;

import com.dieend.uvahunt.service.base.ServiceManager;

public interface UhuntServiceDelegate {
	public ServiceManager getServiceManager();
	public void profileReady(String userdata);
	public void submissionReady();
}
