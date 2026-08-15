package cliente

import model.Cliente
import model.ItemMenu
import model.Pedido
import model.Restaurante
import model.StatusPedido
import util.ClienteRepository
import util.PedidoRepository
import util.RestauranteRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    val clienteRepository = ClienteRepository()
    val restauranteRepository = RestauranteRepository()
    val pedidoRepository = PedidoRepository()

    while (true) {
        println()
        println("[1] Entrar")
        println("[2] Novo Cadastro")
        println("[0] Sair")

        when (lerEntrada("Escolha uma opção: ")) {
            "1" -> entrar(clienteRepository, restauranteRepository, pedidoRepository)
            "2" -> cadastrarNovo(clienteRepository, restauranteRepository, pedidoRepository)
            "0", null -> {
                println("Aplicativo encerrado.")
                return
            }
            else -> println("Opção inválida.")
        }
    }
}

private fun entrar(
    clienteRepository: ClienteRepository,
    restauranteRepository: RestauranteRepository,
    pedidoRepository: PedidoRepository
) {
    val telefone = lerTelefone() ?: return
    val cliente = clienteRepository.buscarPorTelefone(telefone)

    if (cliente == null) {
        println("Cliente não encontrado.")
        return
    }

    println("Bem-vindo, ${cliente.nome}!")
    exibirMenuPrincipal(cliente, restauranteRepository, pedidoRepository)
}

private fun cadastrarNovo(
    clienteRepository: ClienteRepository,
    restauranteRepository: RestauranteRepository,
    pedidoRepository: PedidoRepository
) {
    println()
    println("--- Novo Cadastro ---")

    val nome = lerCampoObrigatorio("Nome: ") ?: return
    val telefone = lerTelefone() ?: return
    val endereco = lerCampoObrigatorio("Endereço: ") ?: return

    if (clienteRepository.telefoneJaCadastrado(telefone)) {
        println("Já existe um cliente cadastrado com esse telefone.")
        return
    }

    val cliente = Cliente(nome, telefone, endereco)
    if (clienteRepository.salvarNovo(cliente)) {
        println("Cadastro realizado com sucesso.")
        exibirMenuPrincipal(cliente, restauranteRepository, pedidoRepository)
    } else {
        println("Não foi possível cadastrar: o telefone já está em uso.")
    }
}

private fun exibirMenuPrincipal(
    cliente: Cliente,
    restauranteRepository: RestauranteRepository,
    pedidoRepository: PedidoRepository
) {
    while (true) {
        println()
        println("[1] Realizar Novo Pedido")
        println("[2] Ver Pedidos em Andamento")
        println("[3] Ver Pedidos Finalizados")
        println("[0] Sair")

        when (lerEntrada("Escolha uma opção: ")) {
            "1" -> realizarNovoPedido(cliente, restauranteRepository, pedidoRepository)
            "2" -> verPedidos(cliente, pedidoRepository, finalizados = false)
            "3" -> verPedidos(cliente, pedidoRepository, finalizados = true)
            "0", null -> {
                println("Sessão de ${cliente.nome} encerrada.")
                return
            }
            else -> println("Opção inválida.")
        }
    }
}

private fun realizarNovoPedido(
    cliente: Cliente,
    restauranteRepository: RestauranteRepository,
    pedidoRepository: PedidoRepository
) {
    val restaurantes = restauranteRepository.listarTodos()
    if (restaurantes.isEmpty()) {
        println("Não há restaurantes cadastrados.")
        return
    }

    println()
    println("--- Restaurantes Disponíveis ---")
    restaurantes.forEachIndexed { indice, restaurante ->
        println("[${indice + 1}] ${restaurante.nome} - ${restaurante.endereco}")
    }

    val indiceRestaurante = lerIndiceRestaurante(restaurantes.size) ?: return
    val restaurante = restaurantes[indiceRestaurante]

    if (restaurante.menu.isEmpty()) {
        println("O restaurante selecionado está com o cardápio vazio.")
        return
    }

    exibirCardapio(restaurante)
    val quantidades = selecionarItens(restaurante.menu)
    if (quantidades.isEmpty()) {
        println("Pedido cancelado: nenhum item foi selecionado.")
        return
    }

    exibirResumo(restaurante.menu, quantidades)
    if (!confirmarPedido()) {
        println("Pedido cancelado.")
        return
    }

    val idPedido = pedidoRepository.gerarProximoId()
    val dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val pedidos = quantidades.map { (numeroItem, quantidade) ->
        val item = restaurante.menu.first { it.numeroItem == numeroItem }
        Pedido(
            idPedido = idPedido,
            dataHora = dataHora,
            emailRestaurante = restaurante.email,
            nomeRestaurante = restaurante.nome,
            telefoneCliente = cliente.telefone,
            nomeCliente = cliente.nome,
            enderecoCliente = cliente.endereco,
            numeroItem = item.numeroItem,
            quantidade = quantidade,
            descricaoItem = item.descricao,
            valorUnitario = item.preco,
            valorTotalItem = item.preco * quantidade,
            status = StatusPedido.SOLICITADO.codigo
        )
    }

    pedidoRepository.salvarNovo(pedidos)
    println("Pedido $idPedido realizado com sucesso.")
}

private fun verPedidos(
    cliente: Cliente,
    pedidoRepository: PedidoRepository,
    finalizados: Boolean
) {
    val pedidos = pedidoRepository.filtrarPorTelefone(cliente.telefone).filter {
        if (finalizados) {
            it.status == StatusPedido.ENTREGUE.codigo
        } else {
            it.status in StatusPedido.SOLICITADO.codigo..StatusPedido.EM_TRANSITO.codigo
        }
    }

    if (pedidos.isEmpty()) {
        val tipo = if (finalizados) "finalizados" else "em andamento"
        println("Não há pedidos $tipo.")
        return
    }

    val titulo = if (finalizados) "Pedidos Finalizados" else "Pedidos em Andamento"
    println()
    println("--- $titulo ---")

    pedidos.groupBy { it.idPedido }.forEach { (idPedido, itens) ->
        val primeiro = itens.first()
        println()
        println("Pedido: $idPedido")
        println("Data: ${primeiro.dataHora}")
        println("Restaurante: ${primeiro.nomeRestaurante}")
        println("Status: ${nomeStatus(primeiro.status)}")

        var totalPedido = 0.0
        itens.forEach { item ->
            totalPedido += item.valorTotalItem
            println(
                "- ${item.descricaoItem} | ${item.quantidade} x R$ " +
                    "${formatarValor(item.valorUnitario)} = R$ ${formatarValor(item.valorTotalItem)}"
            )
        }
        println("Total: R$ ${formatarValor(totalPedido)}")
    }
}

private fun lerIndiceRestaurante(quantidadeRestaurantes: Int): Int? {
    while (true) {
        val entrada = lerEntrada("Selecione o restaurante: ") ?: return null
        val numero = entrada.toIntOrNull()

        if (numero != null && numero in 1..quantidadeRestaurantes) {
            return numero - 1
        }

        println("Restaurante inválido. Escolha um número da lista.")
    }
}

private fun exibirCardapio(restaurante: Restaurante) {
    println()
    println("--- Cardápio de ${restaurante.nome} ---")
    restaurante.menu.forEach { item ->
        println("Número: ${item.numeroItem} | ${item.descricao} | R$ ${formatarValor(item.preco)}")
    }
    println("Pressione Enter no número do item para finalizar a seleção.")
}

private fun selecionarItens(menu: List<ItemMenu>): LinkedHashMap<Int, Int> {
    val quantidades = linkedMapOf<Int, Int>()

    while (true) {
        val numeroTexto = lerEntrada("numero_item: ")
        if (numeroTexto.isNullOrBlank()) {
            return quantidades
        }

        val numeroItem = numeroTexto.toIntOrNull()
        val item = menu.firstOrNull { it.numeroItem == numeroItem }
        if (item == null) {
            println("Item não encontrado no cardápio.")
            continue
        }

        val quantidade = lerQuantidade() ?: return quantidades
        quantidades[item.numeroItem] = (quantidades[item.numeroItem] ?: 0) + quantidade
        println("Item adicionado ao pedido.")
    }
}

private fun lerQuantidade(): Int? {
    while (true) {
        val entrada = lerEntrada("Quantidade: ") ?: return null
        val quantidade = entrada.toIntOrNull()

        if (quantidade != null && quantidade > 0) {
            return quantidade
        }

        println("Quantidade inválida. Informe um número inteiro positivo.")
    }
}

private fun exibirResumo(menu: List<ItemMenu>, quantidades: Map<Int, Int>) {
    var totalPedido = 0.0
    println()
    println("--- Resumo do Pedido ---")

    quantidades.forEach { (numeroItem, quantidade) ->
        val item = menu.first { it.numeroItem == numeroItem }
        val totalItem = item.preco * quantidade
        totalPedido += totalItem
        println("${item.descricao} | $quantidade x R$ ${formatarValor(item.preco)} = R$ ${formatarValor(totalItem)}")
    }

    println("Total: R$ ${formatarValor(totalPedido)}")
}

private fun confirmarPedido(): Boolean {
    while (true) {
        when (lerEntrada("Confirmar pedido? [S/N]: ")?.uppercase()) {
            "S" -> return true
            "N", null -> return false
            else -> println("Opção inválida. Digite S ou N.")
        }
    }
}

private fun formatarValor(valor: Double): String = "%.2f".format(valor)

private fun nomeStatus(codigo: Int): String =
    StatusPedido.entries.firstOrNull { it.codigo == codigo }
        ?.name
        ?.replace('_', ' ')
        ?: "DESCONHECIDO"

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
