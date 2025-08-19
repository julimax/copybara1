# Configuración de GitHub Secrets para Maven Central

## Secrets Requeridos

Para que GitHub Actions pueda publicar automáticamente a Maven Central, necesitas configurar estos secrets en tu repositorio de GitHub:

### 1. Ir a GitHub Repository Settings
1. Ve a tu repositorio en GitHub
2. Click en **Settings** (tab superior)
3. En el menú lateral, click en **Secrets and variables** > **Actions**
4. Click en **New repository secret**

### 2. Configurar los Secrets

#### OSSRH_USERNAME
- **Name**: `OSSRH_USERNAME`
- **Secret**: `qdu9I6`

#### OSSRH_PASSWORD  
- **Name**: `OSSRH_PASSWORD`
- **Secret**: `9znKG6lrg8H2t3rsqVZk47702h1fXZsCb`

#### SIGNING_KEY (GPG Key)
- **Name**: `SIGNING_KEY`
- **Secret**: Tu clave GPG privada en formato base64

Para generar la clave GPG:
```bash
# Generar nueva clave GPG
gpg --batch --gen-key --pinentry-mode loopback << EOF
%echo Generating GPG key for Maven Central
Key-Type: RSA
Key-Length: 4096
Name-Real: Copybara Team
Name-Email: team@copybara.com
Expire-Date: 2y
Passphrase: copybara123
%commit
%echo Done
EOF

# Obtener el Key ID
gpg --list-secret-keys --keyid-format LONG

# Exportar clave privada en base64
gpg --export-secret-keys YOUR_KEY_ID | base64 -w 0
```

#### SIGNING_PASSWORD
- **Name**: `SIGNING_PASSWORD`  
- **Secret**: `copybara123` (o la contraseña que uses para tu clave GPG)

## Proceso de Release Automático

Una vez configurados los secrets:

### 1. Crear Release Tag
```bash
git tag v1.0.0
git push origin v1.0.0
```

### 2. GitHub Actions se ejecuta automáticamente
- Compila el proyecto
- Ejecuta tests
- Firma los JARs con GPG
- Publica a Maven Central

### 3. Verificar Publicación
- Revisa el workflow en GitHub Actions
- Verifica en [Sonatype Nexus](https://s01.oss.sonatype.org/)
- Busca en [Maven Central](https://search.maven.org/) (disponible en 2-4 horas)

## Comandos de Verificación Local

Para probar localmente antes del release:

```bash
# Compilar
./gradlew build

# Publicar a staging (sin release)
./gradlew publishToSonatype

# Ver qué se va a publicar
./gradlew publishToSonatype --dry-run
```

## Estructura Final del Proyecto

```
copybara1/
├── .github/workflows/release.yml    # GitHub Actions workflow
├── gradle.properties               # Configuración local (variables)
├── build.gradle.kts                # Configuración de build y publishing
├── Main.kt                         # Código fuente
└── GITHUB_SECRETS_SETUP.md         # Esta guía
```

## Troubleshooting

### Error: "gpg: signing failed"
- Verificar que SIGNING_KEY y SIGNING_PASSWORD están correctos
- Asegurar que la clave GPG no haya expirado

### Error: "401 Unauthorized"
- Verificar OSSRH_USERNAME y OSSRH_PASSWORD
- Confirmar que la cuenta Sonatype está activa

### Error: "POM validation failed"
- Verificar que todos los campos del POM están completos
- Revisar que la URL del repositorio sea accesible
