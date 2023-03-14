package org.example.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.example.dto.GoogleSheetDTO;
import org.example.dto.GoogleSheetResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class GoogleApiUtil {

    private static final String APPLICATION_NAME = "Parser";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);

    @Value("${spreadsheet.id}")
    private String spreadsheetId;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleApiUtil.class.getResourceAsStream("/cred.json");
        return GoogleCredential.fromStream(in).createScoped(SCOPES);


    }

    public Map<Object, Object> getDataFromSheet() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final String range = "pars!A1:C";
        Sheets service = getSheetService();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        Map<Object, Object> storeDataFromGoogleSheet = new HashMap<>();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                storeDataFromGoogleSheet.put(row.get(0), row.get(2));
            }
        }
        return storeDataFromGoogleSheet;
    }

    private Sheets getSheetService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME).build();
    }

    private SheetProperties addSheet() throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();
        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(LocalDate.now().toString()))));
        return service.spreadsheets()
                .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests))
                .execute().getReplies().get(0).getAddSheet().getProperties();
    }

    private void copyPast(Integer id) throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();
        CopyPasteRequest copyRequest = new CopyPasteRequest()
                .setSource(new GridRange().setSheetId(0))
                .setDestination(new GridRange().setSheetId(id))
                .setPasteType("PASTE_VALUES");

        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setCopyPaste(copyRequest));
        BatchUpdateSpreadsheetRequest body
                = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }

    private List<Object> downloadFile() throws IOException {

        Scanner input = new Scanner(new InputStreamReader(new URL("ftp://FTPcv:$7OVtBQoxgCpWFrSX@65.108.99.156/nasrf.ru-31-01-2023.txt").openStream()));
        input.useDelimiter("-|\n");

        List<Object> result = new ArrayList<>();
        while (input.hasNext()) {
            result.add(input.next());
        }

        input.close();
        return result;
    }

    private void setDataInSheet(List<Object> data, String title, Integer id) throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();

        ValueRange valueRange = new ValueRange()
                .setMajorDimension("COLUMNS")
                .setValues(Collections.singletonList(data));

        service.spreadsheets().values()
                .append(spreadsheetId, title, valueRange)
                .setValueInputOption("RAW")
                .execute();


       /* List<Request> requests = new ArrayList<>();
        List<CellData> values = new ArrayList<>();

        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue("Hello World!")));
        requests.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(id)
                                .setRowIndex(4)
                                .setColumnIndex(4))
                        .setRows(List.of(
                                new RowData().setValues(values)))
                        .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute();*/


        /*BatchUpdateValuesResponse result;
        try {
            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("RAW")
                    .setData(Collections.singletonList(valueRange));
            result = service.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
            System.out.printf("%d cells updated.", result.getTotalUpdatedCells());
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
            } else {
                throw e;
            }
        }*/
    }


    public GoogleSheetResponseDTO createGoogleSheet(GoogleSheetDTO request)
            throws GeneralSecurityException, IOException {
        SheetProperties sheetProperties = addSheet();
        copyPast(sheetProperties.getSheetId());
        setDataInSheet(downloadFile(), sheetProperties.getTitle(), sheetProperties.getSheetId());
        return new GoogleSheetResponseDTO();
    }
}
