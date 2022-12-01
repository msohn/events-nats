events-nats: Gerrit event producer for NATS
=======================

* Author: SAP SE
* Repository: https://gerrit.googlesource.com/plugins/events-nats
* CI/Release: https://gerrit-ci.gerritforge.com/search/?q=events-nats

[![Build Status](https://gerrit-ci.gerritforge.com/job/plugin-events-nats-bazel-master/lastBuild/badge/icon)](https://gerrit-ci.gerritforge.com/job/plugin-events-nats-bazel-master/lastBuild/)

Synopsis
----------------------

This plugins allows to define a distributed stream of events
published by Gerrit.

Events can be anything, from the traditional stream events
to the Gerrit metrics.

This plugin requires NATS Jetstream and Gerrit 3.6 or later.

Environments
---------------------

* `linux`
* `java-11`
* `Bazel`

Build
---------------------
The events-nats plugin can be built as a regular 'in-tree' plugin. That means it is required to
clone a Gerrit source tree first and then to clone the events-nats plugin source directory into
the /plugins path. Additionally, the `plugins/external_plugin_deps.bzl` file needs to be
updated to include the events-nats plugin external dependencies.

    git clone --recursive https://gerrit.googlesource.com/gerrit
    git clone https://gerrit.googlesource.com/plugins/events-nats gerrit/plugins/events-nats
    cd gerrit
    rm plugins/external_plugin_deps.bzl
    ln -s ./events-nats/external_plugin_deps.bzl plugins/.

If your build includes multiple plugins requiring external dependencies edit `plugins/external_plugin_deps.bzl`.
E.g. to include dependencies of both the events-nats and the multi-site plugin:

    load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_CENTRAL", "MAVEN_LOCAL", "maven_jar")
    load("//plugins/events-nats:external_plugin_deps.bzl", deps_events_nats = "external_plugin_deps")
    load("//plugins/multi-site:external_plugin_deps.bzl", deps_multi_site = "external_plugin_deps")

    def external_plugin_deps():
        deps_events_nats()
        deps_multi_site()

To build the events-nats plugins, issue this command from the Gerrit source path:

    bazel build plugins/events-nats

The output is created in

    bazel-genfiles/plugins/events-nats/events-nats.jar

Minimum Configuration
---------------------
Assuming a running NATS JetStream broker on the same Gerrit host, add the following
settings to `etc/gerrit.config`:

```
    [plugin "events-nats"]
        server = nats://localhost:4222
```

Testing
---------------------

Starting a local NATS JetStream server for testing:

```
    docker run -d --name nats-main -p 4222:4222 -p 6222:6222 -p 8222:8222 nats -js
```

You can use the [NATS command line client](https://github.com/nats-io/natscli) to inspect and manage NATS.
