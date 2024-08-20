package com.hudhud.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SmsCountDetail {

    private int status;
    private String message;
    private Long clientId;
    private String clientName;
    private Long totalCount;
    private LocalDate date;
}
