package org.example.service;

import org.example.dto.GoogleSheetDTO;
import org.example.util.GoogleApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class GoogleApiService {

	@Autowired
	private GoogleApiUtil googleApiUtil;

	public void createSheet(GoogleSheetDTO request) throws GeneralSecurityException, IOException {
		googleApiUtil.createGoogleSheet(request);
	}

}
