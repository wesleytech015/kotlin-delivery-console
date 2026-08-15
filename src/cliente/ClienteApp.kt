package cliente

import model.Cliente
import util.ClienteRepository

fun main() {
    val repository = ClienteRepository()

    while (true) {
        println()
        println("[1] Entrar")
        println("[2] Novo Cadastro")
        println("[0] Sair")

        when (lerEntrada("Escolha uma opção: ")) {
            "1" -> entrar(repository)
            "2" -> cadastrarNovo(repository)
            "0", null -> {
                println("Aplicativo encerrado.")
                return
            }
            else -> println("Opção inválida.")
        }
    }
}

private fun entrar(repository: ClienteRepository) {
    val telefone = lerTelefone() ?: return
    val cliente = repository.buscarPorTelefone(telefone)

    if (cliente == null) {
        println("Cliente não encontrado.")
        return
    }

    println("Bem-vindo, ${cliente.nome}!")
    exibirMenuPrincipal(cliente)
}

private fun cadastrarNovo(repository: ClienteRepository) {
    println()
    println("--- Novo Cadastro ---")

    val nome = lerCampoObrigatorio("Nome: ") ?: return
    val telefone = lerTelefone() ?: return
    val endereco = lerCampoObrigatorio("Endereço: ") ?: return

    if (repository.telefoneJaCadastrado(telefone)) {
        println("Já existe um cliente cadastrado com esse telefone.")
        return
    }

    val cliente = Cliente(nome, telefone, endereco)
    if (repository.salvarNovo(cliente)) {
        println("Cadastro realizado com sucesso.")
        exibirMenuPrincipal(cliente)
    } else {
        println("Não foi possível cadastrar: o telefone já está em uso.")
    }
}

private fun exibirMenuPrincipal(cliente: Cliente) {
    while (true) {
        println()
        println("[1] Realizar Novo Pedido")
        println("[2] Ver Pedidos em Andamento")
        println("[3] Ver Pedidos Finalizados")
        println("[0] Sair")

        when (lerEntrada("Escolha uma opção: ")) {
            "1" -> println("A realização de pedidos será adicionada na próxima etapa.")
            "2" -> println("A consulta de pedidos em andamento será adicionada na próxima etapa.")
            "3" -> println("A consulta de pedidos finalizados será adicionada na próxima etapa.")
            "0", null -> {
                println("Sessão de ${cliente.nome} encerrada.")
                return
            }
            else -> println("Opção inválida.")
        }
    }
}

private fun lerTelefone(): String? {
    while (true) {
        val telefone = lerEntrada("Telefone: ") ?: return null

        if (telefone.matches(Regex("\\d{8,15}"))) {
            return telefone
        }

        println("Telefone inválido. Informe somente de 8 a 15 números.")
    }
}

private fun lerCampoObrigatorio(mensagem: String): String? {
    while (true) {
        val valor = lerEntrada(mensagem) ?: return null
        if (valor.isNotBlank()) {
            return valor
        }
        println("Este campo é obrigatório.")
    }
}

private fun lerEntrada(mensagem: String): String? {
    print(mensagem)
    return readlnOrNull()?.trim()
}
