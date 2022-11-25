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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gerrit.server.events.Event;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class NatsBrokerApi implements BrokerApi {
  private final NatsConsumer.Factory consumerFactory;

  private final NatsPublisher publisher;
  private final Set<NatsConsumer> consumers;

  @Inject
  public NatsBrokerApi(NatsPublisher publisher, NatsConsumer.Factory consumerFactory) {
    this.publisher = publisher;
    this.consumerFactory = consumerFactory;
    this.consumers = Collections.newSetFromMap(new ConcurrentHashMap<>());
  }

  @Override
  public ListenableFuture<Boolean> send(String streamName, Event event) {
    return publisher.publish(streamName, event);
  }

  @Override
  public void receiveAsync(String streamName, Consumer<Event> eventConsumer) {
    NatsConsumer consumer = consumerFactory.create(streamName, eventConsumer);
    consumers.add(consumer);
    consumer.subscribe(streamName, eventConsumer);
  }

  @Override
  public Set<TopicSubscriber> topicSubscribers() {
    return consumers.stream()
        .map(s -> TopicSubscriber.topicSubscriber(s.getStreamName(), s.getMessageProcessor()))
        .collect(Collectors.toSet());
  }

  @Override
  public void disconnect() {
    consumers.parallelStream().forEach(NatsConsumer::shutdown);
    consumers.clear();
  }

  @Override
  public void replayAllEvents(String topic) {
    consumers.stream()
        .filter(subscriber -> topic.equals(subscriber.getStreamName()))
        .forEach(NatsConsumer::resetOffset);
  }
}
