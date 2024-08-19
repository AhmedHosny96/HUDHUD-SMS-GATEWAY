package com.hudhud.model.dto;

import com.hudhud.model.SmsHistory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SmsCountResponse {

    private int status;
    private String message;
    private List<SmsHistory> smsCount;
}
