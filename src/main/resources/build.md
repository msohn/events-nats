Build
=====

This plugin is built with Bazel.

Build in Gerrit tree
--------------------

Clone or link this plugin to the plugins directory of Gerrit's source
tree, and issue the command:

```
  bazel build plugins/events-nats
```

The output is created in

```
  bazel-genfiles/plugins/events-nats/events-nats.jar
```

This project can be imported into Eclipse IDE.
Add the plugin name to the `CUSTOM_PLUGINS` set in
Gerrit core in `tools/bzl/plugins.bzl`, and execute:

```
  ./tools/eclipse/project.py
```

How to build the Gerrit Plugin API is described in the [Gerrit
documentation](../../../Documentation/dev-bazel.html#_extension_and_plugin_api_jar_files).
