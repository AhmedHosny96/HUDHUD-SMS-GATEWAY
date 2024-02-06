package com.hudhud.service;

import com.hudhud.exception.CustomException;
import com.hudhud.model.Client;
import com.hudhud.model.Sms;
import com.hudhud.model.dto.ClientDTO;
import com.hudhud.repository.ClientRepository;
import com.hudhud.repository.SmsRepository;
import com.hudhud.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ClientService {


    private final ClientRepository clientRepository;

    private final SmsRepository smsRepository;

    private final UtilService utilService;

    public List<Client> getClients() {
        return clientRepository.findAll();
    }


    public Client createClient(ClientDTO clientDTO) throws Exception {

        Optional<Client> clientByEmail = clientRepository.findClientByEmail(clientDTO.getEmail());

        Optional<Client> clientByName = clientRepository.findClientByName(clientDTO.getName());

        Optional<Client> clientBySenderId = clientRepository.findClientBySenderId(clientDTO.getSenderId());

        if (clientByEmail.isPresent()) {
            throw new CustomException("Email is taken");
        }
        if (clientByName.isPresent()) {
            throw new CustomException("Client Name is taken");
        }
        if (clientBySenderId.isPresent()) {
            throw new CustomException("Client Sender Id is taken");
        }

        var client = new Client();

        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setSenderId(clientDTO.getSenderId());
        client.setUsername(utilService.generateRandomUsername(8));
        client.setPassword(UUID.randomUUID().toString());
        client.setActive(1);
        client.setPackageId(clientDTO.getPackageId());
//        client.setPremium(clientDTO.getClientType().equals("Unlimited") ? true : false);
        clientRepository.save(client);
        return client;
    }

    // function to deactivate clients
    public void deactivateClient(Long clientId, String status) throws CustomException {
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (!optionalClient.isPresent()) {
            throw new CustomException("Client doesn't exist");
        }

        Client client = optionalClient.get();
        if ("active".equals(status)) { // Use .equals() for string comparison
            client.setActive(1);
        } else {
            client.setActive(0);
        }
        clientRepository.save(client);
    }


    public Sms saveSMS(Sms sms) {
        return smsRepository.save(sms);
    }
}
