package com.spring.java.dto;


// DTO de Entrada: O que o cliente envia

public record UsuarioRequestDTO(String nome, String email, String senha) {
}

