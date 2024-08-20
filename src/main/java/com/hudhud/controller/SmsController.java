package com.hudhud.controller;

import com.hudhud.config.JwtConfig;
import com.hudhud.exception.CustomException;
import com.hudhud.model.Client;
import com.hudhud.model.Sms;
import com.hudhud.model.dto.*;
import com.hudhud.repository.ClientRepository;
import com.hudhud.repository.PackageRepository;
import com.hudhud.repository.SmsCountRepository;
import com.hudhud.repository.SmsRepository;
import com.hudhud.service.ClientService;
import com.hudhud.service.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    private final ClientRepository clientRepository;

    private final ClientService clientService;

    private final SmsRepository smsRepository;

    private final JwtConfig jwtConfig;

    private final SmsCountRepository smsCountRepository;

    //        private final SmsCountRepository smsCountRepository;
    private final PackageRepository packageRepository;

//    @GetMapping("/sms/count")
//    public ResponseEntity<?> getSmsCount(
//            @RequestParam("clientId") Long clientId,
//            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//
//        LocalDateTime startDateTime = startDate.atStartOfDay();
//        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
//
//        Long count = smsService.getSmsCount(clientId, startDateTime, endDateTime);
//
//        var response = new CustomResponse();
//        response.setStatus(200);
//        response.setMessage("Message count : " + count);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @GetMapping("/sms/client/{clientId}")
    public ResponseEntity<?> getSmsByClient(@PathVariable Long clientId) throws CustomException {
        List<Sms> smsByClientId = smsService.getSmsByClientId(clientId);
        return new ResponseEntity<>(smsByClientId, HttpStatus.OK);
    }

//    @GetMapping("/sms/count/{clientId}")
//    public ResponseEntity<?> getSmsCountByClient(@PathVariable Long clientId) throws CustomException {
//        SmsCount smsCountById = smsService.getSmsCountById(clientId);
//        return new ResponseEntity<>(smsCountById, HttpStatus.OK);
//    }

    // list sms old
//    @PostMapping(value = "/send-sms-list", produces = "application/json")
//    public ResponseEntity<?> sendListSms(@RequestBody SmsListDto smsDTO) {
//        log.info("SMS REQUEST: {}", smsDTO);
//
//        Optional<Client> client = clientRepository.findClientByUsername(smsDTO.getUsername());
//
//        if (!client.isPresent()) {
//            // Handle invalid username
//            return createErrorResponse("Invalid username", HttpStatus.NOT_FOUND);
//        }
//
//        String senderId = client.get().getSenderId();
//
//        if (client.isPresent() && client.get().getPassword().equals(smsDTO.getPassword())) {
//            if (client.get().getActive() == 0) {
//                // Handle deactivated client
//                return createErrorResponse("Client is deactivated, cannot send SMS. Please contact your system admin.", HttpStatus.BAD_REQUEST);
//            }
//
//            for (ListDto requestDto : smsDTO.getListMessage()) {
//                try {
////                    log.info("MESSAGE SENT TO HIJRA GATEWAY : {}", requestDto.getMessage());
////                    Thread.sleep(200L);
//                    smppService.sendTextMessage(senderId, requestDto.getMessage(), requestDto.getReceiverAddress());
//                    log.info("SMS sent successfully.");
//                } catch (Exception e) {
//                    log.error("Error sending SMS: {}", e.getMessage());
//                }
//            }
//            // All SMS messages sent successfully
//            return createSuccessResponse("All SMS messages sent successfully.", HttpStatus.OK);
//        } else {
//            // Handle authentication failure
//            return createErrorResponse("Authentication credentials not found or invalid", HttpStatus.UNAUTHORIZED);
//        }
//    }
//
//    @Async("asyncExecutor")
//    @PostMapping(value = "/send-sms-list", produces = "application/json")
//    public CompletableFuture<ResponseEntity<?>> sendListSmsAsync(@RequestBody SmsListDto smsDTO) {
//        log.info("SMS REQUEST: {}", smsDTO);
//
//        Optional<Client> client = clientRepository.findClientByUsername(smsDTO.getUsername());
//
//        if (!client.isPresent()) {
//            // Handle invalid username
//            return CompletableFuture.completedFuture(createErrorResponse("Invalid username", HttpStatus.NOT_FOUND));
//        }
//
//        String senderId = client.get().getSenderId();
//
//        if (client.isPresent() && client.get().getPassword().equals(smsDTO.getPassword())) {
//            if (client.get().getActive() == 0) {
//                // Handle deactivated client
//                return CompletableFuture.completedFuture(createErrorResponse("Client is deactivated, cannot send SMS. Please contact your system admin.", HttpStatus.BAD_REQUEST));
//            }
//
//            List<CompletableFuture<?>> futures = new ArrayList<>();
//
//            for (ListDto requestDto : smsDTO.getListMessage()) {
//                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                    try {
//                        smppService.sendTextMessage(senderId, requestDto.getMessage(), requestDto.getReceiverAddress());
//                        log.info("SMS sent successfully.");
//                    } catch (Exception e) {
//                        log.error("Error sending SMS: {}", e.getMessage());
//                    }
//                });
//
//                futures.add(future);
//            }
//
//// Wait for all CompletableFuture instances to complete
//            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//
//            return allOf.thenApply(ignored -> createSuccessResponse("All SMS messages sent successfully.", HttpStatus.OK));
//        } else {
//
//            // Handle authentication failure
//            return CompletableFuture.completedFuture(createErrorResponse("Authentication credentials not found or invalid", HttpStatus.UNAUTHORIZED));
//        }
//    }

    private ResponseEntity<?> createSuccessResponse(String message, HttpStatus status) {
        var response = new JSONObject();
        response.put("status", status.value());
        response.put("message", message);
        return new ResponseEntity<>(response.toString(), status);
    }

    private ResponseEntity<?> createErrorResponse(String errorMessage, HttpStatus status) {
        var response = new JSONObject();
        response.put("status", status.value());
        response.put("message", errorMessage);
        return new ResponseEntity<>(response.toString(), status);
    }


    // single sms
    @PostMapping(value = "/send-sms", produces = "application/json")
    public ResponseEntity<?> sendSms(@RequestBody SmsDTO smsDTO, HttpServletRequest request) throws InterruptedException, JSONException, ExecutionException {

        var response = new JSONObject();

        String loggedInUsername = request.getUserPrincipal().getName();

        // Load credentials from the database based on the provided username
        Optional<Client> client = clientRepository.findClientByUsername(loggedInUsername);


        String senderId = client.get().getSenderId();

//        if (client.get().getStatus() == 0) {
//            response.put("status", "400");
//            response.put("message", "Client is deactivated cannot send SMS , please contact your system admin");
//            return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
//        }

        CompletableFuture<Integer> integerCompletableFuture = smsService.sendSmsAsync(loggedInUsername, smsDTO.getReceiverAddress(), smsDTO.getMessage());


        if (integerCompletableFuture.get() == 202) {

            response.put("status", "200");
            response.put("message", "success");

        } else {
            response.put("status", "400");
            response.put("message", "Error occurred while sending message to gateway");
        }


        return new ResponseEntity<>(response.toString(), HttpStatus.OK);

    }

    // bulk sms
    // TODO: 12/13/2023  max 12 digit , min 9, either 09 , 251 , 9 validation
    @PostMapping(value = "/bulk")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file,
                                             @RequestPart("message") String message,
                                             HttpServletRequest request

    ) {

        String loggedInUsername = request.getUserPrincipal().getName();


        Optional<Client> client = clientRepository.findClientByUsername(loggedInUsername);

        var response = new CustomResponse();

        if (file.isEmpty()) {
            response.setStatus(400);
            response.setMessage("please upload a file");
            return ResponseEntity.badRequest().body(response);
        }
        if (!smsService.hasExcelFormat(file)) {
            response.setStatus(400);
            response.setMessage("Invalid format please upload excel file");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {

            Long clientId = client.get().getId();

            smsService.save(file, message, clientId);
            response.setStatus(200);
            response.setMessage("Uploaded the file successfully : " + file.getOriginalFilename());

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {

            response.setStatus(500);
            response.setMessage("Failed to upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // todo : report apis


    @GetMapping("/sms/count")
    public ResponseEntity<?> getAllSmsCount(@RequestParam LocalDate date) {

        SmsCountResponse smsCount = smsService.getSmsCount(date);

        return ResponseEntity.status(smsCount.getStatus()).body(smsCount);
    }

    @GetMapping("/sms/count/{clientId}")
    public ResponseEntity<?> getClientCount(@PathVariable Long clientId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        SmsCountDetail clientSmsCountSum = smsService.getClientSmsCountSum(clientId, startDate, endDate);

        return ResponseEntity.status(clientSmsCountSum.getStatus()).body(clientSmsCountSum);
    }

    @GetMapping("/sms/count-report")
    public ResponseEntity<?> getDetailedSmsReport(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {

        ReportSmsCountResp detailedReport = smsService.getDetailedReport(startDate, endDate);

        return ResponseEntity.status(detailedReport.getStatus()).body(detailedReport);
    }

//    @GetMapping("/sms/dlr")
//    public String handleDeliveryReport(
//            @RequestParam("dlr") String dlr,
//            @RequestParam("phone") String phone,
//            @RequestParam("msgid") String msgid,
//            @RequestParam("status") String status) {
//
//        smsService.saveDeliveryReport(dlr, phone, msgid, status);
//        return "OK";
//    }
}
