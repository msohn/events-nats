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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import java.io.IOException;
import java.security.ProviderException;

@Singleton
public class NatsJetStreamProvider implements Provider<JetStream> {

  private Connection connection;

  @Inject
  public NatsJetStreamProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public JetStream get() {
    try {
      return connection.jetStream();
    } catch (IOException e) {
      throw new ProviderException(e);
    }
  }
}
