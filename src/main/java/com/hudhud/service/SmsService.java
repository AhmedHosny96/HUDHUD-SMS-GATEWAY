package com.hudhud.service;

import com.hudhud.exception.CustomException;
import com.hudhud.model.*;
import com.hudhud.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

    private final ClientService clientService;


    private final DeliveryReportRepo deliveryReportRepo;


    static String SHEET = "Sheet1";
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust the pool size as needed

    private final SmsRepository smsRepository;

    private final MonitoringService monitoringService;

    private final ClientRepository clientRepository;

    private final SmsCountRepository smsCountRepository;


    private final KannelRepository kannelRepository;

    private final String[] whiteListedClients = {"phuAgjlu", "mPReYLee", "zQwtOBcQ"}; // knNhqNYb : halapay 1. 2. sahay 3. hijra bank


    // delivery report

    // TODO : FORWARD SMS TO KANNEL GATEWAY
    public CompletableFuture<Integer> sendSmsAsync(String username, String destination, String message) {
        boolean reachableViaPing = monitoringService.isReachableViaPing(IPADDRESS);
        if (!reachableViaPing) {
            // Notify Slack
            monitoringService.sendToSlack(":warning: SMS KANNEL NETWORK IS UNREACHABLE");
            throw new RuntimeException("SMS KANNEL NETWORK IS UNREACHABLE");
        }

        Client client = clientRepository.findClientByUsername(username).orElseThrow(() -> new RuntimeException("Client not found"));

        String from = client.getSenderId();

        // Check if the client is whitelisted
        if (Arrays.asList(whiteListedClients).contains(username)) {
            // Increment count for the client
            incrementCountForClient(client.getId());
            log.info("CLIENT COUNT INCREMENTED : {}", client.getId());
        } else {
            // Save only for non-whitelisted clients
            var sms = new Sms();
            sms.setClientId(client.getId());
            sms.setDate(LocalDateTime.now());
            sms.setReceiverAddress(destination);
            sms.setMessage(message);
            sms.setSent(1);
            clientService.saveSMS(sms);
        }

        // Send SMS for all clients
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Encode each query parameter
                String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
                String encodedFrom = URLEncoder.encode(from, StandardCharsets.UTF_8);
                String encodedDestination = URLEncoder.encode(destination, StandardCharsets.UTF_8);

                // Concatenate the URL parameters
                String params = "username=" + URLEncoder.encode(USERNAME, StandardCharsets.UTF_8) +
                        "&password=" + URLEncoder.encode(PASSWORD, StandardCharsets.UTF_8) +
                        "&from=" + encodedFrom +
                        "&to=" + encodedDestination +
                        "&text=" + encodedMessage;

                String endpoint = URL + "?" + params;

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Response Code: {}", response.statusCode());

                if (response.statusCode() != 202) {
                    // notify to slack
                    monitoringService.sendToSlack("🚨 URGENT SMS KANNEL HAS ERROR 🚨");
                }

                return response.statusCode();
            } catch (Exception e) {
                log.error("Failed to send SMS: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send SMS", e);
            }
        }, executorService);
    }

    @Scheduled(fixedRate = 1000L)
    protected void sendPendingSms() {
        Page<Sms> pendingSmsPage = smsRepository.findBySentOrderByIdDesc(0, PageRequest.of(0, 10)); // Fetches 50 unsent SMS

        pendingSmsPage.forEach(sms -> {
            if (sms.getClientId() != null) {
                try {
                    Long clientId = Long.valueOf(sms.getClientId());
                    String username = getUsernameByClient(clientId);
                    log.info("PENDING SMS : {}", sms);
                    sendSmsAsync(username, sms.getReceiverAddress(), sms.getMessage());

                    // Update the status to 'sent'
                    sms.setSent(1);
                    smsRepository.save(sms);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse clientId: {}, Error: {}", sms.getClientId(), e.getMessage());
                }
            } else {
                log.warn("Invalid or missing clientId for SMS: {}", sms);
            }
        });
    }


    @Transactional
    public void incrementKannelCount() {
        Kannel kannel = kannelRepository.findById(1L).orElseThrow(() -> new RuntimeException("Kannel entity not found"));
        kannel.setTotalCount(kannel.getTotalCount() + 1);
        kannelRepository.save(kannel);
    }

    // increment client sms
    private void incrementCountForClient(Long clientId) {
        // Assuming you have a method to save/update the SMS count for the client
        SmsCount smsCount = smsCountRepository.findByClientId(clientId);
        if (smsCount == null) {
            // If no record exists, create a new one
            smsCount = new SmsCount();
            smsCount.setClientId(clientId);
            smsCount.setCreatedAt(LocalDateTime.now());
            smsCount.setUpdatedAt(LocalDateTime.now());
            smsCount.setCount(1L); // Initialize count to 1
        } else {
            // If a record exists, increment the count
            smsCount.setUpdatedAt(LocalDateTime.now());
            smsCount.setCount(smsCount.getCount() + 1);
        }
        smsCountRepository.save(smsCount);
    }

    public Long getSmsCount(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
//        long result = smsRepository.countByClientIdAndDateBetween(clientId, startDate, endDate);
//        log.info("SMS COUNT : {}", result);
        long smsCountByClientIdAndDateBetween = smsCountRepository.smsCountBetweenDatesForClient(clientId, startDate, endDate);
        log.info("SMS COUNT : {}", smsCountByClientIdAndDateBetween);
        return smsCountByClientIdAndDateBetween;
    }

    public List<Sms> getSmsByClientId(Long clientId) throws CustomException {

        List<Sms> byClientId = smsRepository.findByClientId(clientId);

        if (byClientId.isEmpty()) {
            throw new CustomException("No sms found for this client");
        }
        return byClientId;
    }


    // excel format checker
    public boolean hasExcelFormat(MultipartFile file) {
        if (!TYPE.equals(file.getContentType())) {
            return false;
        }
        return true;
    }

    // bulk sms
    public static List<Sms> processExcelFile(InputStream is, String message, Long clientId) {
        final String SHEET = "Sheet1"; // Ensure this matches the name of your sheet
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();

            List<Sms> smsList = new ArrayList<>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // Skip header row
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                Sms sms = new Sms();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    if (currentCell.getCellType() == CellType.BLANK) continue;

                    switch (cellIdx) {
                        case 0: // Receiver Address
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                sms.setReceiverAddress(String.valueOf((long) currentCell.getNumericCellValue()));
                            } else if (currentCell.getCellType() == CellType.STRING) {
                                sms.setReceiverAddress(currentCell.getStringCellValue());
                            }
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                sms.setMessage(message);
                sms.setClientId(clientId);
                sms.setDate(LocalDateTime.now());
                sms.setSent(0); // Default sent status
                smsList.add(sms);
            }
            return smsList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }


    @Transactional
    public void save(MultipartFile file, String message, Long clientId) {
        try {
            List<Sms> tutorials = processExcelFile(file.getInputStream(), message, clientId);
            smsRepository.saveAll(tutorials);
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public String getUsernameByClient(Long clientId) {
        Client client = clientRepository.findById(clientId).get();

        return client.getUsername();
    }

    public Long getClientIdByUsername(String username) {
        Client client = clientRepository.findClientByUsername(username).get();

        return client.getId();
    }

    // sms monthly report

}


