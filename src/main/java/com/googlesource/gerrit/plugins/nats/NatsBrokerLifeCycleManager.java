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

import com.gerritforge.gerrit.eventbroker.BrokerApi;
import com.gerritforge.gerrit.eventbroker.TopicSubscriber;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import java.io.IOException;
import java.security.ProviderException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Singleton
class NatsBrokerLifeCycleManager implements LifecycleListener {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final Configuration config;
  private final Connection connection;
  private final Set<TopicSubscriber> consumers;
  private final BrokerApi brokerApi;
  private JetStreamManagement jetStreamManagement;

  @Inject
  public NatsBrokerLifeCycleManager(
      Configuration config,
      JetStreamManagement jetStreamManagement,
      Connection connection,
      BrokerApi brokerApi,
      Set<TopicSubscriber> consumers) {
    this.config = config;
    this.jetStreamManagement = jetStreamManagement;
    this.connection = connection;
    this.brokerApi = brokerApi;
    this.consumers = consumers;
  }

  @Override
  public void start() {
    String streamName = config.getStreamName();
    if (!streamExists(streamName)) {
      try {
        jetStreamManagement.addStream(
            StreamConfiguration.builder()
                .name(streamName)
                .storageType(StorageType.File)
                .addSubjects(config.getStreamEventsSubject())
                .build());
        logger.at(Level.INFO).log("Created new persistent NATS JetStream '%s'", streamName);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (JetStreamApiException e) {
        e.printStackTrace();
      }
    }
    consumers.forEach(
        topicSubscriber ->
            brokerApi.receiveAsync(topicSubscriber.topic(), topicSubscriber.consumer()));
  }

  private boolean streamExists(String streamName) {
    try {
      StreamInfo info = jetStreamManagement.getStreamInfo(streamName);
      logger.at(Level.INFO).log(
          "NATS JetStream '%s' exists, description: '%s', created: '%s'",
          streamName,
          info.getDescription(),
          info.getCreateTime().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
      return true;
    } catch (JetStreamApiException e) {
      if (e.getErrorCode() == 404) {
        return false;
      }
      throw new ProviderException(e);
    } catch (IOException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public void stop() {
    drainConnection();
    brokerApi.disconnect();
    try {
      connection.close();
    } catch (InterruptedException e) {
      logger.at(Level.SEVERE).withCause(e).log(
          "NATS broker - stopping connection failed with error");
    }
  }

  private void drainConnection() {
    try {
      int shutdownTimeoutMs = config.getShutdownTimeoutMs();
      Duration shutdownTimeout = Duration.ofMillis(shutdownTimeoutMs);
      logger.atInfo().log(
          "NATS consumer - Waiting up to '%s' milliseconds to drain connection to stream '%s'",
          shutdownTimeoutMs, config.getStreamName());
      CompletableFuture<Boolean> f = connection.drain(shutdownTimeout);
      f.get(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      logger.at(Level.WARNING).withCause(e).log("NATS broker - graceful shutdown failed with timeout");
    } catch (InterruptedException | ExecutionException e) {
      logger.at(Level.SEVERE).withCause(e).log("NATS broker - graceful shutdown failed with error");
    }
  }
}
