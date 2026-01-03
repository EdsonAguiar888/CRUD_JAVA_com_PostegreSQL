package com.spring.java.dto;



// DTO de Saída: O que o cliente recebe (Sem a senha!)

public record UsuarioResponseDTO(Long id, String nome, String email) {
}
