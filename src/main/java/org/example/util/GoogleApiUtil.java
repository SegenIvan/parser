package org.example.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.example.dto.GoogleSheetDTO;
import org.example.exception.NotFound;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
public class GoogleApiUtil {

    private static final String APPLICATION_NAME = "Parser";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);

    private static final Map<String, Integer> time = new HashMap<>();

    private final Map<String, Integer> phrases = new HashMap<>();

    private String urlFile;

    //@Value("${spreadsheet.id}")
    private String spreadsheetId;

    private Integer sumFile;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleApiUtil.class.getResourceAsStream("/cred.json");
        return GoogleCredential.fromStream(in).createScoped(SCOPES);
    }

    private Sheets getSheetService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME).build();
    }

    private static List<Object> createMapTime(GoogleSheetDTO request) {
        LocalTime start = LocalTime.of(1, 0);
        LocalTime end = LocalTime.of(23, 0);
        List<Object> data = new ArrayList<>();
        int i = 1;
        long z = 0L;
        LocalTime s;
        do {
            s = start.plusMinutes(z);
            data.add(s.toString());
            time.put(s.toString(), i++);
            z += Long.parseLong(request.getTime());
        } while (s.isBefore(end));
        return data;
    }


    public void getDataFromSheet() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        try {
            final String range = "pars";
            Sheets service = getSheetService();
            ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
            List<List<Object>> values = response.getValues();
            int k = 1;
            for (int i = 1; i < values.size(); i++) {
                phrases.put((String) values.get(k).get(0), i);
                k++;
            }
        } catch (GoogleJsonResponseException e) {
            throw new NotFound("Неверная ссылка");
        }
    }

    private SheetProperties checkSheet() throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();
        Spreadsheet sp = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = sp.getSheets();
        for (Sheet sheet : sheets) {
            if (sheet.getProperties().getTitle().equals(LocalDate.now().toString())) {
                return sheet.getProperties();
            }
        }
        return null;
    }

    private SheetProperties addSheet() throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();
        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(LocalDate.now().toString()))));
        return service.spreadsheets()
                .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests))
                .execute().getReplies().get(0).getAddSheet().getProperties();
    }

    private void copyPast(Integer id, String title, List<Object> data) throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();
        CopyPasteRequest copyRequest = new CopyPasteRequest()
                .setSource(new GridRange().setSheetId(0))
                .setDestination(new GridRange().setSheetId(id))
                .setPasteType("PASTE_NORMAL");

        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setCopyPaste(copyRequest));
        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, body).execute();

        ValueRange valueRange = new ValueRange()
                .setRange(title + "!B1")
                .setValues(Collections.singletonList(data));

        BatchUpdateValuesRequest value = new BatchUpdateValuesRequest().setValueInputOption("RAW")
                .setData(Collections.singletonList(valueRange));
        service.spreadsheets().values().batchUpdate(spreadsheetId, value).execute();

        List<Request> requestss = new ArrayList<>();
        List<CellData> values = new ArrayList<>();
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue("сумма")));
        requestss.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(id)
                                .setRowIndex(phrases.size() + 1)
                                .setColumnIndex(0))
                        .setRows(List.of(
                                new RowData().setValues(values)))
                        .setFields("*")));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue("итог по файлу")));
        requestss.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(id)
                                .setRowIndex(phrases.size() + 3)
                                .setColumnIndex(0))
                        .setRows(List.of(
                                new RowData().setValues(values)))
                        .setFields("*")));

        BatchUpdateSpreadsheetRequest batchUpdateRequestt = new BatchUpdateSpreadsheetRequest()
                .setRequests(requestss);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequestt)
                .execute();
    }

    private Map<String, String> downloadFile() throws IOException {

        Map<String, String> phrasesInFile = new HashMap<>();

        Scanner input = new Scanner(new InputStreamReader(new URL(urlFile).openStream()));
        input.useDelimiter("\n");

        while (input.hasNext()) {
            String str = input.next();
            if (Objects.equals(str, "-----------")) {
                str = input.next();
                String[] split1 = str.split("ИТОГО: ");
                sumFile = Integer.parseInt(split1[1]);
                break;
            }

            String[] split = str.split("-");
            phrasesInFile.put(split[1].replace("\r", ""), split[0]);
        }

        input.close();
        return phrasesInFile;
    }

    private void setDataInSheet(Map<String, String> data, Integer id) throws GeneralSecurityException, IOException {
        Sheets service = getSheetService();
        Integer sum = 0;
        List<Request> requests = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sum += Integer.parseInt(entry.getValue());
            List<CellData> values = new ArrayList<>();
            if (phrases.get(entry.getKey()) != null) {
                values.add(new CellData()
                        .setUserEnteredValue(new ExtendedValue()
                                .setStringValue(entry.getValue())));
                requests.add(new Request()
                        .setUpdateCells(new UpdateCellsRequest()
                                .setStart(new GridCoordinate()
                                        .setSheetId(id)
                                        .setRowIndex(phrases.get(entry.getKey()))
                                        .setColumnIndex(time.get(LocalTime.now().getHour() + ":00")))
                                .setRows(List.of(
                                        new RowData().setValues(values)))
                                .setFields("*")));
            }
        }
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute();

        List<Request> requestss = new ArrayList<>();
        List<CellData> values = new ArrayList<>();
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(String.valueOf(sum))));
        requestss.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(id)
                                .setRowIndex(phrases.size() + 1)
                                .setColumnIndex(5))
                        .setRows(List.of(
                                new RowData().setValues(values)))
                        .setFields("*")));

        BatchUpdateSpreadsheetRequest batchUpdateRequestt = new BatchUpdateSpreadsheetRequest()
                .setRequests(requestss);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequestt)
                .execute();

        List<Request> requestsss = new ArrayList<>();
        List<CellData> valuess = new ArrayList<>();
        valuess.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(String.valueOf(sumFile))));
        requestsss.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(id)
                                .setRowIndex(phrases.size() + 3)
                                .setColumnIndex(5))
                        .setRows(List.of(
                                new RowData().setValues(valuess)))
                        .setFields("*")));

        BatchUpdateSpreadsheetRequest batchUpdateRequesttt = new BatchUpdateSpreadsheetRequest()
                .setRequests(requestsss);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequesttt)
                .execute();
    }

    public void createGoogleSheet(GoogleSheetDTO request) throws GeneralSecurityException, IOException {
        urlFile = request.getFileLink();
        String[] split = request.getSheetLink().split("/");
        spreadsheetId = split[5];
        List<Object> data = createMapTime(request);
        getDataFromSheet();
        SheetProperties sheetProperties = checkSheet();
        if (sheetProperties == null) {
            sheetProperties = addSheet();
        } else {
            copyPast(sheetProperties.getSheetId(), sheetProperties.getTitle(), data);
        }
        setDataInSheet(downloadFile(), sheetProperties.getSheetId());
    }
}
