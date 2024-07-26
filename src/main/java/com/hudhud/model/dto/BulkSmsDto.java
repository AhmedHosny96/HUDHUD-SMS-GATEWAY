package com.hudhud.model.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class BulkSmsDto {
    private MultipartFile file;
    private String username;
    private String password;
}
