package com.example.users.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.users.models.UsuarioModel;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer>{
   
    @Query("select u from UsuarioModel u")
    Page<UsuarioModel> listarUsuarios(Pageable pageable);

    @Query("select u from UsuarioModel u where u.email = :email")
    UsuarioModel obtenerUsuarioPorEmail(String email);

    
}