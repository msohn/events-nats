This plugin publishes gerrit stream events to subjects on a NATS JetStream stream.

It provides a NATS-based implementation of a generic
[Events Broker Api](https://github.com/GerritForge/events-broker) which can be used by
Gerrit and other plugins.

Use-cases
=========

CI/CD Validation
----------------

Gerrit stream events can be published to the internal network where other subscribers
can trigger automated jobs (e.g. CI/CD validation) for fetching the changes and validating
them through build and testing.

__NOTE__: This use-case requires a CI/CD system (e.g. Jenkins, Zuul or other) and
the development of a NATS-based subscriber receiving events and triggering a build.

Events replication
------------------

Multiple Gerrit masters in a multi-site setup can be informed on the stream events
happening on every node thanks to the notification to a NATS subject.

__NOTE__: This use-case requires the [multi-site plugin](https://gerrit.googlesource.com/plugins/multi-site)
on each of the Gerrit masters that are part of the same multi-site cluster.

Pull replication
------------------

Events published to NATS can be used to trigger the pull-replication plugin to git fetch
updates from configured remotes.

__NOTE__: This use-case requires the [pull-replication plugin](https://gerrit.googlesource.com/plugins/pull-replication)
on each of the Gerrit instances participating in the replication.
