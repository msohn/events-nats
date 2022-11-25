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

This plugin requires Gerrit 3.6 or later.

Environments
---------------------

* `linux`
* `java-11`
* `Bazel`

Build
---------------------
events-nats plugin can be build as a regular 'in-tree' plugin. That means that is required to
clone a Gerrit source tree first and then to have the events-nats plugin source directory into
the /plugins path. Additionally, the plugins/external_plugin_deps.bzl file needs to be
updated to match the events-nats plugin one.

    git clone --recursive https://gerrit.googlesource.com/gerrit
    git clone https://gerrit.googlesource.com/plugins/events-nats gerrit/plugins/events-nats
    cd gerrit
    rm plugins/external_plugin_deps.bzl
    ln -s ./events-nats/external_plugin_deps.bzl plugins/.

To build the events-nats plugins, issue the command from the Gerrit source path:

    bazel build plugins/events-nats

The output is created in

    bazel-genfiles/plugins/events-nats/events-nats.jar

Minimum Configuration
---------------------
Assuming a running NATS JetStream broker on the same Gerrit host, add the following
settings to gerrit.config:

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
