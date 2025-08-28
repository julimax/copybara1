# Aplicación Kotlin Hello World

Una aplicación básica de "Hola Mundo" en Kotlin dentro del proyecto Copybara.

## Archivos

- `src/main/kotlin/Main.kt` - Código principal de la aplicación Kotlin
- `build.gradle.kts` - Script de construcción con Gradle
- `README_KOTLIN.md` - Este archivo con instrucciones

## Requisitos

- Java 11 o superior
- Kotlin compiler (kotlinc) o Gradle

## Ejecutar la aplicación

### Opción 1: Con kotlinc (compilador directo)
```bash
# Compilar
kotlinc Main.kt -include-runtime -d main.jar

# Ejecutar
java -jar main.jar
```

### Opción 2: Con Gradle (recomendado)
```bash
# Ejecutar directamente
./gradlew run

# O compilar y luego ejecutar
./gradlew build
java -jar build/libs/copybara1-1.0.0.jar
```

## Salida esperada

```
¡Hola Mundo desde Kotlin!
Hello World from Kotlin!
Aplicación: Copybara v1.0
¡Hola, Desarrollador! Bienvenido a Kotlin.
```

## Características del código

- Función `main()` básica
- Uso de variables (`val`)
- Interpolación de strings con `$`
- Función personalizada `saludar()`
- Mensajes en español e inglés
