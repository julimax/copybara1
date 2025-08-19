# Maven Central Publishing Setup

Esta guía explica cómo configurar la publicación automática de JARs a Maven Central para el proyecto Copybara.

## Prerrequisitos

### 1. Cuenta en Sonatype OSSRH
- Crear cuenta en [Sonatype JIRA](https://issues.sonatype.org)
- Crear ticket para reclamar el namespace `com.copybara`
- Esperar aprobación (puede tomar 1-2 días hábiles)

### 2. Claves GPG para Signing
```bash
# Generar nueva clave GPG
gpg --gen-key

# Listar claves
gpg --list-secret-keys --keyid-format LONG

# Exportar clave pública a servidor de claves
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Exportar clave privada para CI/CD
gpg --export-secret-keys YOUR_KEY_ID | base64 > signing-key.txt
```

## Configuración Local

### 1. Archivo `gradle.properties`
Copiar `gradle.properties.template` a `gradle.properties` y completar:

```properties
ossrhUsername=tu-usuario-sonatype
ossrhPassword=tu-password-sonatype
signing.keyId=TU_KEY_ID
signing.password=tu-password-gpg
signing.secretKeyRingFile=/home/usuario/.gnupg/secring.gpg
```

### 2. Comandos de Publicación

```bash
# Construir y verificar
./gradlew build

# Publicar a staging
./gradlew publishToSonatype

# Cerrar y liberar (publicar definitivamente)
./gradlew closeAndReleaseSonatypeStagingRepository
```

## Configuración GitHub Actions

### Secrets Requeridos
En GitHub Settings > Secrets and variables > Actions, agregar:

- `OSSRH_USERNAME`: Tu usuario de Sonatype
- `OSSRH_PASSWORD`: Tu password de Sonatype  
- `SIGNING_KEY`: Tu clave GPG privada (base64)
- `SIGNING_PASSWORD`: Password de tu clave GPG

### Proceso de Release

1. **Tag de versión**: `git tag v1.0.0 && git push origin v1.0.0`
2. **GitHub Actions** se ejecuta automáticamente
3. **Verificación** en [Nexus Repository Manager](https://s01.oss.sonatype.org/)
4. **Disponibilidad** en Maven Central (2-4 horas después)

## Estructura de Archivos Generados

```
build/libs/
├── copybara1-1.0.0.jar          # JAR principal
├── copybara1-1.0.0-sources.jar  # Código fuente
└── copybara1-1.0.0-javadoc.jar  # Documentación
```

## Verificación de Publicación

### Maven Central Search
- URL: https://search.maven.org/artifact/com.copybara/copybara1/1.0.0/jar

### Uso en otros proyectos

#### Gradle
```kotlin
dependencies {
    implementation("com.copybara:copybara1:1.0.0")
}
```

#### Maven
```xml
<dependency>
    <groupId>com.copybara</groupId>
    <artifactId>copybara1</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Troubleshooting

### Errores Comunes

1. **401 Unauthorized**: Verificar credenciales OSSRH
2. **GPG signing failed**: Verificar configuración de claves GPG
3. **POM validation errors**: Verificar que todos los campos requeridos estén completos

### Logs Útiles
```bash
# Ver detalles de publicación
./gradlew publishToSonatype --info

# Verificar signing
./gradlew signMavenPublication --info
```

## Recursos Adicionales

- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Gradle Nexus Publish Plugin](https://github.com/gradle-nexus/publish-plugin)
- [Maven Central Requirements](https://central.sonatype.org/publish/requirements/)
