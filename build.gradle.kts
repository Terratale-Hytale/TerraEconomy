plugins {
    id("java")
}

group = "terratale"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
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

    // Comentado: ruta de Windows no compatible con macOS
    // doLast {
    //     copy {
    //         from(archiveFile)
    //         into("C:\\dev\\tools\\Hytale\\Server\\mods")
    //     }
    // }
}