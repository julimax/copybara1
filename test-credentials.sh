#!/bin/bash
set -e

# Configuración
NAMESPACE="io.github.julimax"
PROJECT_NAME="copybara"
VERSION="1.0.6"
BASE_URL="https://central.sonatype.com/api/v1/publisher"

# Credenciales confirmadas
OSSRH_USERNAME="wUXgFk"
OSSRH_PASSWORD="uFIJDevyT4AOmqj4uQhpikTwSnBMEikif"

echo "🚀 Probando subida a Maven Central..."
echo "   Namespace: $NAMESPACE"
echo "   Project: $PROJECT_NAME"
echo "   Version: $VERSION"

# Verificar artifacts
JAR_FILE="build/libs/${PROJECT_NAME}-${VERSION}.jar"
POM_FILE="build/publications/maven/pom-default.xml"

if [[ ! -f "$JAR_FILE" ]]; then
    echo "❌ Error: No se encontró el JAR: $JAR_FILE"
    exit 1
fi

if [[ ! -f "$POM_FILE" ]]; then
    echo "❌ Error: No se encontró el POM: $POM_FILE"
    exit 1
fi

echo "✅ Artifacts encontrados:"
echo "   JAR: $JAR_FILE ($(du -h $JAR_FILE | cut -f1))"
echo "   POM: $POM_FILE ($(du -h $POM_FILE | cut -f1))"

# Crear bundle temporal
BUNDLE_DIR="build/maven-central-bundle"
mkdir -p "$BUNDLE_DIR"

# Copiar artifacts al bundle con nombres correctos
cp "$JAR_FILE" "$BUNDLE_DIR/${PROJECT_NAME}-${VERSION}.jar"
cp "$POM_FILE" "$BUNDLE_DIR/${PROJECT_NAME}-${VERSION}.pom"

# Crear el bundle ZIP
BUNDLE_FILE="build/${PROJECT_NAME}-${VERSION}-bundle.zip"
cd "$BUNDLE_DIR"
zip -r "../$(basename "$BUNDLE_FILE")" .
cd - > /dev/null

echo "📦 Bundle creado: $BUNDLE_FILE ($(du -h $BUNDLE_FILE | cut -f1))"

# Debug de credenciales
echo "🔍 Debug de credenciales:"
echo "   Username: $OSSRH_USERNAME"
echo "   Password length: ${#OSSRH_PASSWORD}"
echo "   Password starts with: ${OSSRH_PASSWORD:0:8}..."

# Subir el bundle
echo "📤 Probando subida a Maven Central..."
echo "🔗 URL: $BASE_URL/upload"

UPLOAD_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST \
    "$BASE_URL/upload" \
    -H "Authorization: Bearer $OSSRH_PASSWORD" \
    -H "User-Agent: Local-Test/1.0" \
    -F "bundle=@$BUNDLE_FILE" \
    -F "name=$PROJECT_NAME" \
    -F "namespace=$NAMESPACE")

HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$UPLOAD_RESPONSE" | head -n -1)

echo "📋 Código HTTP: $HTTP_CODE"
echo "📋 Respuesta: $RESPONSE_BODY"

if [[ "$HTTP_CODE" == "201" ]]; then
    echo "✅ ¡Éxito! Bundle subido correctamente a Maven Central"
    
    # Extraer deployment ID si está disponible
    if command -v jq >/dev/null 2>&1; then
        DEPLOYMENT_ID=$(echo "$RESPONSE_BODY" | jq -r '.deploymentId // empty' 2>/dev/null)
        if [[ -n "$DEPLOYMENT_ID" ]]; then
            echo "🆔 Deployment ID: $DEPLOYMENT_ID"
            echo "🔗 Puedes verificar el estado en: https://central.sonatype.com/publishing/deployments"
        fi
    fi
else
    echo "❌ Error al subir bundle. Código HTTP: $HTTP_CODE"
    echo "📋 Respuesta completa: $RESPONSE_BODY"
    
    # Análisis del error
    if [[ "$HTTP_CODE" == "401" ]]; then
        echo "🔍 Error 401: Problema de autenticación"
        echo "   - Verifica que el token sea válido"
        echo "   - Verifica que el token tenga permisos de publicación"
    elif [[ "$HTTP_CODE" == "403" ]]; then
        echo "🔍 Error 403: Problema de permisos"
        echo "   - Verifica que tengas acceso al namespace: $NAMESPACE"
    elif [[ "$HTTP_CODE" == "400" ]]; then
        echo "🔍 Error 400: Problema con los datos enviados"
        echo "   - Verifica el formato del bundle"
        echo "   - Verifica el namespace y nombre del proyecto"
    fi
    
    exit 1
fi
