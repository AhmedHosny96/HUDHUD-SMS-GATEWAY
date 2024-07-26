package com.hudhud;

import com.hudhud.model.Client;
import com.hudhud.model.SmsCount;
import com.hudhud.repository.SmsCountRepository;
import com.hudhud.repository.SmsRepository;
import com.hudhud.service.ClientService;
import com.hudhud.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class WorkerService {

    @Scheduled(cron = "0 32 20 * * *")
    public void testScheduler() {
        System.out.println("CRON TEST SCHEDULER KICKED OFF");
    }

//    @Scheduled(fixedRate = 1000L)
//    public void fix() {
//        System.out.println(" FIXED TEST SCHEDULER KICKED OFF");
//    }


//    private final SmsCountRepository smsCountRepository;
//
//    private final SmsRepository smsRepository;
//
//    private final ClientService clientService;
//
//    private final MonitoringService monitoringService;
//
//    private final Long SAHAY_CLIENT_ID = 10002L;
//    private final Long HALAL_PAY_CLIENT_ID = 13L;
//
//
//    // daily slack push for all client sent sms at 8:00 AM morning
//
//    @Scheduled(cron = "0 11 17 * * *", zone = "GMT+3")
//    public void transferSmsToSmsCount() {
//        log.info("Automated service kick off ================");
//
//        try {
//            Client sahay = clientService.getClientById(Long.valueOf(SAHAY_CLIENT_ID));
//            Client halal = clientService.getClientById(Long.valueOf(HALAL_PAY_CLIENT_ID));
//
//            long sahayCount = smsRepository.countByClientId(SAHAY_CLIENT_ID);
//
//     /*       SmsCount sahaySmsCount = new SmsCount();
//            sahaySmsCount.setCount(Long.valueOf(sahaySms.stream().count()));
//            sahaySmsCount.setCreatedAt(LocalDateTime.now());
//            sahaySmsCount.setClientId(Long.valueOf(sahaySms.get(0).getClientId()));
//
//            smsCountRepository.save(sahaySmsCount);
//*/
//            long halalByCount = smsRepository.countByClientId(HALAL_PAY_CLIENT_ID);
//
//            SmsCount halalPaySmsCount = new SmsCount();
//            halalPaySmsCount.setCount(halalByCount);
//            halalPaySmsCount.setCreatedAt(LocalDateTime.now());
//            halalPaySmsCount.setClientId(halal.getId());
//
//            smsCountRepository.save(halalPaySmsCount);
//
//            // Get the client names
//
//            String sahaySenderId = sahay.getSenderId();
//            String halalSenderId = halal.getSenderId();
//
//            // Create the Slack message
//            String reportDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
////            String sahaySmsCountString = String.valueOf(sahaySmsCount.getCount());
//            String halalPaySmsCountString = String.valueOf(halalPaySmsCount.getCount());
//
//            String slackMessage = "" +
//                    "Sms Daily report\n" +
//                    "================\n" +
//                    "Date : " + reportDate + "\n\n" +
//                    "Client               Total SMS\n" +
//                    "-----------------------------\n" +
//                    sahaySenderId + "               " + "" + "\n" +
//                    halalSenderId + "               " + halalPaySmsCountString + "\n";
//
//            // Send to Slack
//            log.info("Sending message to Slack: " + slackMessage);
//            monitoringService.sendToSlack(slackMessage);
//            log.info("Message sent to Slack successfully.");
//
//            log.info("Deleted SMS records for sahay and halal clients.");
//        } catch (Exception e) {
//            log.error("An error occurred while transferring SMS to SMS Count and sending to Slack", e);
//        }
//    }
}
