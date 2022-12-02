// Copyright (C) 2022 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.nats;

import com.google.common.base.Strings;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritInstanceId;
import com.google.gerrit.server.config.GerritServerId;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.nats.client.JetStreamOptions;
import io.nats.client.Options;
import io.nats.client.PublishOptions;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;

@Singleton
class Configuration {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final boolean sendStreamEvents;
  private final String streamName;
  private final String streamEventsSubject;
  private final boolean isPublishAsync;
  private final int publishThreads;
  private final int shutdownTimeoutMs;

  private final Options natsOptions;
  private final JetStreamOptions jetStreamOptions;
  private final PublishOptions publishOptions;
  private final PushSubscribeOptions pushSubscribeOptions;

  private int ackWaitMs;

  private long maxAckPending;

  @Inject
  public Configuration(
      PluginConfigFactory configFactory,
      @PluginName String pluginName,
      @GerritServerId String gerritServerId,
      @Nullable @GerritInstanceId String gerritInstanceId) {
    PluginConfig pluginConfig = configFactory.getFromGerritConfig(pluginName);

    sendStreamEvents = pluginConfig.getBoolean("sendStreamEvents", false);
    streamName =
        String.format("%s-%s", pluginConfig.getString("streamName", "gerrit"), gerritServerId);
    streamEventsSubject =
        getStringParam(pluginConfig, "streamEventsSubject", "gerrit_stream_events");
    isPublishAsync = pluginConfig.getBoolean("publishAsync", false);
    publishThreads = pluginConfig.getInt("publishThreads", 1);
    shutdownTimeoutMs = pluginConfig.getInt("shutdownTimeoutMs", 30000);
    ackWaitMs = pluginConfig.getInt("ackWaitMs", 30000);
    maxAckPending = pluginConfig.getLong("maxAckPending", 1000L);

    natsOptions = new Options.Builder().servers(getServerUrls(pluginConfig)).build();
    jetStreamOptions = new JetStreamOptions.Builder().build();
    publishOptions = new PublishOptions.Builder().stream(streamEventsSubject).build();
    String consumerName = "instance-" + gerritInstanceId;
    pushSubscribeOptions =
        ConsumerConfiguration.builder()
            .durable(consumerName)
            .ackPolicy(AckPolicy.Explicit)
            .ackWait(ackWaitMs)
            .maxAckPending(maxAckPending)
            .deliverPolicy(DeliverPolicy.All)
            .buildPushSubscribeOptions();

    logger.atInfo().log(
        "NATS client configuration: sendStreamEvents: %b, streamName %s, streamEventsSubject: %s,"
            + " isPublishAsync: %b, publishThreads: %d, shutdownTimeoutMs: %d, ackWaitMs: %d,"
            + " maxAckPending: %d",
        sendStreamEvents,
        streamName,
        streamEventsSubject,
        isPublishAsync,
        publishThreads,
        shutdownTimeoutMs,
        ackWaitMs,
        maxAckPending);
  }

  private static String[] getServerUrls(PluginConfig pluginConfig) {
    String[] serverList = getStringListParam(pluginConfig, "server");
    if (serverList == null || serverList.length == 0) {
      serverList = new String[] {"nats://localhost:4222"};
    }
    return serverList;
  }

  private static String getStringParam(
      PluginConfig pluginConfig, String name, String defaultValue) {
    return Strings.isNullOrEmpty(System.getProperty(name))
        ? pluginConfig.getString(name, defaultValue)
        : System.getProperty(name);
  }

  private static String[] getStringListParam(PluginConfig pluginConfig, String name) {
    return Strings.isNullOrEmpty(System.getProperty(name))
        ? pluginConfig.getStringList(name)
        : new String[] {System.getProperty(name)};
  }

  public Options getNatsOptions() {
    return natsOptions;
  }

  /**
   * Get the configured Nats publish options
   *
   * @return the configured Nats publish options
   */
  public PublishOptions getPublishOptions() {
    return publishOptions;
  }

  /**
   * Get the configured number of threads for publishing events to Nats
   *
   * @return the configured number of threads for publishing events to Nats
   */
  public int getPublishThreads() {
    return publishThreads;
  }

  /**
   * Whether stream-events should be published asynchronously
   *
   * @return whether stream-events should be published asynchronously
   */
  public Boolean isPublishAsync() {
    return isPublishAsync;
  }

  /**
   * Whether to publish stream-events
   *
   * @return whether to publish stream-events
   */
  public Boolean isSendStreamEvents() {
    return sendStreamEvents;
  }

  public String getStreamName() {
    return streamName;
  }

  public JetStreamOptions getJetStreamOptions() {
    return jetStreamOptions;
  }
  /**
   * Get the subject stream-events are published to
   *
   * @return subject stream-events are published to
   */
  public String getStreamEventsSubject() {
    return streamEventsSubject;
  }

  /**
   * Get the push subscribe options
   *
   * @return the push subscribe options
   */
  public PushSubscribeOptions getPushSubscribeOptions() {
    return pushSubscribeOptions;
  }

  int getShutdownTimeoutMs() {
    return shutdownTimeoutMs;
  }
}
