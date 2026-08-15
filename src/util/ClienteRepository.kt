package util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import model.Cliente
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ClienteRepository(
    private val arquivo: Path = Paths.get("dados", "clientes.json"),
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
) {
    private val tipoListaClientes = object : TypeToken<List<Cliente>>() {}.type

    fun listarTodos(): List<Cliente> {
        criarArquivoSeNecessario()
        val conteudo = Files.readString(arquivo, StandardCharsets.UTF_8).trim()

        if (conteudo.isEmpty()) {
            return emptyList()
        }

        return gson.fromJson<List<Cliente>>(conteudo, tipoListaClientes) ?: emptyList()
    }

    fun buscarPorTelefone(telefone: String): Cliente? =
        listarTodos().firstOrNull { telefonesIguais(it.telefone, telefone) }

    fun telefoneJaCadastrado(telefone: String): Boolean =
        buscarPorTelefone(telefone) != null

    fun salvarNovo(cliente: Cliente): Boolean {
        val clientes = listarTodos().toMutableList()
        if (clientes.any { telefonesIguais(it.telefone, cliente.telefone) }) {
            return false
        }

        clientes.add(cliente)
        salvarLista(clientes)
        return true
    }

    fun atualizar(cliente: Cliente): Boolean {
        val clientes = listarTodos().toMutableList()
        val indice = clientes.indexOfFirst { telefonesIguais(it.telefone, cliente.telefone) }

        if (indice == -1) {
            return false
        }

        clientes[indice] = cliente
        salvarLista(clientes)
        return true
    }

    private fun salvarLista(clientes: List<Cliente>) {
        criarArquivoSeNecessario()
        Files.newBufferedWriter(
            arquivo,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { escritor ->
            gson.toJson(clientes, tipoListaClientes, escritor)
        }
    }

    private fun criarArquivoSeNecessario() {
        arquivo.parent?.let { Files.createDirectories(it) }
        if (Files.notExists(arquivo)) {
            Files.writeString(arquivo, "[]\n", StandardCharsets.UTF_8)
        }
    }

    private fun telefonesIguais(primeiro: String, segundo: String): Boolean =
        primeiro.trim() == segundo.trim()
}
