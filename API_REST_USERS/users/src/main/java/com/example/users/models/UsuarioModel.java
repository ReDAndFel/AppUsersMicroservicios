package com.example.users.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class UsuarioModel {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "id")
    private Integer id;

    @NonNull
    @Column(nullable = false, name = "email")
    private String email;
    
    @NonNull
    @Column(nullable = false, length = 12, name = "contrasena")
    private String contrasena;  
    
}

