package com.example.users.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.users.DTO.LogDTO;
import com.example.users.models.UsuarioModel;
import com.example.users.models.dtos.MessageDTO;
import com.example.users.services.JwtInterface;
import com.example.users.services.UsuarioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin("*")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Value("${nats.server.tema}")
    private String natsTema;

    @Autowired
    private Connection natsConnection;

    @Autowired
    UsuarioService usuarioService;
    @Autowired
    private JwtInterface jwtUtil;

    @PostMapping
    public ResponseEntity<MessageDTO> crearUsuario(@RequestBody UsuarioModel usuario) throws JsonProcessingException {
        usuarioService.guardarUsuario(usuario);
        LogDTO logDTO = new LogDTO("Registro", "Api_users", "UsuarioController", "Usuario logueado",
                "El usuario con id " + usuario.getId() + " se creo");
        ObjectMapper objectMapper = new ObjectMapper();
        String logJson = objectMapper.writeValueAsString(logDTO);
        natsConnection.publish(natsTema, logJson.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageDTO(HttpStatus.CREATED, false, "Usuario creado correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> obtenerUsuarioPorId(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageDTO(HttpStatus.OK, false, usuarioService.obtenerUsuarioPorId(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDTO> eliminarUsuario(HttpServletRequest request, @PathVariable Integer id)
            throws JsonProcessingException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String tokenHeader = authorizationHeader.substring(7);
            if (jwtUtil.isTokenValid(tokenHeader)) {
                usuarioService.eliminarUsuario(id);
                LogDTO logDTO = new LogDTO("Eliminacion", "Api_users", "UsuarioController", "Usuario logueado",
                        "El usuario con id " + id + " se eliminó");
                ObjectMapper objectMapper = new ObjectMapper();
                String logJson = objectMapper.writeValueAsString(logDTO);
                natsConnection.publish(natsTema, logJson.getBytes());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new MessageDTO(HttpStatus.OK, false, "Usuario eliminado correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                        "El token no es valido para el correo proporcionado"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                            "Se requiere un token JWT en la cabecera Authorization\""));
        }

    }

    @PutMapping("actualizarContraseña/{id}")
    public ResponseEntity<MessageDTO> actualizarContraseña(HttpServletRequest request, @PathVariable Integer id,
            @RequestBody UsuarioModel usuario) throws JsonProcessingException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String tokenHeader = authorizationHeader.substring(7);
            if (jwtUtil.isTokenValid(tokenHeader)) {
                usuarioService.cambiarContraseña(id, usuario);
                LogDTO logDTO = new LogDTO("Actualizacion", "Api_users", "UsuarioController", "Usuario logueado",
                        "El usuario con id " + usuario.getId() + " actualizó su contraseña");
                ObjectMapper objectMapper = new ObjectMapper();
                String logJson = objectMapper.writeValueAsString(logDTO);
                natsConnection.publish(natsTema, logJson.getBytes());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new MessageDTO(HttpStatus.OK, false, "contraseña actualizada correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                        "El token no es valido para el correo proporcionado"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                            "Se requiere un token JWT en la cabecera Authorization"));
        }
    }

    @PostMapping("recuperarContraseña/{email}")
    public ResponseEntity<MessageDTO> recuperarContraseña(@PathVariable String email) throws Exception {
        LogDTO logDTO = new LogDTO("Recuperacion", "Api_users", "UsuarioController", "Usuario logueado",
                "El usuario con el email " + email + " pidio una recuperacion de su contraseña");
        ObjectMapper objectMapper = new ObjectMapper();
        String logJson = objectMapper.writeValueAsString(logDTO);
        natsConnection.publish(natsTema, logJson.getBytes());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageDTO(HttpStatus.OK, false, usuarioService.recuperarContraseña(email)));
    }
}
