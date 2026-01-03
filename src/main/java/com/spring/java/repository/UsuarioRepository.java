package com.spring.java.repository;

import com.spring.java.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // O Spring cria a query automaticamente só pelo nome do método!
    boolean existsByEmail(String email);
}







