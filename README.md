---

# 🚀 Projeto CRUD Java 17 + Spring Boot & PostgreSQL

Este repositório contém um sistema completo de gerenciamento de utilizadores, focado na arquitetura de camadas e nas melhores práticas de desenvolvimento com Java 17.

---

## 📖 Para entender como estas camadas funcionam

Imagine o funcionamento de um **restaurante bem organizado**. Cada camada tem um papel específico para garantir que o "pedido" do cliente seja entregue corretamente, sem bagunça na cozinha.

### 1. O que é cada camada?

* **Controller (O Garçom):** É a porta de entrada. Ele recebe o pedido do cliente, verifica se está completo e entrega a resposta final. Ele não cozinha; ele apenas gerencia a comunicação.
* **DTO - Data Transfer Object (O Cardápio/Comanda):** É um objeto simples usado apenas para transportar dados. Serve para filtrar o que entra e o que sai, garantindo que o cliente não veja dados sensíveis (como senhas) ou envie dados desnecessários.
* **Service (O Chefe de Cozinha):** É onde a "mágica" acontece. Aqui fica a **lógica de negócio**. Ele decide se o pedido pode ser feito, calcula descontos, verifica estoque e coordena as tarefas pesadas.
* **Model/Entity (A Receita/Ingredientes):** Representa a estrutura dos dados no banco de dados. É o formato real de como a informação é guardada "na despensa".

### 2. O Passo a Passo do Fluxo

Vamos simular a criação de um novo usuário em um sistema:

1. **Passo 1: Chegada no Controller** – O cliente envia Nome, Email e Senha. O Controller recebe via **DTO**. (Evita que o utilizador envie campos como `isAdmin`).
2. **Passo 2: O Controller chama o Service** – O Controller delega a criação ao Service.
3. **Passo 3: A Lógica no Service** – O Service verifica se o email já existe e se a senha é forte. Converte o DTO em **Model**.
4. **Passo 4: O Model e o Banco de Dados** – O Model interage com o banco e o Service guarda a informação.
5. **Passo 5: O Retorno** – O Controller cria um **DTO de Resposta** (sem a senha) e envia ao cliente.

### 3. Resumo das Responsabilidades

| Camada | Responsabilidade | O que **não** deve fazer |
| --- | --- | --- |
| **Controller** | Rotas, status HTTP (200, 404), receber DTOs. | Nunca deve ter lógica de cálculo ou regras de negócio. |
| **DTO** | Apenas carregar dados de um ponto a outro. | Não deve ter lógica complexa. |
| **Service** | Regras de negócio, validações, cálculos. | Não deve saber detalhes de rotas ou requisições HTTP. |
| **Model** | Representar a tabela do banco de dados. | Não deve conter regras de negócio complexas. |

---

## 💻 Exemplo Prático: Código Fonte

### 1. Model (A Entidade do Banco)
     Esta classe mapeia exatamente como os dados aparecem no banco de dados.

```java
package com.spring.java.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

    @Entity
    @Table(name = "usuarios")  // Cria a tabela no postgres
    public class Usuario {
        
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Id
        //Atributos
        private Long id;
        private String nome;
        private String email;
        private String senha;
        
        //Construtor Vazio
        public Usuario() {
        }
        //Construtor
        public Usuario(Long id, String nome, String email, String senha) {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.senha = senha;
        }

        //Getters and Setters
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getNome() {
            return nome;
        }
        public void setNome(String nome) {
            this.nome = nome;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getSenha() {
            return senha;
        }
        public void setSenha(String senha) {
            this.senha = senha;
        }
    }

```

### 2. DTO (Data Transfer Object)
    Usamos um DTO para não expor a senha no retorno e para validar a entrada.

```java
// DTO de Entrada: O que o cliente envia
public record UsuarioRequestDTO(String nome, String email, String senha) {}

// DTO de Saída: O que o cliente recebe (Sem a senha!)
public record UsuarioResponseDTO(Long id, String nome, String email) {}

```

### 3. Service (Lógica Completa)
    Aqui é onde convertemos o DTO em Model e aplicamos as regras.     
```java

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

//=================================================================================================================

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

// ================================================================================================================

    //  #Listar Todos
    public List<UsuarioResponseDTO> listarTodos() {
        // Busca do banco
        List<Usuario> usuarios = repository.findAll();

        // Converte de Model para DTO manualmente para não ter erro de Stream
        return usuarios.stream()
                .map(u -> new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail()))
                .toList();
    }

//=================================================================================================================

    // Buscar Por Id
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));

        return new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail());
    }

//=================================================================================================================

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
//==================================================================================================================

}


```

### 4. Controller (Endpoints REST)
    Ele apenas recebe a requisição e entrega para o serviço.

```java
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
```

### 5. Repository ( JPA )
       O Repository é uma interface. Você não precisa escrever o código de 
       "SALVAR" ou "DELETAR"; o Spring Data JPA já faz isso para você.

```Java

package com.spring.java.repository;

import com.spring.java.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// O Spring cria a query automaticamente só pelo nome do método!
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    //Verifica se ha email existente bloqueando se tiver
    boolean existsByEmail(String email);  
}
```

---
## 📖 O Fluxo Visual da Requisição
  1. O Cliente faz um POST enviando um JSON.

  2. O Controller recebe esse JSON e o Spring o transforma automaticamente no UsuarioRequestDTO.

  3. O Service recebe o DTO, valida as regras e cria o objeto Usuario (Model).

  4. O Repository (camada de dados) salva o Usuario no banco.

  5. O Service transforma o Usuario salvo em um UsuarioResponseDTO.

  6. O Controller envia esse DTO de volta com o status 201 Created.

    Por que isso é bom?
         Repare que o cliente nunca viu a senha de volta no JSON de resposta, porque 
       o UsuarioResponseDTO não tem o campo senha. Além disso, se você precisar 
       mudar o nome da tabela no banco, você só mexe no Model, sem quebrar o 
       que o cliente recebe lá na ponta.

---

## 🛠️ Configuração do Ambiente

### 1. Dependências (`pom.xml`)

* **Spring Data JPA**
* **PostgreSQL Driver**
* **Spring Web**

### 2. Configuração do Banco de Dados (`em application.properties`)

```properties
# spring.application.name=spring-boot-com-JAVA

spring.datasource.url=jdbc:postgresql://localhost:5432/teste_estudo_java
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-postgresql=true
spring.properties.hibernate.format_postgresql=true
#spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgresPlusDialecttgreSQLDialect
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
server.port=8080

spring.web.resources.static-locations=classpath:/static/


```

---

### O Programa Completo ( Estrutura )
    Aqui está a estrutura de pastas sugerida: src/main/java/com/exemplo/projeto
```java
controller/UsuarioController.java

service/UsuarioService.java

repository/UsuarioRepository.java

model/Usuario.java

dto/UsuarioRequestDTO.java

dto/UsuarioResponseDTO.java
```

---


## 📖 Passo a Passo das Operações no Banco de Dados

1. POST (Create): O Controller recebe o DTO -> Service valida -> Repository executa INSERT INTO.

2. GET (Read): O Controller chama o Service -> Repository executa SELECT * FROM -> Service converte a lista de Models para DTOs.

3. PUT (Update): O Controller envia o ID e os novos dados -> Service busca o Model atual -> Altera os campos -> Repository executa UPDATE.

4. DELETE (Delete): O Controller envia o ID -> Service verifica existência -> Repository executa DELETE FROM.

Resumo da Estrutura Final:

1. Model: Sua tabela no Postgres.

2. DTO: O filtro de segurança para não expor a senha.

3 .Repository: Onde o SQL é gerado automaticamente.

4. Service: Onde você coloca "travas" (ex: não deletar se for admin).

5. Controller: Onde você define as URLs.




## 💻 Interface Frontend (SPA)

  Para que tudo funcione perfeitamente, seu projeto deve estar assim:
```java

  src/main/java (Seu código Java: Controllers, Services, etc.)
  
  src/main/resources
  
  static/
  
      index.html (A interface que criamos)
      
      style.css
      
      script.js 
  
  application.properties (Configuração do Postgres)
```

### Código HTML
```html
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Gerenciamento de Usuários</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<div class="container">
    <h2>Sistema de Usuários</h2>
    <div class="form-group">
        <input type="hidden" id="userId">
        <input type="text" id="nome" placeholder="Nome completo">
        <input type="email" id="email" placeholder="E-mail">
        <input type="password" id="senha" placeholder="Senha">
        <button class="btn-save" onclick="salvarUsuario()">Salvar</button>
    </div>
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>E-mail</th>
            <th>Ações</th>
        </tr>
        </thead>
        <tbody id="tabelaCorpo"></tbody>
    </table>
</div>
<script src="main.js"></script>
</body>
</html>

```

### Código CSS
```css
 :root { --primary: #4a90e2; --dark: #333; --light: #f4f4f4; --danger: #e74c3c; }

body { 
    font-family: sans-serif; 
    background: var(--light);
    display: flex; 
    justify-content: center; 
}

.container { 
    width: 90%; 
    max-width: 800px; 
    background: white; 
    padding: 20px; 
    margin-top: 30px; 
    border-radius: 8px; 
    box-shadow: 0 4px 6px rgba(0,0,0,0.1); 
}

.form-group { 
    display: grid; 
    grid-template-columns: 1fr 1fr 1fr auto; 
    gap: 10px; margin-bottom: 20px; 
}

input { 
    padding: 10px; 
    border: 1px solid #ddd; 
    border-radius: 4px; 
}

button { 
    padding: 10px 15px; 
    border: none; 
    border-radius: 4px; 
    cursor: pointer; 
    color: white; 
}

.btn-save { 
    background: var(--primary); 
}

.btn-delete { 
    background: var(--danger); 
}


.btn-edit { 
    background: #f39c12; 
    margin-right: 5px; 
}

table { 
    width: 100%; 
    border-collapse: collapse; 
}

th, td { 
    padding: 12px; 
    border-bottom: 1px solid #ddd; 
    text-align: left; 
}
th { 
    background: var(--primary); 
    color: white; 
}
```

### Código JS
```javascript
  //const API_URL = '/usuarios'; // Como o HTML está no mesmo servidor, não precisa do localhost:8080
  const API_URL = 'http://localhost:8080/usuarios';  //Força a api abrir a url descriminada


document.addEventListener('DOMContentLoaded', listarUsuarios);

async function listarUsuarios() {
    try {
        const resp = await fetch(API_URL);
        const usuarios = await resp.json();
        const corpo = document.getElementById('tabelaCorpo');
        corpo.innerHTML = '';

        usuarios.forEach(u => {
            corpo.innerHTML += `
                <tr>
                    <td>${u.id}</td>
                    <td>${u.nome}</td>
                    <td>${u.email}</td>
                    <td>
                        <button class="btn-edit" onclick="preencherForm(${u.id}, '${u.nome}', '${u.email}')">Editar</button>
                        <button class="btn-delete" onclick="deletarUsuario(${u.id})">Excluir</button>
                    </td>
                </tr>`;
        });
    } catch (error) {
        console.error("Erro ao listar:", error);
    }
}

async function salvarUsuario() {
    const id = document.getElementById('userId').value;
    const dados = {
        nome: document.getElementById('nome').value,
        email: document.getElementById('email').value,
        senha: document.getElementById('senha').value
    };

    const metodo = id ? 'PUT' : 'POST';
    const url = id ? `${API_URL}/${id}` : API_URL;

    await fetch(url, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    });

    limparForm();
    listarUsuarios();
}

async function deletarUsuario(id) {
    if(confirm('Deseja excluir?')) {
        await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        listarUsuarios();
    }
}

function preencherForm(id, nome, email) {
    document.getElementById('userId').value = id;
    document.getElementById('nome').value = nome;
    document.getElementById('email').value = email;
}

function limparForm() {
    document.getElementById('userId').value = '';
    document.getElementById('nome').value = '';
    document.getElementById('email').value = '';
    document.getElementById('senha').value = '';
}

function limparFormulario() {
    document.getElementById('userId').value = '';
    document.getElementById('nome').value = '';
    document.getElementById('email').value = '';
    document.getElementById('senha').value = '';
    document.getElementById('senha').placeholder = "Senha";

    salvarUsuario()
    listarUsuarios()
}

```

Os ficheiros devem ser guardados em `src/main/resources/static/` para evitar erros 404.

* **index.html:** Estrutura básica com link para `style.css` e `main.js`.
* **main.js:** Utiliza `fetch` para consumir a API.
```javascript
const API_URL = 'http://localhost:8080/usuarios';
async function listarUsuarios() {
    const resp = await fetch(API_URL);
    const usuarios = await resp.json();
}

```
---

## 💻 Utilizando o Postman para testar a API. 

  Como o seu projeto está usando as anotações do Spring, ele estará rodando por padrão em ``` http://localhost:8080. ```

### Aqui está o guia para testar cada operação:

 ### 1. Criar Usuário (POST)
  Este é o primeiro passo para popular seu banco de dados.
  Crie uma requisição POST para http://localhost:8080/usuarios.
  
  Método: POST
  
  URL: http://localhost:8080/usuarios
  
  Aba Body: Selecione raw e mude o tipo para JSON.
  
  Conteúdo:
  
  ```JSON  
  {
      "nome": "João Silva",
      "email": "joao@email.com",
      "senha": "123"
  }
```
    Resposta esperada: Status 201 Created ou 200 OK com o JSON do usuário e o id gerado.
  
  
 ### 2. Listar Todos (GET)
  Método: GET
    
  URL: http://localhost:8080/usuarios
  
  Aba Body: Nenhuma (None).
  
    Resposta esperada: Uma lista [] contendo todos os usuários cadastrados.
  
 ### 3. Atualizar Usuário (PUT)
  Aqui você precisa passar o ID do usuário que deseja alterar na URL.
  
  Método: PUT
  
  URL: http://localhost:8080/usuarios/1 (Troque o 1 pelo ID real que você recebeu no POST).
  
  Aba Body: raw -> JSON.
  
  Conteúdo:
  
  ```JSON  
  {
      "nome": "João Silva Alterado",
      "email": "joao.novo@email.com",
      "senha": "123" 
  }
```
    Resposta esperada: Retorna o objeto atualizado.
  
 ### 4. Deletar Usuário (DELETE)
  Método: DELETE
  
  URL: http://localhost:8080/usuarios/1 (Troque o 1 pelo ID que deseja apagar).
  
  Aba Body: Nenhuma.
  
    Resposta esperada: Status 204 No Content. Se você fizer um GET depois disso, o usuário não deve mais aparecer.
  
  Dicas de Ouro no Postman
  
      Verifique o Status Code: No canto superior direito da resposta, o 
    Postman mostra o status (ex: 200 OK, 201 Created, 404 Not Found). 
    Se der 500, olhe o console da sua IDE (IntelliJ/VS Code), pois 
    houve um erro no Java.
  
      Content-Type: Certifique-se de que, ao enviar um POST ou PUT, o cabeçalho
    (Headers) contenha Content-Type: application/json. O Postman faz isso 
    automaticamente quando você seleciona "JSON" no Body.
    
      Logs do Hibernate: No seu console do Java, você verá o Spring imprimindo
    comandos como Hibernate: insert into... ou Hibernate: select.... Isso 
    confirma que o código está chegando ao Postgres.

---
### Para testar a api direto no navegador 
  Abra o navegador e cole os seguintes endereços:

  1. Verificar se a api esta funcionando retornando um JSON ( http://localhost:8080/usuarios )    
  
  2. Verificar se o arquivo main.js esta funcionando ( http://localhost:8080/main.js )  
  
  3. Verificar se o arquivo style.css esta funcionando ( http://localhost:8080/style.css )
  

---

## ⚠️ Guia de Erros Solucionados

| Erro | Causa | Solução |
| --- | --- | --- |
| **Illegal start of expression** | Chaves `{}` mal fechadas ou método dentro de outro. | Formatar código e alinhar escopos. |
| **Unexpected token '<'** | JS tentou ler JSON mas o Java enviou HTML (Erro 404). | Corrigir URL da API e garantir `@RestController`. |
| **404 Not Found (JS/CSS)** | Ficheiros em subpastas não mapeadas. | Mover ficheiros para a raiz da pasta `static`. |
| **Long id specified error** | Spring 3.2+ não identifica nome do parâmetro. | Usar `@PathVariable(name = "id")`. |

---

*Este README foi gerado com base na documentação técnica do sistema.*
