package com.hudhud.controller;


import com.hudhud.model.Client;
import com.hudhud.model.dto.ClientDTO;
import com.hudhud.model.dto.CustomResponse;
import com.hudhud.model.dto.StatusDTO;
import com.hudhud.repository.ClientRepository;
import com.hudhud.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    @PostMapping("client/registration")
    public ResponseEntity<?> registerClient(@RequestBody ClientDTO clientDTO) throws Exception {
        Client client = clientService.createClient(clientDTO);
        var customResponse = new CustomResponse();
        customResponse.setStatus("200");
        customResponse.setMessage("Client Registered successfully");
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    @PutMapping("client/deactivate/{clientId}")
    public ResponseEntity<?> registerClient(@PathVariable Long clientId, @RequestBody StatusDTO statusDTO) throws Exception {

        log.info("STATUS :{}", statusDTO.getStatus());
        clientService.deactivateClient(clientId, statusDTO.getStatus());
        var customResponse = new CustomResponse();
        customResponse.setStatus("200");
        customResponse.setMessage("Client status changed successfully");
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

}
