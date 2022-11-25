load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "nats-client",
        artifact = "io.nats:jnats:2.16.4",
        sha1 = "dba5dd08f374d84cd54854610f9895abc2a19e80",
    )

    FUTURE_CONVERTER_VERSION = "1.2.0"

    maven_jar(
        name = "future-converter-java8-guava",
        artifact = "net.javacrumbs.future-converter:future-converter-java8-guava:" + FUTURE_CONVERTER_VERSION,
        sha1 = "9d6d59ee4e8f337ccf69ddd66e291a1ef77fbf4e",
    )

    maven_jar(
        name = "future-converter-common",
        artifact = "net.javacrumbs.future-converter:future-converter-common:" + FUTURE_CONVERTER_VERSION,
        sha1 = "5fc7ea7c58ee0ce950e6104d5dda899f81959d7b",
    )

    maven_jar(
        name = "future-converter-java8-common",
        artifact = "net.javacrumbs.future-converter:future-converter-java8-common:" + FUTURE_CONVERTER_VERSION,
        sha1 = "575932e773d58ddd459af417b2df31e2d07c4afc",
    )

    maven_jar(
        name = "future-converter-guava-common",
        artifact = "net.javacrumbs.future-converter:future-converter-guava-common:" + FUTURE_CONVERTER_VERSION,
        sha1 = "b329c26e298bd77994cc2e304e4ac20da6f1569f",
    )

    TESTCONTAINERS_VERSION = "1.15.3"

    maven_jar(
        name = "testcontainers",
        artifact = "org.testcontainers:testcontainers:" + TESTCONTAINERS_VERSION,
        sha1 = "95c6cfde71c2209f0c29cb14e432471e0b111880",
    )

    DOCKER_JAVA_VERS = "3.2.8"

    maven_jar(
        name = "docker-java-api",
        artifact = "com.github.docker-java:docker-java-api:" + DOCKER_JAVA_VERS,
        sha1 = "4ac22a72d546a9f3523cd4b5fabffa77c4a6ec7c",
    )

    maven_jar(
        name = "docker-java-transport",
        artifact = "com.github.docker-java:docker-java-transport:" + DOCKER_JAVA_VERS,
        sha1 = "c3b5598c67d0a5e2e780bf48f520da26b9915eab",
    )

    maven_jar(
        name = "jackson-annotations",
        artifact = "com.fasterxml.jackson.core:jackson-annotations:2.10.3",
        sha1 = "0f63b3b1da563767d04d2e4d3fc1ae0cdeffebe7",
    )

    maven_jar(
        name = "events-broker",
        artifact = "com.gerritforge:events-broker:3.6.3",
        sha1 = "2a78d4492810d5b4280c6a92e6b8bbdadaffe7d2",
    )
