package com.hudhud.service;


import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetAddress;


@Service
@RequiredArgsConstructor
public class MonitoringService {

    @Value("${slack.webhook-url}")
    private String SLACK_URL;

    private final RestTemplate restTemplate;

    private static final int TIME_OUT = 60000;

    public boolean isReachableViaPing(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return inetAddress.isReachable(TIME_OUT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendToSlack(String message) {
        var request = new JSONObject();
        request.put("text", "<!channel> " + message);
        restTemplate.postForEntity(SLACK_URL, request.toString(), String.class);
    }

}
