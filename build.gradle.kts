plugins {
    kotlin("jvm") version "2.0.21"
}

group = "br.edu.delivery"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runRestaurante") {
    group = "application"
    description = "Executa o App Restaurante."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("restaurante.RestauranteAppKt")
    standardInput = System.`in`
}

tasks.register<JavaExec>("runCliente") {
    group = "application"
    description = "Executa o App Cliente."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("cliente.ClienteAppKt")
    standardInput = System.`in`
}
