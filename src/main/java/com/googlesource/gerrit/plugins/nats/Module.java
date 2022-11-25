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

import static com.google.inject.Scopes.SINGLETON;

import com.gerritforge.gerrit.eventbroker.BrokerApi;
import com.gerritforge.gerrit.eventbroker.TopicSubscriber;
import com.google.common.collect.Sets;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.registration.DynamicItem;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.gerrit.server.events.EventListener;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamManagement;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class Module extends LifecycleModule {
  private final Configuration configuration;
  private Set<TopicSubscriber> activeConsumers = Sets.newHashSet();

  @Inject
  Module(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * By default the events-broker library (loaded directly by the multi-site) registers a noop
   * implementation, which itself registers a list of topic subscribers. Since we have no control
   * over which plugin is loaded first (multi-site or events-aws-kinesis), we deal with the
   * possibility that a broker api was already registered and, in that case, reassign its consumers
   * to the events-aws-kinesis plugin. The injection is optional because if the events-aws-kinesis
   * plugin is loaded first, then there will be no previously registered broker API.
   *
   * @param previousBrokerApi
   */
  @Inject(optional = true)
  public void setPreviousBrokerApi(DynamicItem<BrokerApi> previousBrokerApi) {
    if (previousBrokerApi != null && previousBrokerApi.get() != null) {
      this.activeConsumers = previousBrokerApi.get().topicSubscribers();
    }
  }

  @Override
  protected void configure() {
    factory(NatsConsumer.Factory.class);
    bind(ExecutorService.class)
        .annotatedWith(PublishCallbackExecutor.class)
        .toProvider(PublishCallbackExecutorProvider.class)
        .in(SINGLETON);
    bind(Connection.class).toProvider(NatsClientProvider.class).in(SINGLETON);
    bind(JetStream.class).toProvider(NatsJetStreamProvider.class).in(SINGLETON);
    bind(JetStreamManagement.class).toProvider(JetStreamManagementProvider.class);
    bind(new TypeLiteral<Set<TopicSubscriber>>() {}).toInstance(activeConsumers);
    DynamicItem.bind(binder(), BrokerApi.class).to(NatsBrokerApi.class).in(SINGLETON);
    DynamicSet.bind(binder(), LifecycleListener.class).to(NatsBrokerLifeCycleManager.class);
    if (configuration.isSendStreamEvents()) {
      DynamicSet.bind(binder(), EventListener.class).to(NatsPublisher.class);
    }
  }
}
