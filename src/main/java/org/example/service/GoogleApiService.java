package org.example.service;

import org.example.dto.GoogleSheetDTO;
import org.example.dto.GoogleSheetResponseDTO;
import org.example.util.GoogleApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@Service
public class GoogleApiService {

	@Autowired
	private GoogleApiUtil googleApiUtil;

	public Map<Object, Object> readDataFromGoogleSheet() throws GeneralSecurityException, IOException {
		return googleApiUtil.getDataFromSheet();
	}

	public GoogleSheetResponseDTO createSheet(GoogleSheetDTO request) throws GeneralSecurityException, IOException {
		return googleApiUtil.createGoogleSheet(request);
	}

}
