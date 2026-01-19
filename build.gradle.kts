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
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly(files("libs/Cassaforte-0.1.3.jar"))
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
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