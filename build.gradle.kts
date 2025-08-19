plugins {
    kotlin("jvm") version "1.9.10"
    application
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "com.copybara"
version = "1.0.0"
description = "Copybara Kotlin Hello World Application"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

// Maven Central Publishing Configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Copybara Kotlin App")
                description.set("A basic Kotlin Hello World application for Copybara project")
                url.set("https://github.com/copybara/copybara1")
                
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("copybara-team")
                        name.set("Copybara Team")
                        email.set("team@copybara.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/copybara/copybara1.git")
                    developerConnection.set("scm:git:ssh://github.com:copybara/copybara1.git")
                    url.set("https://github.com/copybara/copybara1")
                }
            }
        }
    }
}

// Nexus publishing configuration
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("ossrhUsername") as String? ?: "")
            password.set(project.findProperty("ossrhPassword") as String? ?: "")
        }
    }
}

// Signing configuration
signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

// Generate sources JAR
tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

// Generate Javadoc JAR (using Dokka for Kotlin)
tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

// Add sources and javadoc JARs to publication
publishing {
    publications {
        named<MavenPublication>("maven") {
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}
