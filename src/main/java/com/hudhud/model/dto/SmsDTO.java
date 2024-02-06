package com.hudhud.model.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsDTO {

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @Pattern(regexp = "^2519[0-9]{8}$", message = "Receiver address must start with 2519 and be 12 digits long.")
    @NotEmpty
    private String receiverAddress;
    @NotEmpty
    private String message;


}
