package com.hudhud.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SlackRequest {

    private String message;
    private String url;
}
