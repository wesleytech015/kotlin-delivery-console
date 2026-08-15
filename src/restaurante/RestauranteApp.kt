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
        return
    }

    println("Bem-vindo, ${restaurante.nome}!")
    exibirMenuPrincipal(restaurante, repository)
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
        exibirMenuPrincipal(restaurante, repository)
    } else {
        println("Não foi possível cadastrar: o e-mail já está em uso.")
    }
}

private fun exibirMenuPrincipal(
    restaurante: Restaurante,
    repository: RestauranteRepository
) {
    while (true) {
        println()
        println("[1] Gerenciar Cardápio")
        println("[2] Visualizar Pedidos por Status")
        println("[3] Alterar Status do Pedido")
        println("[0] Sair")

        when (lerEntrada("Escolha uma opção: ")) {
            "1" -> gerenciarCardapio(restaurante, repository)
            "2" -> println("A visualização de pedidos será adicionada na etapa de pedidos.")
            "3" -> println("A alteração de status será adicionada na etapa de pedidos.")
            "0", null -> {
                println("Sessão encerrada.")
                return
            }
            else -> println("Opção inválida.")
        }
    }
}

private fun gerenciarCardapio(
    restaurante: Restaurante,
    repository: RestauranteRepository
) {
    while (true) {
        println()
        println("[A] Ver Cardápio")
        println("[B] Adicionar Item")
        println("[C] Remover Item")
        println("[0] Voltar")

        when (lerEntrada("Escolha uma opção: ")?.uppercase()) {
            "A" -> verCardapio(restaurante)
            "B" -> adicionarItem(restaurante, repository)
            "C" -> removerItem(restaurante, repository)
            "0", null -> return
            else -> println("Opção inválida.")
        }
    }
}

private fun verCardapio(restaurante: Restaurante) {
    println()
    println("--- Cardápio ---")

    if (restaurante.menu.isEmpty()) {
        println("O cardápio está vazio.")
        return
    }

    restaurante.menu.forEach { item ->
        println("Número: ${item.numeroItem} | Descrição: ${item.descricao} | Preço: R$ %.2f".format(item.preco))
    }
}

private fun adicionarItem(
    restaurante: Restaurante,
    repository: RestauranteRepository
) {
    val numeroItem = lerNumeroItem() ?: return

    if (restaurante.menu.any { it.numeroItem == numeroItem }) {
        println("Já existe um item com esse número.")
        return
    }

    val descricao = lerCampoObrigatorio("Descrição: ") ?: return
    val preco = lerPrecoValido() ?: return
    val item = ItemMenu(numeroItem, descricao, preco)
    restaurante.menu.add(item)

    if (repository.atualizar(restaurante)) {
        println("Item adicionado com sucesso.")
    } else {
        restaurante.menu.remove(item)
        println("Não foi possível salvar o item.")
    }
}

private fun removerItem(
    restaurante: Restaurante,
    repository: RestauranteRepository
) {
    val numeroItem = lerNumeroItem() ?: return
    val indice = restaurante.menu.indexOfFirst { it.numeroItem == numeroItem }

    if (indice == -1) {
        println("Item não encontrado no cardápio.")
        return
    }

    val itemRemovido = restaurante.menu.removeAt(indice)
    if (repository.atualizar(restaurante)) {
        println("Item removido com sucesso.")
    } else {
        restaurante.menu.add(indice, itemRemovido)
        println("Não foi possível salvar a remoção.")
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

private fun lerNumeroItem(): Int? {
    while (true) {
        val entrada = lerEntrada("Número do item: ") ?: return null
        val numero = entrada.toIntOrNull()

        if (numero != null && numero > 0) {
            return numero
        }

        println("Número do item inválido. Informe um número inteiro positivo.")
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
