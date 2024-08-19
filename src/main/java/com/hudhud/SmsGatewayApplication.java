package com.hudhud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@EnableScheduling
@SpringBootApplication(exclude = SqlInitializationAutoConfiguration.class)
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SmsGatewayApplication {


//    @Scheduled(cron = "0 51 17 * * *")
//    public void testScheduler() {
//        System.out.println("TEST SCHEDULER KICKED OFF");
//    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(SmsGatewayApplication.class, args);
    }

}
