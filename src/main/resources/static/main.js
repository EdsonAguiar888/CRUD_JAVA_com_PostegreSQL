//const API_URL = '/usuarios'; // Como o HTML está no mesmo servidor, não precisa do localhost:8080
const API_URL = 'http://localhost:8080/usuarios';


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