load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "events-nats",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: events-nats",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.nats.InitConfig",
        "Gerrit-Module: com.googlesource.gerrit.plugins.nats.Module",
        "Implementation-Title: Gerrit NATS plugin",
        "Implementation-URL: https://gerrit.googlesource.com/plugins/events-nats",
    ],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@events-broker//jar",
        "@future-converter-java8-guava//jar",
        "@future-converter-common//jar",
        "@future-converter-java8-common//jar",
        "@future-converter-guava-common//jar",
        "@nats-client//jar",
    ],
)

junit_tests(
    name = "events_nats_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["events-nats"],
    deps = [
        ":events-nats__plugin_test_deps",
        "@events-broker//jar",
        "@nats-client//jar",
    ],
)

java_library(
    name = "events-nats__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":events-nats__plugin",
        "@jackson-annotations//jar",
        "@testcontainers//jar",
        "@docker-java-api//jar",
        "@docker-java-transport//jar",
    ],
)
