# Imagen simple con herramientas para compilar Copybara (como cimg/openjdk pero personalizada)
FROM ubuntu:24.04

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates curl git jq unzip zip \
    openjdk-11-jdk \
    bash coreutils tar gnupg \
    build-essential gcc g++ make \
    python3 python3-pip \
    tini \
  && rm -rf /var/lib/apt/lists/*

# Instala Bazelisk
RUN curl -fsSL https://github.com/bazelbuild/bazelisk/releases/latest/download/bazelisk-linux-amd64 \
    -o /usr/local/bin/bazelisk && chmod +x /usr/local/bin/bazelisk \
 && ln -s /usr/local/bin/bazelisk /usr/local/bin/bazel

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH="/usr/local/bin:${PATH}"

# Usuario no root
RUN useradd -ms /bin/bash circleci
USER circleci
WORKDIR /workspace

# Tini como init; deja bash por defecto para pasar comandos
ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["/bin/bash"]
