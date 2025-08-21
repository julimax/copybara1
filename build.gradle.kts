plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "2.0.0"        // Docs para Kotlin
    id("org.jreleaser") version "1.19.0"             // Publicación al Central Portal
    application
}

group = "io.github.julimax"
version = "1.0.6"
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

// --- JReleaser: sube al Central Publisher Portal ---
// Gradle publica primero a un staging local; JReleaser lo empuja al Portal.
jreleaser {
    project {
        description.set("Copybara Kotlin Hello World Application")
        longDescription.set("A basic Kotlin Hello World application for Copybara project")
        links {
            homepage.set("https://github.com/julimax/copybara1")
        }
        authors.set(listOf("Juli Gonzalez"))
        license.set("Apache-2.0")
        inceptionYear.set("2024")
    }
    
    signing {
        active.set(org.jreleaser.model.Active.NEVER)
    }
    
    deploy {
        maven {
            active.set(org.jreleaser.model.Active.ALWAYS)
            mavenCentral {
                // Definir un deployer con nombre (ej: "app")
                app {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    
                    // Decirle dónde dejó Gradle los artefactos stageados
                    stagingRepositories.add(
                        layout.buildDirectory.dir("staging-deploy").get().asFile.absolutePath
                    )
                    
                    // Opcional: si usás Bearer token (recomendado en Central Portal)
                    // authorization.set(org.jreleaser.model.api.deploy.maven.MavenCentralDeployer.Authorization.BEARER)
                    // username no es necesario para BEARER. El token va como "password".
                    // Si preferís usuario/clave, cambiá a BASIC.
                }
            }
        }
    }
}
