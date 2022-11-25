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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventGson;
import com.google.gerrit.server.events.EventListener;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.NatsMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import net.javacrumbs.futureconverter.java8guava.FutureConverter;

@Singleton
class NatsPublisher implements EventListener {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final JetStream jetStream;
  private final Configuration configuration;
  private final Gson gson;
  private final ExecutorService callBackExecutor;

  @Inject
  public NatsPublisher(
      @EventGson Gson gson,
      Configuration configuration,
      JetStream jetStream,
      @PublishCallbackExecutor ExecutorService callBackExecutor) {
    this.gson = gson;
    this.jetStream = jetStream;
    this.configuration = configuration;
    this.callBackExecutor = callBackExecutor;
  }

  @Override
  public void onEvent(Event event) {
    String subject = configuration.getStreamEventsSubject();
    publish(subject, event);
    logger.atFine().log("NATS publisher - Successfully published Gerrit stream-event '%s'", event);
  }

  ListenableFuture<Boolean> publish(String subject, Event event) {
    NatsMessage msg =
        NatsMessage.builder()
            .subject(subject)
            .data(gson.toJson(event), StandardCharsets.UTF_8)
            .build();
    return configuration.isPublishAsync() ? publishAsync(msg) : publishSync(msg);
  }

  private ListenableFuture<Boolean> publishAsync(NatsMessage msg) {
    try {
      ListenableFuture<PublishAck> f =
          FutureConverter.toListenableFuture(jetStream.publishAsync(msg));

      Futures.addCallback(
          f,
          new FutureCallback<PublishAck>() {
            @Override
            public void onSuccess(PublishAck result) {
              logger.atFine().log("NATS publisher - Successfully published event '%s'", msg);
            }

            @Override
            public void onFailure(Throwable e) {
              logger.atSevere().withCause(e).log(
                  "NATS publisher - Failed publishing event %s", msg);
            }
          },
          callBackExecutor);

      return Futures.transform(f, res -> !res.hasError(), callBackExecutor);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("NATS publisher - Error when publishing event %s", msg);
      return Futures.immediateFailedFuture(e);
    }
  }

  private ListenableFuture<Boolean> publishSync(NatsMessage msg) {
    try {
      return Futures.immediateFuture(!jetStream.publish(msg).hasError());
    } catch (IOException | JetStreamApiException e) {
      logger.atSevere().withCause(e).log("NATS publisher - Failed publishing event : %s", msg);
      return Futures.immediateFailedFuture(e);
    }
  }
}
