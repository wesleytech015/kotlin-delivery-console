package restaurante

import model.ItemMenu
import model.Restaurante
import util.RestauranteRepository

fun main() {
    val repository = RestauranteRepository()

    while (true) {
        println()
        println("[1] Entrar como Restaurante Existente")
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

private fun entrar(repository: RestauranteRepository) {
    val email = lerEntrada("E-mail: ")

    if (email.isNullOrBlank()) {
        println("E-mail inválido.")
        return
    }

    val restaurante = repository.buscarPorEmail(email)
    if (restaurante == null) {
        println("Restaurante não cadastrado.")
    } else {
        println("Bem-vindo, ${restaurante.nome}!")
    }
}

private fun cadastrarNovo(repository: RestauranteRepository) {
    println()
    println("--- Novo Cadastro ---")

    val nome = lerCampoObrigatorio("Nome: ") ?: return
    val email = lerCampoObrigatorio("E-mail: ") ?: return
    val endereco = lerCampoObrigatorio("Endereço: ") ?: return

    if (repository.emailJaCadastrado(email)) {
        println("Já existe um restaurante cadastrado com esse e-mail.")
        return
    }

    val menu = cadastrarCardapioInicial()
    val restaurante = Restaurante(nome, email, endereco, menu)

    if (repository.salvarNovo(restaurante)) {
        println("Cadastro realizado com sucesso.")
    } else {
        println("Não foi possível cadastrar: o e-mail já está em uso.")
    }
}

private fun cadastrarCardapioInicial(): MutableList<ItemMenu> {
    val itens = mutableListOf<ItemMenu>()

    println()
    println("--- Cadastro Inicial do Cardápio ---")
    println("Pressione Enter no número do item para finalizar.")

    while (true) {
        val numeroTexto = lerEntrada("numero_item: ")
        if (numeroTexto.isNullOrBlank()) {
            return itens
        }

        val numeroItem = numeroTexto.toIntOrNull()
        if (numeroItem == null || numeroItem <= 0) {
            println("Número do item inválido. Informe um número inteiro positivo.")
            continue
        }

        if (itens.any { it.numeroItem == numeroItem }) {
            println("Já existe um item com esse número.")
            continue
        }

        val descricao = lerCampoObrigatorio("Descrição: ") ?: return itens
        val preco = lerPrecoValido() ?: return itens

        itens.add(ItemMenu(numeroItem, descricao, preco))
        println("Item adicionado ao cardápio.")
    }
}

private fun lerPrecoValido(): Double? {
    while (true) {
        val entrada = lerEntrada("Preço: ") ?: return null
        val preco = entrada.replace(',', '.').toDoubleOrNull()

        if (preco != null && preco.isFinite() && preco > 0) {
            return preco
        }

        println("Preço inválido. Informe um valor numérico maior que zero.")
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
