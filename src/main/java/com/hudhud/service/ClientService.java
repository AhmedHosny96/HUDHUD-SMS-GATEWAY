package com.hudhud.service;

import com.hudhud.exception.CustomException;
import com.hudhud.model.Client;
import com.hudhud.model.Sms;
import com.hudhud.model.dto.ClientDTO;
import com.hudhud.model.dto.CustomResponse;
import com.hudhud.repository.ClientRepository;
import com.hudhud.repository.SmsRepository;
import com.hudhud.utils.UtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ClientService {


    private final ClientRepository clientRepository;

    private final SmsRepository smsRepository;

    private final PasswordEncoder passwordEncoder;

    private final NotificationService notificationService;

    private final UtilService utilService;

    public Client getClientById(Long id) {
        return clientRepository.findById(id).get();
    }

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

        String generatedPassword = utilService.generatePassword();

        String generatedUsername = utilService.generateRandomUsername();

        log.info("USERNAME : {} , PASSWORD : {}", generatedUsername, generatedPassword);

        var client = new Client();

        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setSenderId(clientDTO.getSenderId());
        client.setUsername(generatedUsername);
        client.setPassword(passwordEncoder.encode(generatedPassword));
        client.setStatus(1);
        client.setPackageId(clientDTO.getPackageId());
//        client.setPremium(clientDTO.getClientType().equals("Unlimited") ? true : false);
        clientRepository.save(client);

        Context context = new Context();
        context.setVariable("name", client.getName());
        context.setVariable("username", generatedUsername);
        context.setVariable("otp", generatedPassword);
        context.setVariable("currentYear", LocalDate.now().getYear());


        notificationService.sendMail(clientDTO.getEmail(), "Hudhud Cloud", "email-template", context);


        log.info("EMAIL CONTEXT : {}", context);

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
            client.setStatus(1);
        } else {
            client.setStatus(0);
        }
        clientRepository.save(client);
    }


    public Sms saveSMS(Sms sms) {
        return smsRepository.save(sms);
    }


    // rest client password
    public CustomResponse resetClientPassword(String username) {

        Optional<Client> clientByUsername = clientRepository.findClientByUsername(username);

        var customResponse = new CustomResponse();

        if (!clientByUsername.isPresent()) {

            customResponse.setStatus(400);
            customResponse.setMessage("Client with username not found");

            return customResponse;
        }

        Client existingClient = clientByUsername.get();

        String generatePassword = utilService.generatePassword();

        log.info("New password : {}", generatePassword);

        existingClient.setPassword(passwordEncoder.encode(generatePassword));

        clientRepository.save(existingClient);

        Context context = new Context();
        context.setVariable("name", existingClient.getName());
        context.setVariable("username", username);
        context.setVariable("otp", generatePassword);
        context.setVariable("currentYear", LocalDate.now().getYear());

        notificationService.sendMail(existingClient.getEmail(), "Password reset", "email-template.html", context);

        customResponse.setStatus(200);
        customResponse.setMessage("Password reset successful sent via email");

        return customResponse;

    }
}
