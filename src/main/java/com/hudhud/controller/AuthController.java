package com.hudhud.controller;

import com.hudhud.config.JwtConfig;
import com.hudhud.exception.CustomException;
import com.hudhud.model.dto.AuthRequestDto;
import com.hudhud.model.dto.AuthResponse;
import com.hudhud.model.dto.TokenBody;
import com.hudhud.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class AuthController {

    private final JwtConfig jwtService;

    private final ClientRepository userRepository;

//    private final RoleRepo roleRepo;

    private final AuthenticationManager authenticationManager;

    @SneakyThrows
    @PostMapping(value = "/login")
    public ResponseEntity<?> auth(@RequestBody AuthRequestDto authRequest) {
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.username(), authRequest.password()

        ));


        var currentUser = userRepository.findClientByUsername(authRequest.username()).get();

        if (currentUser.getStatus() == 0) {
            throw new CustomException("Client deactivated please contact your service provider");
        }

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("invalid username or password");
        }

        var tokenBody = new TokenBody(currentUser.getUsername(), currentUser.getId(), currentUser.getStatus());

        String token = jwtService.generateToken(tokenBody);

        var customResponse = new AuthResponse(
                200,
                "success",
                authRequest.username(),
                token
        );
        return new ResponseEntity<>(customResponse, HttpStatus.OK);


    }
}
