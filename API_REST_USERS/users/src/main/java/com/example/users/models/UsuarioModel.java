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
    @Column(nullable = false, length = 12, name = "first_Name")
    private String firstName;

    @NonNull
    @Column(nullable = false, length = 12, name = "last_Name")
    private String lastName;
    
    @NonNull
    @Column(nullable = false, name = "email")
    private String email;
    
    @NonNull
    @Column(nullable = false, length = 12, name = "contrasena")
    private String contrasena;  

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;
    
}

