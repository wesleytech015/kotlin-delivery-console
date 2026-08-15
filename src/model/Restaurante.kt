package model

data class Restaurante(
    val nome: String,
    val email: String,
    val endereco: String,
    val menu: MutableList<ItemMenu>
)
