package org.example.controller;

import org.example.dto.GoogleSheetDTO;
import org.example.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class DashboardController {

	@Autowired
	private GoogleApiService googleApiService;

	@PostMapping("/createSheet")
	public void createGoogleSheet(@RequestBody GoogleSheetDTO request)
			throws GeneralSecurityException, IOException {
		googleApiService.createSheet(request);
	}
}
