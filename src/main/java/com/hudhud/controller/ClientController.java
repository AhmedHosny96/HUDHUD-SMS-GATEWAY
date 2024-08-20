package com.hudhud.controller;


import com.hudhud.model.Client;
import com.hudhud.model.dto.ClientDTO;
import com.hudhud.model.dto.CustomResponse;
import com.hudhud.model.dto.StatusDTO;
import com.hudhud.repository.ClientRepository;
import com.hudhud.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    private final ClientRepository clientRepository;

    @GetMapping("/client")
    public ResponseEntity<?> getClients() {
        List<Client> clients = clientService.getClients();
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    @GetMapping("/client/reset")
    public ResponseEntity<?> resetClientPassword(@RequestParam() String username) {
        CustomResponse customResponse = clientService.resetClientPassword(username);
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }


    @PostMapping("client/registration")
    public ResponseEntity<?> registerClient(@RequestBody ClientDTO clientDTO, HttpServletRequest request) throws Exception {

        Client client = clientService.createClient(clientDTO);
        var customResponse = new CustomResponse();
        customResponse.setStatus(200);
        customResponse.setMessage("Client Registered successfully");
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    @PutMapping("client/status/{clientId}")
    public ResponseEntity<?> updateClientStatus(@PathVariable Long clientId, @RequestParam Boolean activate) throws Exception {


        clientService.updateClientStatus(clientId, activate);

        CustomResponse customResponse = new CustomResponse();
        customResponse.setStatus(200);
        customResponse.setMessage(String.format("Client status changed to %s successfully", activate ? "active" : "inactive"));
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

}
