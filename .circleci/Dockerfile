# ==== builder: compila Copybara con Bazelisk ====
FROM ubuntu:24.04 AS builder

ARG DEBIAN_FRONTEND=noninteractive
ARG COPYBARA_TAG=v20250818

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates curl git jq unzip zip \
    openjdk-11-jdk \
    bash coreutils tar gnupg \
    build-essential gcc g++ make \
    python3 python3-pip \
  && rm -rf /var/lib/apt/lists/*

# Instala Bazelisk
RUN curl -fsSL https://github.com/bazelbuild/bazelisk/releases/latest/download/bazelisk-linux-amd64 \
    -o /usr/local/bin/bazelisk && chmod +x /usr/local/bin/bazelisk \
 && ln -s /usr/local/bin/bazelisk /usr/local/bin/bazel

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH="/usr/local/bin:${PATH}"

# Crear usuario no root para la compilación
RUN useradd -ms /bin/bash builduser

# Clona y "checkout" de Copybara
WORKDIR /opt
RUN git clone https://github.com/google/copybara.git
RUN chown -R builduser:builduser /opt/copybara
WORKDIR /opt/copybara

# Cambiar a usuario no root
USER builduser

# Intenta tag; si no existe, usa master
RUN git fetch --tags --quiet || true \
 && (git checkout "${COPYBARA_TAG}" 2>/dev/null || git checkout origin/master)

# Compila Copybara
RUN bazelisk shutdown || true \
 && bazelisk build //java/com/google/copybara

# ==== final: imagen mínima con runtime y binarios ====
FROM ubuntu:24.04

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates curl git jq \
    openjdk-21-jre-headless \
    bash tini \
  && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="/usr/local/bin:${PATH}"

# Copia el binario de Copybara y sus runfiles
COPY --from=builder /opt/copybara/bazel-bin/java/com/google/copybara/copybara /usr/local/bin/copybara
COPY --from=builder /opt/copybara/bazel-bin/java/com/google/copybara/copybara.runfiles /usr/local/bin/copybara.runfiles
RUN chmod +x /usr/local/bin/copybara

# Configurar variables de entorno para runfiles
ENV JAVA_RUNFILES=/usr/local/bin/copybara.runfiles

# Usuario no root
RUN useradd -ms /bin/bash circleci
USER circleci
WORKDIR /workspace

# Tini como init; deja bash por defecto para pasar comandos
ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["/bin/bash"]
