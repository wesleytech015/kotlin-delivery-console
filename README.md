# Kotlin Delivery Console

Sistema de delivery executado pelo console, desenvolvido como atividade acadêmica
para praticar Kotlin/JVM, orientação a objetos e persistência local de dados.

O projeto possui dois aplicativos independentes: um para restaurantes e outro
para clientes. Não utiliza banco de dados ou servidor.

## Tecnologias

- Kotlin/JVM
- Gradle com Kotlin DSL e Gradle Wrapper
- Gson
- JSON
- CSV separado por ponto e vírgula

## Estrutura

```text
src/
├── restaurante/   # App Restaurante
├── cliente/       # App Cliente
├── model/         # Modelos de dados
└── util/          # Repositórios JSON e CSV
dados/
├── restaurantes/  # restaurante_1.json, restaurante_2.json, ...
├── clientes.json
└── pedidos.csv
```

## App Restaurante

Permite cadastrar um restaurante com e-mail único, fazer login, cadastrar o
cardápio inicial, visualizar, adicionar e remover itens. O restaurante também
pode consultar os pedidos recebidos por status e alterar o status de um pedido
que lhe pertence.

Cada restaurante é salvo em um arquivo individual
`dados/restaurantes/restaurante_ID.json`, contendo nome, e-mail, endereço e
cardápio. O Gson mantém o JSON legível.

## App Cliente

Permite cadastrar um cliente com telefone único, fazer login, listar
restaurantes e montar um pedido com vários itens. Antes de gravar, o aplicativo
mostra o resumo e solicita confirmação. O cliente pode consultar separadamente
pedidos em andamento e finalizados.

Todos os clientes são armazenados como uma lista JSON em
`dados/clientes.json`.

## Pedidos

Os pedidos são gravados em `dados/pedidos.csv`. Cada item ocupa uma linha e os
itens do mesmo pedido compartilham `id_pedido`, data, restaurante, cliente e
status. O cabeçalho é:

```text
id_pedido;data_hora;email_restaurante;nome_restaurante;telefone_cliente;nome_cliente;endereco_cliente;numero_item;quantidade;descricao_item;valor_unitario;valor_total_item;status
```

Status disponíveis:

| Código | Status |
|---:|---|
| 0 | SOLICITADO |
| 1 | EM_PREPARACAO |
| 2 | AGUARDANDO_ENTREGADOR |
| 3 | EM_TRANSITO |
| 4 | ENTREGUE |

## Como compilar e executar

Requisito: JDK 17 ou superior. O Gradle não precisa estar instalado, pois o
projeto inclui o Wrapper.

No Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runRestaurante
.\gradlew.bat runCliente
```

No Linux ou macOS:

```bash
./gradlew build
./gradlew runRestaurante
./gradlew runCliente
```

Execute os comandos na raiz do projeto para que a pasta `dados/` seja localizada
corretamente.

## Exemplo de fluxo

1. Cadastre um restaurante e seus itens.
2. Cadastre um cliente.
3. No App Cliente, selecione o restaurante, os itens e confirme o pedido.
4. No App Restaurante, consulte o pedido e atualize seu status.
5. No App Cliente, acompanhe o pedido até ele aparecer como finalizado.

O sistema valida e-mail e telefone únicos, números de item sem repetição no
mesmo cardápio, preços e quantidades positivos, seleção de restaurante, IDs de
pedido e códigos de status. Arquivos de dados ausentes ou vazios são preparados
automaticamente pelos repositórios.
