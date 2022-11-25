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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gerrit.server.logging.LoggingContextAwareExecutorService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class PublishCallbackExecutorProvider implements Provider<ExecutorService> {

  private final Configuration configuration;

  @Inject
  PublishCallbackExecutorProvider(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public ExecutorService get() {
    return new LoggingContextAwareExecutorService(
        Executors.newFixedThreadPool(
            configuration.getPublishThreads(),
            new ThreadFactoryBuilder().setNameFormat("nats-publish-callback-executor-%d").build()));
  }
}
