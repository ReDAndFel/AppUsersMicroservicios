package com.example.users.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.users.DTO.LogDTO;
import com.example.users.models.UsuarioModel;
import com.example.users.models.dtos.LoginDTO;
import com.example.users.repositories.UsuarioRepository;
import com.example.users.services.JwtInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;

@CrossOrigin("*")
@RestController
public class LoginController {

    @Autowired
    private JwtInterface jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${nats.server.tema}")
    private String natsTema;

    @Autowired
    private Connection natsConnection;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO login) throws JsonProcessingException {

        String email = login.getEmail();
        String password = login.getPassword();

        System.out.println("el email manado es " + email);

        if (login != null && !email.isEmpty() && !password.isEmpty()) {
            UsuarioModel userAux = usuarioRepository.obtenerUsuarioPorEmail(email);

            if (userAux != null && userAux.getContrasena().equals(login.getPassword())) {
                String token = jwtUtil.generateToken(email);
                System.out.println("tema a enviar mensaje es: " + natsTema);
                LogDTO logDTO = new LogDTO("Login", "Api_users", "LoginController", "Usuario logueado",
                        "El usuario con correo " + login.getEmail() + " se logueo");
                ObjectMapper objectMapper = new ObjectMapper();
                String logJson = objectMapper.writeValueAsString(logDTO);
                natsConnection.publish(natsTema, logJson.getBytes());
                System.out.println("Mensaje enviado!");

                return ResponseEntity.ok(token);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se encontró usuario con esas credenciales");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Los atributos 'usuario' y 'clave' son obligatorios");
        }
    }
}
