package com.example.users.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.users.DTO.LogDTO;
import com.example.users.DTO.ProfileDTO;
import com.example.users.models.UsuarioModel;
import com.example.users.models.dtos.MessageDTO;
import com.example.users.repositories.UsuarioRepository;
import com.example.users.services.JwtInterface;
import com.example.users.services.UsuarioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;


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
        private UsuarioRepository usuarioRepository;

        @Autowired
        private JwtInterface jwtUtil;

        @PutMapping("/{id}")
        public ResponseEntity<MessageDTO> actualizarUsuario(HttpServletRequest request, @RequestBody ProfileDTO profileDTO, @PathVariable int id)
                        throws JsonProcessingException {
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        String tokenHeader = authorizationHeader.substring(7);
                        if (jwtUtil.isTokenValid(tokenHeader)) {
                                UsuarioModel usuario = usuarioService.obtenerUsuarioPorId(id);
                                usuario.setEmail(profileDTO.getEmail());
                                usuarioService.guardarUsuario(usuario);
                                LogDTO logDTO = new LogDTO("Actualizar", "Api_users", "UsuarioController",
                                                "Usuario actualizado",
                                                "El usuario con id " + usuario.getId() + " se actualizó");
                                ObjectMapper objectMapper = new ObjectMapper();
                                String logJson = objectMapper.writeValueAsString(logDTO);
                                natsConnection.publish(natsTema, logJson.getBytes());
                                return ResponseEntity.status(HttpStatus.CREATED)
                                                .body(new MessageDTO(HttpStatus.CREATED, false,
                                                                "Usuario actualizado correctamente"));
                        } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                                                                "El token no es valido para el correo proporcionado"));
                        }
                } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                                                        "Se requiere un token JWT en la cabecera Authorization"));
                }
        }

        @PostMapping("/")
        public ResponseEntity<MessageDTO> crearUsuario(@RequestBody ProfileDTO profileDTO)
                        throws JsonProcessingException {
                UsuarioModel usuario = new UsuarioModel();
                usuario.setId(profileDTO.getId());
                usuario.setEmail(profileDTO.getEmail());
                usuario.setContrasena(profileDTO.getPassword());
                usuarioService.guardarUsuario(usuario);
                LogDTO logDTO = new LogDTO("Registro", "Api_users", "UsuarioController", "Usuario logueado",
                                "El usuario con id " + usuario.getId() + " se creo");
                ObjectMapper objectMapper = new ObjectMapper();
                String logJson = objectMapper.writeValueAsString(logDTO);
                natsConnection.publish(natsTema, logJson.getBytes());
                ObjectMapper objetoMapper = new ObjectMapper();
                String profileJson = objetoMapper.writeValueAsString(profileDTO);
                natsConnection.publish("profile", profileJson.getBytes());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new MessageDTO(HttpStatus.CREATED, false, "Usuario creado correctamente"));
        }

        @GetMapping("/{id}")
        public ResponseEntity<MessageDTO> obtenerUsuarioPorId(@PathVariable Integer id) {
                return ResponseEntity.status(HttpStatus.OK)
                                .body(new MessageDTO(HttpStatus.OK, false, usuarioService.obtenerUsuarioPorId(id)));
        }

        @GetMapping("/list")
        public Page<UsuarioModel> obtenerUsuarios(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
            return usuarioRepository.findAll(pageable);  /* GET /users?page=0&size=10&sort=nombre,asc */
        }
        

        @DeleteMapping("/{id}")
        public ResponseEntity<MessageDTO> eliminarUsuario(HttpServletRequest request, @PathVariable Integer id)
                        throws JsonProcessingException {
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        String tokenHeader = authorizationHeader.substring(7);
                        if (jwtUtil.isTokenValid(tokenHeader)) {
                                usuarioService.eliminarUsuario(id);
                                LogDTO logDTO = new LogDTO("Eliminacion", "Api_users", "UsuarioController",
                                                "Usuario logueado",
                                                "El usuario con id " + id + " se eliminó");
                                ObjectMapper objectMapper = new ObjectMapper();
                                String logJson = objectMapper.writeValueAsString(logDTO);
                                natsConnection.publish(natsTema, logJson.getBytes());
                                return ResponseEntity.status(HttpStatus.OK)
                                                .body(new MessageDTO(HttpStatus.OK, false,
                                                                "Usuario eliminado correctamente"));
                        } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                                                                "El token no es valido para el correo proporcionado"));
                        }
                } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                                                        "Se requiere un token JWT en la cabecera Authorization\""));
                }

        }

        @PutMapping("/actualizarContraseña/{id}")
        public ResponseEntity<MessageDTO> actualizarContraseña(HttpServletRequest request, @PathVariable Integer id,
                        @RequestBody UsuarioModel usuario) throws JsonProcessingException {
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        String tokenHeader = authorizationHeader.substring(7);
                        if (jwtUtil.isTokenValid(tokenHeader)) {
                                usuarioService.cambiarContraseña(id, usuario);
                                LogDTO logDTO = new LogDTO("Actualizacion", "Api_users", "UsuarioController",
                                                "Usuario logueado",
                                                "El usuario con id " + usuario.getId() + " actualizó su contraseña");
                                ObjectMapper objectMapper = new ObjectMapper();
                                String logJson = objectMapper.writeValueAsString(logDTO);
                                natsConnection.publish(natsTema, logJson.getBytes());
                                return ResponseEntity.status(HttpStatus.OK)
                                                .body(new MessageDTO(HttpStatus.OK, false,
                                                                "contraseña actualizada correctamente"));
                        } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                                                                "El token no es valido para el correo proporcionado"));
                        }
                } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new MessageDTO(HttpStatus.UNAUTHORIZED, true,
                                                        "Se requiere un token JWT en la cabecera Authorization"));
                }
        }

        @PostMapping("/recuperarContrasenia/{email}")
        public ResponseEntity<MessageDTO> recuperarContraseña(@PathVariable String email) throws Exception {
                LogDTO logDTO = new LogDTO("Recuperacion", "Api_users", "UsuarioController", "Usuario logueado",
                                "El usuario con el email " + email + " pidio una recuperacion de su contraseña");
                ObjectMapper objectMapper = new ObjectMapper();
                String logJson = objectMapper.writeValueAsString(logDTO);
                natsConnection.publish(natsTema, logJson.getBytes());
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                                .body(new MessageDTO(HttpStatus.OK, false, usuarioService.recuperarContraseña(email)));
        }
}
