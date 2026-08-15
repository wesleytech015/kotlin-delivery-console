package restaurante

import model.ItemMenu
import model.Pedido
import model.Restaurante
import model.StatusPedido
import util.PedidoRepository
import util.RestauranteRepository

fun main() {
    val restauranteRepository = RestauranteRepository()
    val pedidoRepository = PedidoRepository()

    while (true) {
        println()
        println("[1] Entrar como Restaurante Existente")
        println("[2] Novo Cadastro")
        println("[0] Sair")

        when (lerEntrada("Escolha uma opção: ")) {
            "1" -> entrar(restauranteRepository, pedidoRepository)
            "2" -> cadastrarNovo(restauranteRepository, pedidoRepository)
            "0", null -> {
                println("Aplicativo encerrado.")
                return
            }
            else -> println("Opção inválida.")
        }
    }
}

private fun entrar(
    restauranteRepository: RestauranteRepository,
    pedidoRepository: PedidoRepository
) {
    val email = lerEntrada("E-mail: ")

    if (email.isNullOrBlank()) {
        println("E-mail inválido.")
        return
    }

    val restaurante = restauranteRepository.buscarPorEmail(email)
    if (restaurante == null) {
        println("Restaurante não cadastrado.")
        return
    }

    println("Bem-vindo, ${restaurante.nome}!")
    exibirMenuPrincipal(restaurante, restauranteRepository, pedidoRepository)
}

private fun cadastrarNovo(
    restauranteRepository: RestauranteRepository,
    pedidoRepository: PedidoRepository
) {
    println()
    println("--- Novo Cadastro ---")

    val nome = lerCampoObrigatorio("Nome: ") ?: return
    val email = lerCampoObrigatorio("E-mail: ") ?: return
    val endereco = lerCampoObrigatorio("Endereço: ") ?: return

    if (restauranteRepository.emailJaCadastrado(email)) {
        println("Já existe um restaurante cadastrado com esse e-mail.")
        return
    }

    val menu = cadastrarCardapioInicial()
    val restaurante = Restaurante(nome, email, endereco, menu)

    if (restauranteRepository.salvarNovo(restaurante)) {
        println("Cadastro realizado com sucesso.")
        exibirMenuPrincipal(restaurante, restauranteRepository, pedidoRepository)
    } else {
        println("Não foi possível cadastrar: o e-mail já está em uso.")
    }
}

private fun exibirMenuPrincipal(
    restaurante: Restaurante,
    restauranteRepository: RestauranteRepository,
    pedidoRepository: PedidoRepository
) {
    while (true) {
        println()
        println("[1] Gerenciar Cardápio")
        println("[2] Visualizar Pedidos por Status")
        println("[3] Alterar Status do Pedido")
        println("[0] Sair")

        when (lerEntrada("Escolha uma opção: ")) {
            "1" -> gerenciarCardapio(restaurante, restauranteRepository)
            "2" -> visualizarPedidos(restaurante, pedidoRepository)
            "3" -> alterarStatusPedido(restaurante, pedidoRepository)
            "0", null -> {
                println("Sessão encerrada.")
                return
            }
            else -> println("Opção inválida.")
        }
    }
}

private fun visualizarPedidos(
    restaurante: Restaurante,
    pedidoRepository: PedidoRepository
) {
    val pedidos = pedidoRepository.filtrarPorEmailRestaurante(restaurante.email)
    if (pedidos.isEmpty()) {
        println("Não há pedidos para este restaurante.")
        return
    }

    println()
    println("--- Pedidos do Restaurante ---")

    pedidos.groupBy { it.status }.toSortedMap().forEach { (status, pedidosDoStatus) ->
        println()
        println("Status: ${nomeStatus(status)}")
        pedidosDoStatus.groupBy { it.idPedido }.forEach { (idPedido, itens) ->
            exibirPedido(idPedido, itens)
        }
    }
}

private fun exibirPedido(idPedido: String, itens: List<Pedido>) {
    val primeiro = itens.first()
    println()
    println("Pedido: $idPedido | Data: ${primeiro.dataHora}")
    println("Cliente: ${primeiro.nomeCliente}")
    println("Telefone: ${primeiro.telefoneCliente}")
    println("Endereço: ${primeiro.enderecoCliente}")

    var totalPedido = 0.0
    itens.forEach { item ->
        totalPedido += item.valorTotalItem
        println(
            "- ${item.descricaoItem} | ${item.quantidade} x R$ ${formatarValor(item.valorUnitario)} " +
                "= R$ ${formatarValor(item.valorTotalItem)}"
        )
    }
    println("Total: R$ ${formatarValor(totalPedido)}")
}

private fun alterarStatusPedido(
    restaurante: Restaurante,
    pedidoRepository: PedidoRepository
) {
    val idPedido = lerEntrada("ID do pedido: ")
    if (idPedido.isNullOrBlank()) {
        println("ID do pedido inválido.")
        return
    }

    val pedidosDoRestaurante = pedidoRepository.filtrarPorEmailRestaurante(restaurante.email)
    val itensDoPedido = pedidosDoRestaurante.filter { it.idPedido == idPedido }
    if (itensDoPedido.isEmpty()) {
        println("Pedido não encontrado para este restaurante.")
        return
    }

    println("Status atual: ${nomeStatus(itensDoPedido.first().status)}")
    StatusPedido.entries.forEach { status ->
        println("[${status.codigo}] ${status.name.replace('_', ' ')}")
    }

    val novoStatus = lerStatus() ?: return
    if (pedidoRepository.atualizarStatus(idPedido, novoStatus)) {
        println("Status do pedido atualizado com sucesso.")
    } else {
        println("Não foi possível atualizar o pedido.")
    }
}

private fun lerStatus(): Int? {
    while (true) {
        val entrada = lerEntrada("Novo status: ") ?: return null
        val status = entrada.toIntOrNull()

        if (status != null && StatusPedido.entries.any { it.codigo == status }) {
            return status
        }

        println("Status inválido. Informe um número de 0 a 4.")
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

private fun formatarValor(valor: Double): String = "%.2f".format(valor)

private fun nomeStatus(codigo: Int): String =
    StatusPedido.entries.firstOrNull { it.codigo == codigo }
        ?.name
        ?.replace('_', ' ')
        ?: "DESCONHECIDO"
