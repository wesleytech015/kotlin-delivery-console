# Sistema de Delivery Kotlin via Console

Estrutura inicial de uma atividade acadêmica em Kotlin/JVM para um sistema de
delivery executado pela linha de comando.

O projeto será composto futuramente por duas aplicações independentes:

- **App Restaurante**
- **App Cliente**

Nesta etapa estão disponíveis somente a configuração do projeto, a organização
dos diretórios e os arquivos usados para persistência local. Cadastro, login,
cardápio e pedidos ainda não foram implementados.

## Tecnologias

- Kotlin/JVM
- Gradle com Kotlin DSL
- Gson para leitura e escrita de JSON

## Estrutura

```text
src/
├── restaurante/
├── cliente/
├── model/
└── util/
dados/
├── restaurantes/
├── clientes.json
└── pedidos.csv
```

Os futuros arquivos de cada restaurante deverão ser armazenados em
`dados/restaurantes/`, com nomes como `restaurante_1.json` e
`restaurante_2.json`.

## Compilação

Com o Gradle instalado, execute:

```bash
gradle build
```

Ainda não há uma classe principal executável nesta etapa.
