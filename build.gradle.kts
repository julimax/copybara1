plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "2.0.0"        // Docs para Kotlin
    id("org.jreleaser") version "1.19.0"             // Publicación al Central Portal
    application
}

group = "io.github.julimax"
version = "1.0.2"
description = "Copybara Kotlin Hello World Application"

repositories { mavenCentral() }

dependencies {
    implementation(kotlin("stdlib"))
}

application { mainClass.set("MainKt") }

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
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.named("javadocJar"))
            // sourcesJar ya viene de 'java.withSourcesJar()'

            pom {
                name.set("Copybara Kotlin App")
                description.set("A basic Kotlin Hello World application for Copybara project")
                url.set("https://github.com/julimax/copybara1") // <-- alinea con tu repo real

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("julimax")
                        name.set("Juli Gonzalez")
                        email.set("you@example.com")
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

    require(!keyFile.isNullOrBlank()) { "SIGNING_KEY_FILE is required" }

    val key = file(keyFile).readText(Charsets.UTF_8)  // ✅ sin imports
    useInMemoryPgpKeys(key, pass)
    sign(publishing.publications) // o ["maven"] si así se llama
}



// --- JReleaser: sube al Central Publisher Portal ---
// Gradle publica primero a un staging local; JReleaser lo empuja al Portal.
tasks.register("publishStaging") {
    dependsOn("publishAllPublicationsToMavenLocal") // opcional si quieres validar local
}

jreleaser {
    signing {
        active.set(org.jreleaser.model.Active.NEVER)
    }
    deploy {
        maven {
            mavenCentral {
                active.set(org.jreleaser.model.Active.ALWAYS)
                // JReleaser detecta las publicaciones de Gradle y las sube al Portal
            }
        }
    }
}
