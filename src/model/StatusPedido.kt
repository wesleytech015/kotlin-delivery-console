package model

enum class StatusPedido(val codigo: Int) {
    SOLICITADO(0),
    EM_PREPARACAO(1),
    AGUARDANDO_ENTREGADOR(2),
    EM_TRANSITO(3),
    ENTREGUE(4)
}
