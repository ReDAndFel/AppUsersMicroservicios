package com.example.users.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class LogDTO {
    String tipo;
    String aplicacion;
    String clase_modulo;
    String resumen;
    String descripcion;
}
