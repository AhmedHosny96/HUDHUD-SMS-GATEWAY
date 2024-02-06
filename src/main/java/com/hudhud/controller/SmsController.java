package com.hudhud.controller;

import com.hudhud.exception.CustomException;
import com.hudhud.model.Client;
import com.hudhud.model.Sms;
import com.hudhud.model.Packages;
import com.hudhud.model.dto.CustomResponse;
import com.hudhud.model.dto.SmsDTO;
import com.hudhud.repository.ClientRepository;
import com.hudhud.repository.PackageRepository;
import com.hudhud.repository.SmsCountRepository;
import com.hudhud.repository.SmsRepository;
import com.hudhud.service.ClientService;
import com.hudhud.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final SmsCountRepository smsCountRepository;

//    private final SmppService smppService;


    //        private final SmsCountRepository smsCountRepository;
    private final PackageRepository packageRepository;

    @GetMapping("/sms/count")
    public ResponseEntity<?> getSmsCount(
            @RequestParam("clientId") Long clientId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Long count = smsService.getSmsCount(clientId, startDateTime, endDateTime);

        var response = new CustomResponse();
        response.setStatus("200");
        response.setMessage("Message count : " + count);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/sms/client/{clientId}")
    public ResponseEntity<?> getSmsByClient(@PathVariable String clientId) throws CustomException {
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
    public ResponseEntity<?> sendSms(@RequestBody SmsDTO smsDTO) throws InterruptedException, JSONException, ExecutionException {
        log.info("SMS REQUEST: {}", smsDTO);
        // Load credentials from the database based on the provided username
        Optional<Client> client = clientRepository.findClientByUsername(smsDTO.getUsername());

        if (!client.isPresent()) {
            var response = new JSONObject();
            response.put("status", "404");
            response.put("message", "Invalid username");
            return new ResponseEntity<>(response.toString(), HttpStatus.NOT_FOUND);
        }

        String senderId = client.get().getSenderId();

        if (client != null && client.get().getPassword().equals(smsDTO.getPassword())) {

            if (client.get().getActive() == 0) {
                var response = new JSONObject();
                response.put("status", "400");
                response.put("message", "Client is deactivated cannot send SMS , please contact your system admin");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            Optional<Packages> packageById = packageRepository.findById(client.get().getPackageId());
//            SmsCount smsCount = smsCountRepository.findByClient(client);
//            if (smsCount == null) {
//                smsCount = new SmsCount();
//                smsCount.setClient(client.get());
//                smsCount.setDate(LocalDateTime.now());
//                smsCount.setCount(1);
//            } else {
//                smsCount.setCount(smsCount.getCount() + 1);
//                if (smsCount.getCount() >= packageById.get().getNumber() + 1 && packageById.get().getType().equals("limited")) {
//                    var response = new JSONObject();
//                    response.put("status", "400");
//                    response.put("message", "You have finished your package , please subscribe new package and enjoy !");
//                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
//                }
//            }
//            smsCountRepository.save(smsCount);


//            new Application().sendTextMessage(senderId, smsDTO.getMessage(), smsDTO.getReceiverAddress());
//            var response = new JSONObject();
//            response.put("status", "200");
//            response.put("message", "Success");

            CompletableFuture<Integer> integerCompletableFuture = smsService.sendSmsAsync(smsDTO.getUsername(), smsDTO.getReceiverAddress(), smsDTO.getMessage());

            var response = new JSONObject();
            response.put("status", "200");
            response.put("message", integerCompletableFuture.get());

            var sms = new Sms();
            sms.setClientId(client.get().getId().toString());
            sms.setDate(LocalDateTime.now());
            sms.setReceiverAddress(smsDTO.getReceiverAddress());
            sms.setMessage(smsDTO.getMessage());
            sms.setSent(1);
            clientService.saveSMS(sms);

            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } else {
            // Handle authentication failure
            JSONObject response = new JSONObject();
            response.put("status", "401");
            response.put("message", "Authentication credentials not found or invalid");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // bulk sms
    // TODO: 12/13/2023  max 12 digit , min 9, either 09 , 251 , 9 validation
    @PostMapping("/bulk")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("username") String username,
                                             @RequestParam("password") String password) {
        var response = new CustomResponse();

        Optional<Client> client = clientRepository.findClientByUsername(username);

        if (!client.isPresent()) {
            response.setStatus("404");
            response.setMessage("Invalid username");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (client != null && client.get().getPassword().equals(password)) {
            if (file.isEmpty()) {
                response.setStatus("400");
                response.setMessage("please upload a file");
                return ResponseEntity.badRequest().body(response);
            }

            if (!smsService.hasExcelFormat(file)) {
                response.setStatus("400");
                response.setMessage("Invalid format please upload excel file");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            try {
                smsService.save(file);
                response.setStatus("200");
                response.setMessage("Uploaded the file successfully : " + file.getOriginalFilename());

                return ResponseEntity.status(HttpStatus.OK).body(response);
            } catch (Exception e) {

                response.setStatus("500");
                response.setMessage("Failed to upload: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } else {
            // Handle authentication failure
            response.setStatus("401");
            response.setMessage("Authentication credentials not found or invalid");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


    }
}
