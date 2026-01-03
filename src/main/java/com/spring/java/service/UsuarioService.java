package com.spring.java.service;

import com.spring.java.dto.UsuarioRequestDTO;
import com.spring.java.dto.UsuarioResponseDTO;
import com.spring.java.model.Usuario;
import com.spring.java.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


/*
@PostMapping (Criar)
@GetMapping (Listar todos)
@GetMapping("/{id}") (Buscar por id)
@PutMapping("/{id}") (Atualizar)
@DeleteMapping("/{id}") (Deletar)
*/

@Service
public class UsuarioService {

    @Autowired
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

//======================================================================================================================

    // #Cria Novo Usuario
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {

        // Regra de Negócio: Verificar se email já existe
        if (repository.existsByEmail((dto.email()))) {
            throw new RuntimeException("Email já existe!!");
        }

        // Converte DTO para Model
        Usuario novoUsuario = new Usuario();

        novoUsuario.setNome(dto.nome());
        novoUsuario.setEmail(dto.email());
        novoUsuario.setSenha(dto.senha()); // Aqui você aplicaria um BCrypt para encriptar

        repository.save(novoUsuario); //O Repository envia para o Postgres


        // Retorna o DTO de saída (escondendo a senha)
        return new UsuarioResponseDTO(novoUsuario.getId(), novoUsuario.getNome(), novoUsuario.getEmail());
    }

// =====================================================================================================================

    //  #Listar Todos
    public List<UsuarioResponseDTO> listarTodos() {
        // Busca do banco
        List<Usuario> usuarios = repository.findAll();

        // Converte de Model para DTO manualmente para não ter erro de Stream
        return usuarios.stream()
                .map(u -> new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail()))
                .toList();
    }

//==================================================================================================================

    // Buscar Por Id
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));

        return new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail());
    }

//======================================================================================================================

    // UPDATE
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        // Aqui você poderia atualizar a senha também se desejar
        repository.save(usuario);
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

//=================================================================================================================

    //Delele
    public void deletar(Long id) {
        repository.deleteById(id);

    }

//======================================================================================================================
}








