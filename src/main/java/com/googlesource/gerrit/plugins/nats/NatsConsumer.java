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

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventGson;
import com.google.gson.Gson;
import com.google.inject.Inject;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.ProviderException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;

// import software.amazon.kinesis.coordinator.Scheduler;

class NatsConsumer {
  interface Factory {
    NatsConsumer create(String topic, Consumer<Event> messageProcessor);
  }

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final Configuration config;

  private java.util.function.Consumer<Event> messageProcessor;
  private String subjectName;
  private AtomicBoolean resetOffset = new AtomicBoolean(false);
  private final Connection connection;
  private final JetStream jetStream;
  private final JetStreamManagement jetStreamManagement;
  private JetStreamSubscription subscription;
  private final Dispatcher dispatcher;
  private final Gson gson;

  @Inject
  public NatsConsumer(
      @EventGson Gson gson,
      Configuration config,
      Connection connection,
      JetStream jetStream,
      JetStreamManagement jetStreamManagement) {
    this.gson = gson;
    this.config = config;
    this.connection = connection;
    this.jetStream = jetStream;
    this.jetStreamManagement = jetStreamManagement;
    // create one dispatcher per consumer
    this.dispatcher = connection.createDispatcher();
  }

  public void subscribe(String subjectName, java.util.function.Consumer<Event> messageProcessor) {
    this.subjectName = subjectName;
    this.messageProcessor = messageProcessor;
    String subject = getOrCreateSubject(subjectName);
    subscription = runReceiver(messageProcessor, subject);
    logger.atInfo().log("NATS consumer - subscribed to subject [%s]", subjectName);
  }

  private String getOrCreateSubject(String name) {
    try {
      StreamInfo stream = jetStreamManagement.getStreamInfo(config.getStreamName());
      StreamConfiguration sc = stream.getConfiguration();
      List<String> subjects = sc.getSubjects();
      if (!subjects.stream().anyMatch(n -> n.equals(name))) {
        addSubject(sc, name);
      }
      return name;
    } catch (IOException | JetStreamApiException e) {
      throw new ProviderException(e);
    }
  }

  private void addSubject(StreamConfiguration sc, String name)
      throws IOException, JetStreamApiException {
    List<String> subjects = sc.getSubjects();
    subjects.add(name);
    sc = StreamConfiguration.builder(sc).subjects(subjects).build();
    jetStreamManagement.updateStream(sc);
    logger.atInfo().log(
        "NATS consumer - added subject [%s] to stream [%s], subjects: [%s]",
        name, sc.getName(), subjects);
  }

  private JetStreamSubscription runReceiver(
      java.util.function.Consumer<Event> messageProcessor, String subject) {
    try {
      return jetStream.subscribe(
          subject,
          dispatcher,
          new MessageHandler() {

            @Override
            public void onMessage(Message msg) throws InterruptedException {
              String json = new String(msg.getData(), StandardCharsets.UTF_8);
              Event e = gson.fromJson(json, Event.class);
              messageProcessor.accept(e);
              msg.ack();
              logger.atFine().log("NATS consumer - consumed and acked event '%s'", e);
            }
          },
          false);
    } catch (IOException | JetStreamApiException e) {
      logger.atSevere().withCause(e).log(
          "NATS consumer - subscribing to subject [%s] failed", subject);
      return null;
    }
  }

  public void shutdown() {
    try {
      subscription.unsubscribe();
      connection.closeDispatcher(dispatcher);
      logger.at(Level.INFO).log(
          "NATS consumer - consumer for subject '%s' was shutdown", subjectName);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log(
          "NATS consumer - error caught when shutting down consumer for subject %s",
          getStreamName());
    }
    logger.atInfo().log("NATS consumer - shutdown consumer of subject %s completed.", subjectName);
  }

  public java.util.function.Consumer<Event> getMessageProcessor() {
    return messageProcessor;
  }

  public String getStreamName() {
    // we map BrokerApi streams to NATS subjects
    return subjectName;
  }

  public void resetOffset() {
    // Move all checkpoints (if any) to TRIM_HORIZON, so that the consumer
    // scheduler will start consuming from beginning.
    // checkpointResetter.setAllShardsToBeginning(streamName);

    // Even when no checkpoints have been persisted, instruct the consumer
    // scheduler to start from TRIM_HORIZON, irrespective of 'initialPosition'
    // configuration.
    resetOffset.set(true);
  }
}
