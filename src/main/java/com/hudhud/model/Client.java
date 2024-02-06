package com.hudhud.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String senderId;
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    @Size(min = 6, max = 20)
    private String username;
    // You can generate a password using some logic or a library here
    private String password;
    private int active;
    private boolean isPremium;
    private int packageId;

}
