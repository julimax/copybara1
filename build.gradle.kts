plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "2.0.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
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

java {
    withSourcesJar()
}

// Javadoc JAR usando Dokka
tasks.register<org.gradle.jvm.tasks.Jar>("javadocJar") {
    dependsOn(tasks.named("dokkaJavadoc"))
    from(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
}

// Maven Central publishing
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.named("javadocJar"))

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

// Nexus publishing configuration for Central Publisher Portal
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://central.sonatype.com/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_CENTRAL_TOKEN"))
        }
    }
}

// PGP signing
signing {
    val signingKey = System.getenv("ORG_GRADLE_PROJECT_signingKey")
    val signingPassword = System.getenv("ORG_GRADLE_PROJECT_signingPassword")

    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
