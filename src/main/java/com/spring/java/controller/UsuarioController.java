package com.spring.java.controller;


import com.spring.java.dto.UsuarioRequestDTO;
import com.spring.java.dto.UsuarioResponseDTO;

import com.spring.java.repository.UsuarioRepository;
import com.spring.java.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*
@PostMapping (Criar)
@GetMapping (Listar todos)
@GetMapping("/{id}") (Buscar um único) -> Adicionado agora!
@PutMapping("/{id}") (Atualizar)
@DeleteMapping("/{id}") (Deletar)
*/

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    @Autowired
    private UsuarioService service;



    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> salvar(@RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable(name = "id") Long id) {
        UsuarioResponseDTO dto = service.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> alterar(@PathVariable Long id, @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable(value = "id") Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

}

