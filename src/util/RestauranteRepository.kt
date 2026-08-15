package util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import model.Restaurante
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class RestauranteRepository(
    private val diretorio: Path = Paths.get("dados", "restaurantes"),
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
) {
    private val padraoNomeArquivo = Regex("restaurante_(\\d+)\\.json")

    fun listarTodos(): List<Restaurante> =
        listarArquivos().map { lerRestaurante(it) }

    fun buscarPorEmail(email: String): Restaurante? =
        listarTodos().firstOrNull { emailsIguais(it.email, email) }

    fun emailJaCadastrado(email: String): Boolean =
        buscarPorEmail(email) != null

    fun salvarNovo(restaurante: Restaurante): Boolean {
        criarDiretorioSeNecessario()

        if (emailJaCadastrado(restaurante.email)) {
            return false
        }

        val arquivo = diretorio.resolve("restaurante_${proximoId()}.json")
        escreverRestaurante(arquivo, restaurante)
        return true
    }

    fun atualizar(restaurante: Restaurante): Boolean {
        val arquivo = listarArquivos().firstOrNull {
            emailsIguais(lerRestaurante(it).email, restaurante.email)
        } ?: return false

        escreverRestaurante(arquivo, restaurante)
        return true
    }

    private fun listarArquivos(): List<Path> {
        criarDiretorioSeNecessario()

        return Files.list(diretorio).use { arquivos ->
            arquivos
                .filter { Files.isRegularFile(it) && padraoNomeArquivo.matches(it.fileName.toString()) }
                .sorted(compareBy { extrairId(it) })
                .toList()
        }
    }

    private fun proximoId(): Int {
        val idsEmUso = listarArquivos().map { extrairId(it) }.toSet()
        return generateSequence(1) { it + 1 }.first { it !in idsEmUso }
    }

    private fun extrairId(arquivo: Path): Int =
        padraoNomeArquivo.matchEntire(arquivo.fileName.toString())
            ?.groupValues
            ?.get(1)
            ?.toInt()
            ?: error("Nome de arquivo de restaurante invalido: ${arquivo.fileName}")

    private fun lerRestaurante(arquivo: Path): Restaurante =
        Files.newBufferedReader(arquivo, StandardCharsets.UTF_8).use { leitor ->
            gson.fromJson(leitor, Restaurante::class.java)
        }

    private fun escreverRestaurante(arquivo: Path, restaurante: Restaurante) {
        Files.newBufferedWriter(
            arquivo,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { escritor ->
            gson.toJson(restaurante, escritor)
        }
    }

    private fun criarDiretorioSeNecessario() {
        Files.createDirectories(diretorio)
    }

    private fun emailsIguais(primeiro: String, segundo: String): Boolean =
        primeiro.trim().equals(segundo.trim(), ignoreCase = true)
}
