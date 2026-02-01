plugins {
    id("java")
}

group = "terratale"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/creatorfromhell/")
        name = "VaultUnlocked"
    }
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly("net.cfh.vault:VaultUnlocked:2.18.3")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("src/main/resources")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    // Excluir archivos de firma que causan problemas
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")

    // Comentado: ruta de Windows no compatible con macOS
    // doLast {
    //     copy {
    //         from(archiveFile)
    //         into("C:\\dev\\tools\\Hytale\\Server\\mods")
    //     }
    // }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
