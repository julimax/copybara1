plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "2.0.0"        // Docs para Kotlin
    application
}

group = "io.github.julimax"
version = "1.0.7"
description = "Copybara Kotlin Hello World Application"

repositories { mavenCentral() }

dependencies {
    implementation(kotlin("stdlib"))
}

application { mainClass.set("MainKt") }

// Configurar el JAR para que sea ejecutable
tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    // Crear un Fat JAR que incluya todas las dependencias
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

java {
    // crea automáticamente sourcesJar; el javadocJar lo haremos con Dokka
    withSourcesJar()
}

// --- Dokka -> javadocJar (Kotlin) ---
tasks.register<org.gradle.jvm.tasks.Jar>("javadocJar") {
    dependsOn(tasks.named("dokkaJavadoc"))  // genera Javadoc-style con Dokka
    from(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
}

// --- Publicación (POM completo + artifacts) ---
publishing {
    repositories {
        maven {
            name = "staging"
            url = uri(layout.buildDirectory.dir("staging-deploy").get())
        }
        // Maven Central (Sonatype OSSRH) - alternativa directa a JReleaser
        maven {
            name = "sonatype"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
    
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.named("javadocJar"))
            // sourcesJar ya viene de 'java.withSourcesJar()'

            pom {
                name.set("Copybara Kotlin Hello World")
                description.set("A basic Kotlin Hello World application for Copybara project")
                url.set("https://github.com/julimax/copybara1")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("julimax")
                        name.set("Juli Gonzalez")
                        email.set("julimax951@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/julimax/copybara1.git")
                    developerConnection.set("scm:git:git@github.com:julimax/copybara1.git")
                    url.set("https://github.com/julimax/copybara1")
                }
            }
        }
    }
}

// --- Firma PGP en memoria (requerida por Central) ---
signing {
    val keyFile = System.getenv("SIGNING_KEY_FILE")
    val pass = System.getenv("SIGNING_PASSPHRASE")

    if (!keyFile.isNullOrBlank()) {
        val key = file(keyFile).readText(Charsets.UTF_8)
        useInMemoryPgpKeys(key, pass)
        sign(publishing.publications)
    } else {
        logger.lifecycle("Signing disabled (no SIGNING_KEY_FILE).")
    }
}

// --- Tareas personalizadas para deployment ---
tasks.register("publishStaging") {
    dependsOn("publishMavenPublicationToStagingRepository")
    description = "Publica los artefactos al repositorio de staging local"
    group = "publishing"
}

tasks.register("publishToMavenCentral") {
    dependsOn("publishMavenPublicationToSonatypeRepository")
    description = "Publica los artefactos directamente a Maven Central (Sonatype OSSRH)"
    group = "publishing"
}

// --- Maven Central deployment via direct HTTP upload ---
// Using Sonatype Central Publisher API directly instead of JReleaser
