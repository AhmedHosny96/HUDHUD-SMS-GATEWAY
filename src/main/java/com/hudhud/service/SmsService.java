package com.hudhud.service;

import com.hudhud.exception.CustomException;
import com.hudhud.model.Client;
import com.hudhud.model.Sms;
import com.hudhud.model.dto.SmsCount;
import com.hudhud.repository.ClientRepository;
import com.hudhud.repository.PackageRepository;
import com.hudhud.repository.SmsCountRepository;
import com.hudhud.repository.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${sms.smpp.url}")
    private String URL;
    @Value("${sms.smpp.username}")
    private String USERNAME;
    @Value("${sms.smpp.password}")
    private String PASSWORD;

    @Value("${sms.smpp.ipAddress}")
    private String IPADDRESS;


    static String SHEET = "Sheet1";
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust the pool size as needed

    private final SmsRepository smsRepository;

    private final MonitoringService monitoringService;

    private final ClientRepository clientRepository;

    private final SmsCountRepository smsCountRepository;

    private final PackageRepository packageRepository;

    // TODO : FORWARD SMS TO KANNEL GATEWAY
    public CompletableFuture<Integer> sendSmsAsync(String username, String destination, String message) {

        boolean reachableViaPing = monitoringService.isReachableViaPing(IPADDRESS);

//        if (reachableViaPing)

        log.info("SMS-KANNEL REACHABLE : {}", reachableViaPing);

        if (!reachableViaPing) {
            // Notify Slack
            monitoringService.sendToSlack(":warning: SMS KANNEL NETWORK IS UNREACHABLE");
            // Throw an exception or return a specific value
            // You can customize this based on your requirements
            throw new RuntimeException("SMS KANNEL NETWORK IS UNREACHABLE");
        }

        Client client = clientRepository.findClientByUsername(username).get();
        String from = client.getSenderId();

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Replace spaces in message with %20
                String encodedMessage = message.replaceAll(" ", "%20");

                // Concatenate the URL parameters without encoding
                String params = "username=" + USERNAME +
                        "&password=" + PASSWORD +
                        "&from=" + from +
                        "&to=" + destination +
                        "&text=" + encodedMessage;

                String endpoint = URL + "?" + params;

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("SMS KANNEL RESPONSE : {}", response);
                log.info("Response Code: {}", response.statusCode());


                return response.statusCode();
            } catch (Exception e) {
                log.error("Failed to send SMS: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send SMS", e);
            }
        }, executorService);
    }

    public Long getSmsCount(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        long result = smsRepository.countByClientIdAndDateBetween(clientId, startDate, endDate);

        log.info("SMS COUNT : {}", result);

        return result;
    }

    public List<Sms> getSmsByClientId(String clientId) throws CustomException {

        List<Sms> byClientId = smsRepository.findByClientId(clientId);

        if (byClientId.isEmpty()) {
            throw new CustomException("No sms found for this client");
        }
        return byClientId;
    }

    //
    public SmsCount getSmsCountById(Long clientId) throws CustomException {
        Client client = clientRepository.findById(clientId).get();
        SmsCount byClientId = smsCountRepository.findByClient(Optional.of(client));
        if (byClientId == null) {
            throw new CustomException("No sms found for this client");
        }
        return byClientId;
    }


//    @Transactional(propagation = Propagation.REQUIRED)
//    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
//    void processPendingSms() {
//        List<Sms> pendingSmsList = smsRepository.findPendingSms();
//        if (!pendingSmsList.isEmpty()) {
//            Sms sms = pendingSmsList.get(0); // Get the first pending SMS
//            try {
//                Optional<Client> senderId = clientRepository.findById(Long.valueOf(sms.getClientId()));
//                if (senderId.isPresent()) {
//                    log.info("SENDER ID: {}", senderId.get().getSenderId());
//                    log.info("SMS PAYLOAD: {}", sms);
//                    new ApplsendTextMessage(senderId.get().getSenderId(), sms.getMessage(), sms.getReceiverAddress());
//                } else {
//                    log.error("Client not found for SMS ID: {}", sms.getId());
//                }
//            } catch (Exception e) {
//                log.error("Error sending SMS ID: {}", sms.getId(), e);
//            }
//        }
//    }

    // asychronous processing


    // excel format checker
    public boolean hasExcelFormat(MultipartFile file) {
        if (!TYPE.equals(file.getContentType())) {
            return false;
        }
        return true;
    }

    // bulk sms
    public static List<Sms> processExcelFile(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();

            List<Sms> tutorials = new ArrayList<>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();

                Sms sms = new Sms();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                sms.setClientId(String.valueOf((long) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                sms.setClientId(currentCell.getStringCellValue());
                            }
                            break;
                        case 1:
                            sms.setMessage(currentCell.getStringCellValue());
                            sms.setDate(LocalDateTime.now());
                            break;

                        case 2:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                sms.setReceiverAddress(String.valueOf((long) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                sms.setReceiverAddress(currentCell.getStringCellValue());
                            }
                            break;
                        default:
                            sms.setSent(0);
                            break;
                    }
                    cellIdx++;
                }

                tutorials.add(sms);
            }
            workbook.close();

            return tutorials;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public void save(MultipartFile file) {
        try {
            List<Sms> tutorials = processExcelFile(file.getInputStream());
            smsRepository.saveAll(tutorials);
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    // process sms list , message and phone number


//
}


