package util

import model.Pedido
import model.StatusPedido
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class PedidoRepository(
    private val arquivo: Path = Paths.get("dados", "pedidos.csv")
) {
    companion object {
        const val CABECALHO =
            "id_pedido;data_hora;email_restaurante;nome_restaurante;telefone_cliente;nome_cliente;" +
                "endereco_cliente;numero_item;quantidade;descricao_item;valor_unitario;valor_total_item;status"
    }

    fun listarTodos(): List<Pedido> {
        garantirArquivo()
        val linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8)

        if (linhas.firstOrNull()?.trim() != CABECALHO) {
            error("Cabeçalho inválido no arquivo de pedidos.")
        }

        return linhas.drop(1)
            .filter { it.isNotBlank() }
            .map { converterLinhaEmPedido(it) }
    }

    fun salvarNovo(pedido: Pedido) = salvarNovo(listOf(pedido))

    fun salvarNovo(pedidos: List<Pedido>) {
        if (pedidos.isEmpty()) {
            return
        }

        require(pedidos.map { it.idPedido }.distinct().size == 1) {
            "Todas as linhas de um novo pedido devem possuir o mesmo ID."
        }
        pedidos.forEach { validarPedido(it) }

        garantirArquivo()
        val linhas = pedidos.joinToString(System.lineSeparator()) { converterPedidoEmLinha(it) }

        Files.writeString(
            arquivo,
            linhas + System.lineSeparator(),
            StandardCharsets.UTF_8,
            StandardOpenOption.APPEND
        )
    }

    fun gerarProximoId(): String =
        ((listarTodos().mapNotNull { it.idPedido.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()

    fun filtrarPorTelefone(telefone: String): List<Pedido> =
        listarTodos().filter { it.telefoneCliente.trim() == telefone.trim() }

    fun filtrarPorEmailRestaurante(email: String): List<Pedido> =
        listarTodos().filter { it.emailRestaurante.trim().equals(email.trim(), ignoreCase = true) }

    fun filtrarPorStatus(status: Int): List<Pedido> =
        listarTodos().filter { it.status == status }

    fun atualizarStatus(idPedido: String, novoStatus: Int): Boolean {
        require(StatusPedido.entries.any { it.codigo == novoStatus }) {
            "Status deve estar entre 0 e 4."
        }

        val pedidos = listarTodos()
        if (pedidos.none { it.idPedido == idPedido }) {
            return false
        }

        val atualizados = pedidos.map {
            if (it.idPedido == idPedido) it.copy(status = novoStatus) else it
        }
        reescrever(atualizados)
        return true
    }

    private fun reescrever(pedidos: List<Pedido>) {
        garantirDiretorio()
        val conteudo = buildString {
            appendLine(CABECALHO)
            pedidos.forEach { appendLine(converterPedidoEmLinha(it)) }
        }
        Files.writeString(
            arquivo,
            conteudo,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    private fun converterPedidoEmLinha(pedido: Pedido): String = listOf(
        pedido.idPedido,
        pedido.dataHora,
        pedido.emailRestaurante,
        pedido.nomeRestaurante,
        pedido.telefoneCliente,
        pedido.nomeCliente,
        pedido.enderecoCliente,
        pedido.numeroItem.toString(),
        pedido.quantidade.toString(),
        pedido.descricaoItem,
        pedido.valorUnitario.toString(),
        pedido.valorTotalItem.toString(),
        pedido.status.toString()
    ).joinToString(";") { escaparCampo(it) }

    private fun validarPedido(pedido: Pedido) {
        require(pedido.idPedido.isNotBlank()) { "ID do pedido não pode ser vazio." }
        require(pedido.numeroItem > 0) { "Número do item deve ser positivo." }
        require(pedido.quantidade > 0) { "Quantidade deve ser positiva." }
        require(pedido.valorUnitario > 0 && pedido.valorUnitario.isFinite()) {
            "Valor unitário deve ser positivo."
        }
        require(pedido.valorTotalItem > 0 && pedido.valorTotalItem.isFinite()) {
            "Valor total do item deve ser positivo."
        }
        require(StatusPedido.entries.any { it.codigo == pedido.status }) {
            "Status deve estar entre 0 e 4."
        }
    }

    private fun converterLinhaEmPedido(linha: String): Pedido {
        val campos = separarCampos(linha)
        require(campos.size == 13) { "Linha inválida no arquivo de pedidos: $linha" }

        return Pedido(
            idPedido = campos[0],
            dataHora = campos[1],
            emailRestaurante = campos[2],
            nomeRestaurante = campos[3],
            telefoneCliente = campos[4],
            nomeCliente = campos[5],
            enderecoCliente = campos[6],
            numeroItem = campos[7].toInt(),
            quantidade = campos[8].toInt(),
            descricaoItem = campos[9],
            valorUnitario = campos[10].toDouble(),
            valorTotalItem = campos[11].toDouble(),
            status = campos[12].toInt()
        )
    }

    private fun escaparCampo(valor: String): String {
        val normalizado = valor.replace('\r', ' ').replace('\n', ' ')
        if (';' !in normalizado && '"' !in normalizado) {
            return normalizado
        }
        return "\"${normalizado.replace("\"", "\"\"")}\""
    }

    private fun separarCampos(linha: String): List<String> {
        val campos = mutableListOf<String>()
        val campoAtual = StringBuilder()
        var entreAspas = false
        var indice = 0

        while (indice < linha.length) {
            val caractere = linha[indice]
            when {
                caractere == '"' && entreAspas && indice + 1 < linha.length && linha[indice + 1] == '"' -> {
                    campoAtual.append('"')
                    indice++
                }
                caractere == '"' -> entreAspas = !entreAspas
                caractere == ';' && !entreAspas -> {
                    campos.add(campoAtual.toString())
                    campoAtual.clear()
                }
                else -> campoAtual.append(caractere)
            }
            indice++
        }

        require(!entreAspas) { "Campo com aspas não finalizadas no CSV." }
        campos.add(campoAtual.toString())
        return campos
    }

    private fun garantirArquivo() {
        garantirDiretorio()
        if (Files.notExists(arquivo) || Files.readString(arquivo, StandardCharsets.UTF_8).isBlank()) {
            Files.writeString(arquivo, CABECALHO + System.lineSeparator(), StandardCharsets.UTF_8)
            return
        }

        val conteudo = Files.readString(arquivo, StandardCharsets.UTF_8)
        if (!conteudo.endsWith("\n") && !conteudo.endsWith("\r")) {
            Files.writeString(arquivo, System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.APPEND)
        }
    }

    private fun garantirDiretorio() {
        arquivo.parent?.let { Files.createDirectories(it) }
    }
}
