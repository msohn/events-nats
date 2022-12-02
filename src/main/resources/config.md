Configuration of the @PLUGIN@ plugin
======================

Configuration options can be configured in the Gerrit config file.

Sample config
---------------------

```
[plugin "@PLUGIN@"]
        server = nats://localhost:4222
```

The @PLUGIN@ plugin configuration can be defined in gerrit.config.

Configuration options
---------------------

`plugin.@PLUGIN@.server`
:   URL of NATS server, for productive use it is recommended to use a
    cluster of NATS servers  to improve availability and allow zero
    downtime updates of NATS. Repeat this option for the URL of each
    NATS server in the NATS cluster.
    Default: nats://localhost:4222

`plugin.@PLUGIN@.streamName`
:   Name of the NATS JetStream stream all events will be published on,
    the Gerrit serverId will be appended to ensure the stream name is unique.
    Example: `gerrit-f2f21467-a3db-4bb0-a5d0-1da41c09a6e8`.
    Default: gerrit

`plugin.@PLUGIN@.sendStreamEvents`
:   Whether to send stream events to the `streamEventsSubject` subject.
    Default: false

`plugin.@PLUGIN@.streamEventsSubject`
:   Name of the NATS subject gerrit stream-events will be published on.
    Default: gerrit_stream_events

`plugin.@PLUGIN@.publishAsync`
:   Whether to publish events asynchronously to NATS.
    Default: false

`plugin.@PLUGIN@.publishThreads`
:   Number of threads used for asynchronous publishing of events.
    Default: 1

`plugin.@PLUGIN@.shutdownTimeoutMs`
:   Timeout in milliseconds for graceful shutdown.
    Default: 30000

`plugin.@PLUGIN@.ackWaitMs`
:   The duration in milliseconds that the server will wait for an ack for any
    individual message once it has been delivered to a consumer. If an ack is
    not received in time, the message will be redelivered.
    Default: 30000

`plugin.@PLUGIN@.maxAckPending`
:   Defines the maximum number of messages, without an acknowledgement, that
    can be outstanding. Once this limit is reached message delivery will be
    suspended.
    Default: 1000
