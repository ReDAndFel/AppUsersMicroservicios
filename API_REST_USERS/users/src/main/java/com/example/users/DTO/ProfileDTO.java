package com.example.users.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ProfileDTO {
    int id;
    String email;
    String password;
    String pagina_personal;
    String apodo;
    Boolean contacto_publico;
    String direccion;
    String biografia;
    String organizacion;
    String pais;
    List<RedesDTO> redes;
}
