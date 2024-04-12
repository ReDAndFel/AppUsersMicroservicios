package com.example.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.users.models.UsuarioModel;
import com.example.users.models.dtos.EmailDTO;
import com.example.users.repositories.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    JwtInterface jwtInterface;
    @Autowired
    EmailService emailService;

    public UsuarioModel guardarUsuario(UsuarioModel usuario) {
        return usuarioRepository.save(usuario);
    }

    public UsuarioModel obtenerUsuarioPorId(int id) {
        Optional<UsuarioModel> optionalUsuario = usuarioRepository.findById(id);
        return optionalUsuario.get();
    }

    public UsuarioModel actualizarUsuario(int id, UsuarioModel usuario) {
        UsuarioModel usuarioAux = usuarioRepository.findById(id).get();
        usuarioAux.setFirstName(usuario.getFirstName());
        usuarioAux.setLastName(usuario.getLastName());
        return usuarioRepository.save(usuarioAux);
    }

    public UsuarioModel cambiarContraseña(int id, UsuarioModel usuario) {
        UsuarioModel usuarioAux = usuarioRepository.findById(id).get();
        usuarioAux.setContrasena(usuario.getContrasena());
        return usuarioRepository.save(usuarioAux);
    }

    public String recuperarContraseña(String email) throws Exception {
        String token = jwtInterface.generateToken(email);
        emailService.sendEmail(new EmailDTO("Recuperacion de Contraseña", "Su token de recuperacion es: " + token, email));
        return "Correo de recuperación enviado";
    }

    public boolean eliminarUsuario(Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
