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
    
    public UsuarioModel cambiarContraseña(int id, UsuarioModel usuario) {
        UsuarioModel usuarioAux = usuarioRepository.findById(id).get();
        usuarioAux.setContrasena(usuario.getContrasena());
        return usuarioRepository.save(usuarioAux);
    }

    public String recuperarContraseña(String email) throws Exception {
        try {
            String token = jwtInterface.generateToken(email);
            emailService.sendMail(new EmailDTO(email, "Recuperación de contraseña "+token, token));
            return "Correo de recuperación enviado";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al enviar el correo";
        }
        
    }

    //Cambiar para que elimine en esta api y el perfil de la api perfiles
    public boolean eliminarUsuario(Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
