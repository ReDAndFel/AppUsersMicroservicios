package com.example.users.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.users.models.UsuarioModel;
import com.example.users.models.dtos.MessageDTO;
import com.example.users.services.JwtInterface;
import com.example.users.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin("*")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    private JwtInterface jwtUtil;

    @PostMapping
    public ResponseEntity<MessageDTO> crearUsuario(@RequestBody UsuarioModel usuario) {
        usuarioService.guardarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageDTO(HttpStatus.CREATED, false, "Usuario creado correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> obtenerUsuarioPorId(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageDTO(HttpStatus.OK, false, usuarioService.obtenerUsuarioPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageDTO> actualizarUsuario(HttpServletRequest request, @PathVariable Integer id,
            @RequestBody UsuarioModel usuario) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String tokenHeader = authorizationHeader.substring(7);
            if (jwtUtil.isTokenValid(tokenHeader)) {
                usuarioService.actualizarUsuario(id, usuario);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new MessageDTO(HttpStatus.OK, false, "Usuario actualizado correctamente"));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDTO> eliminarUsuario(HttpServletRequest request, @PathVariable Integer id) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String tokenHeader = authorizationHeader.substring(7);
            if (jwtUtil.isTokenValid(tokenHeader)) {
                usuarioService.eliminarUsuario(id);
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
            @RequestBody UsuarioModel usuario) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String tokenHeader = authorizationHeader.substring(7);
            if (jwtUtil.isTokenValid(tokenHeader)) {
                usuarioService.cambiarContraseña(id, usuario);
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

        return ResponseEntity.status(HttpStatus.OK).body(new MessageDTO(HttpStatus.OK, false, usuarioService.recuperarContraseña(email)));
    }
}
