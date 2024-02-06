package com.hudhud.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {

    public String status;
    public String message;
    public String path;
    public LocalDateTime localDateTime;
}
