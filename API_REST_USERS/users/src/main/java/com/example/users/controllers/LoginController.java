package com.example.users.controllers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.users.models.UsuarioModel;
import com.example.users.models.dtos.LoginDTO;
import com.example.users.repositories.UsuarioRepository;
import com.example.users.services.JwtInterface;

@CrossOrigin("*")
@RestController
public class LoginController {

    @Autowired
    private JwtInterface jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO login) {

        String email = login.getEmail();
        String password = login.getPassword();

        System.out.println("el email manado es " + email);

        if (login != null && !email.isEmpty() && !password.isEmpty()) {
            UsuarioModel userAux = usuarioRepository.obtenerUsuarioPorEmail(email);

            if (userAux != null && userAux.getContrasena().equals(login.getPassword())) {
                String token = jwtUtil.generateToken(email);
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
