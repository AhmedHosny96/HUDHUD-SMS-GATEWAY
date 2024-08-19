package com.hudhud.model.dto;

import com.hudhud.model.SmsHistory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReportSmsCountResp {

    private int status;
    private String message;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalSms;
    private List<SmsHistory> smsCounts;
}
