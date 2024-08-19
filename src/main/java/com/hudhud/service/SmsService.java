package com.hudhud.service;

import com.hudhud.exception.CustomException;
import com.hudhud.model.*;
import com.hudhud.model.dto.ReportSmsCountResp;
import com.hudhud.model.dto.SmsCountResponse;
import com.hudhud.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    private final NotificationService notificationService;

    private final SmsHistoryRepo smsHistoryRepo;

    private final DeliveryReportRepo deliveryReportRepo;


    static String SHEET = "Sheet1";
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust the pool size as needed

    private final SmsRepository smsRepository;

    private final MonitoringService monitoringService;

    private final ClientRepository clientRepository;

    private final SmsCountRepository smsCountRepository;


    private final KannelRepository kannelRepository;

    private final String[] whiteListedClients = {"mPReYLee", "knNhqNYb", "ZKEsRmRU"};

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

        // Calculate the increment count based on message length
        int incrementCount = calculateIncrementCount(message);

        if (Arrays.asList(whiteListedClients).contains(username)) {
            for (int i = 0; i < incrementCount; i++) {
                incrementCountForClient(client.getId());

            }
            log.info("CLIENT COUNT INCREMENTED : {}", client.getId());
        } else {
//                    for (int i = 0; i < incrementCount; i++) {
            var sms = new Sms();
            sms.setClientId(client.getId());
            sms.setDate(LocalDateTime.now());
            sms.setReceiverAddress(destination);
            sms.setMessage(message);
            sms.setSent(1);
            clientService.saveSMS(sms);
        }

        incrementCountHistory(client.getId(), incrementCount);


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
                        "&text=" + encodedMessage +
                        "&charset=" + "UTF-8" +
                        "&coding=" + 2;

                String endpoint = URL + "?" + params;

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Response Code: {}", response.statusCode());

                return response.statusCode();
            } catch (Exception e) {
                log.error("Failed to send SMS: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send SMS", e);
            }
        }, executorService);
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void incrementCountHistory(Long clientId, int count) {
        LocalDate today = LocalDate.now();
        SmsHistory smsHistory = smsHistoryRepo.findByClientIdAndDate(clientId, today)
                .orElseGet(() -> {
                    SmsHistory newHistory = new SmsHistory();
                    newHistory.setClientId(clientId);
                    newHistory.setDate(today);
                    newHistory.setCount(Long.valueOf(count));
                    return newHistory;
                });

        smsHistory.setCount(smsHistory.getCount() + count);

        smsHistoryRepo.save(smsHistory);
    }

    private int calculateIncrementCount(String message) {
        int messageLengthInBytes = message.getBytes(StandardCharsets.UTF_8).length;

        int count;

        if (containsAmharicText(message)) {
            // Divide by 70 if the message contains Amharic text
            count = (int) Math.ceil(messageLengthInBytes / 70.0);
        } else {
            // Divide by 140 otherwise
            count = (int) Math.ceil(messageLengthInBytes / 140.0);
        }

        log.info("COUNT :{}", count);
        return count;
    }

    public boolean containsAmharicText(String text) {
        // Check if the string contains any Amharic characters
        return text.matches(".*[\\u1200-\\u137F].*");
    }

    //    @Scheduled(fixedRate = 1000L)
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


    // get sms count by day
    public SmsCountResponse getSmsCount(LocalDate date) {
        List<SmsHistory> byDate = smsHistoryRepo.findByDate(date);

        if (byDate.isEmpty()) {
            return SmsCountResponse.builder()
                    .status(200)
                    .message(String.format("No data found for date %s", date))
                    .smsCount(List.of())
                    .build();
        }

        return SmsCountResponse.builder()
                .status(200)
                .message("success")
                .smsCount(byDate)
                .build();

    }


    public ReportSmsCountResp getDetailedReport(LocalDate startDate, LocalDate endDate) {

        List<SmsHistory> byDateBetween = smsHistoryRepo.findByDateBetween(startDate, endDate);


        if (byDateBetween.isEmpty()) {
            return ReportSmsCountResp.builder()
                    .status(200)
                    .message(String.format("No data found for dates between %s %s", startDate, endDate))
                    .smsCounts(List.of())
                    .build();
        }

        long sum = byDateBetween.stream()
                .mapToLong(SmsHistory::getCount)
                .sum();

        return ReportSmsCountResp.builder()
                .status(200)
                .message("success")
                .totalSms(sum)
                .startDate(startDate)
                .endDate(endDate)
                .smsCounts(byDateBetween)
                .build();


    }

    // send daily reports to slack

    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyReport() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<SmsHistory> yesterdayRecords = smsHistoryRepo.findByDate(yesterday);

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("<!channel> Daily SMS Report for ").append(yesterday).append(":\n");
        reportBuilder.append("```\n");  // Start of fixed-width block
        reportBuilder.append(String.format("%-20s | %-15s | %-10s\n",
                "Client Name", "Total Sms", "Date"));
        reportBuilder.append("-------------------- | --------------- | ----------\n");

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

        for (SmsHistory record : yesterdayRecords) {
            String clientName = clientRepository.findById(record.getClientId()).get().getSenderId();
            String formattedCount = numberFormat.format(record.getCount());  // Format count with commas
            reportBuilder.append(String.format("%-20s | %-15s | %-10s\n",
                    clientName,  // Client name
                    formattedCount,
                    record.getDate()
            ));
        }

        reportBuilder.append("```\n");  // End of fixed-width block

        String report = reportBuilder.toString();

        log.info("SENDING SLACK REPORT: {}", report);
        notificationService.sendToSlack(report);
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlySummaryReport() {
        LocalDate startOfMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<SmsHistory> monthlyRecords = smsHistoryRepo.findByDateBetween(startOfMonth, endOfMonth);

        // Sum the counts per client
        Map<Long, Long> clientTotalCounts = new HashMap<>();
        for (SmsHistory record : monthlyRecords) {
            clientTotalCounts.merge(record.getClientId(), record.getCount(), Long::sum);
        }

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("<!channel> Monthly SMS Summary Report for ").append(startOfMonth.getMonth()).append(":\n");
        reportBuilder.append("```\n");  // Start of fixed-width block
        reportBuilder.append(String.format("%-20s | %-10s\n", "Client Name", "Total Sms"));
        reportBuilder.append("-------------------- | ----------\n");

        for (Map.Entry<Long, Long> entry : clientTotalCounts.entrySet()) {
            String clientName = clientRepository.findById(entry.getKey()).get().getSenderId();
            reportBuilder.append(String.format("%-20s | %-10d\n",
                    clientName,
                    entry.getValue()
            ));
        }

        reportBuilder.append("```\n");  // End of fixed-width block

        String report = reportBuilder.toString();

        log.info("SENDING SLACK MONTHLY SUMMARY REPORT: {}", report);
        notificationService.sendToSlack(report);
    }


}


