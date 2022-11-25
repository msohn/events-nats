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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.api.ConsoleUI;
import com.google.gerrit.pgm.init.api.InitStep;
import com.google.gerrit.pgm.init.api.Section;
import com.google.gerrit.server.config.GerritInstanceIdProvider;
import com.google.inject.Inject;

public class InitConfig implements InitStep {
  private final Section pluginSection;
  private final String pluginName;
  private final ConsoleUI ui;
  private final GerritInstanceIdProvider gerritInstanceIdProvider;

  @Inject
  InitConfig(
      Section.Factory sections,
      @PluginName String pluginName,
      GerritInstanceIdProvider gerritInstanceIdProvider,
      ConsoleUI ui) {
    this.pluginName = pluginName;
    this.ui = ui;
    this.gerritInstanceIdProvider = gerritInstanceIdProvider;
    this.pluginSection = sections.get("plugin", pluginName);
  }

  @Override
  public void run() throws Exception {
    ui.header(String.format("%s plugin", pluginName));

    // pluginSection.string("AWS region (leave blank for default provider chain)", REGION_FIELD,
    // null);
    // pluginSection.string("AWS endpoint (dev or testing, not for production)", ENDPOINT_FIELD,
    // null);

    // boolean sendStreamEvents = ui.yesno(DEFAULT_SEND_STREAM_EVENTS, "Should send stream
    // events?");
    // pluginSection.set(SEND_STREAM_EVENTS_FIELD, Boolean.toString(sendStreamEvents));

    // if (sendStreamEvents) {
    //   pluginSection.string(
    //       "Stream events topic", STREAM_EVENTS_TOPIC_FIELD, DEFAULT_STREAM_EVENTS_TOPIC);
    // }
    // pluginSection.string(
    //     "Number of subscribers", NUMBER_OF_SUBSCRIBERS_FIELD, DEFAULT_NUMBER_OF_SUBSCRIBERS);
    // pluginSection.string("Application name", APPLICATION_NAME_FIELD, pluginName);
    // pluginSection.string("Initial position", INITIAL_POSITION_FIELD, DEFAULT_INITIAL_POSITION);
    // pluginSection.string(
    //     "Polling Interval (ms)",
    //     POLLING_INTERVAL_MS_FIELD,
    //     Long.toString(DEFAULT_POLLING_INTERVAL_MS));
    // pluginSection.string(
    //     "Maximum number of record to fetch",
    //     MAX_RECORDS_FIELD,
    //     Integer.toString(DEFAULT_MAX_RECORDS));

    // pluginSection.string(
    //     "Maximum total time waiting for a publish result (ms)",
    //     PUBLISH_SINGLE_REQUEST_TIMEOUT_MS_FIELD,
    //     Long.toString(DEFAULT_PUBLISH_SINGLE_REQUEST_TIMEOUT_MS));

    // pluginSection.string(
    //     "Maximum total time waiting for publishing, including retries",
    //     PUBLISH_TIMEOUT_MS_FIELD,
    //     Long.toString(DEFAULT_PUBLISH_TIMEOUT_MS));

    // pluginSection.string(
    //     "Maximum total time waiting when shutting down (ms)",
    //     SHUTDOWN_MS_FIELD,
    //     Long.toString(DEFAULT_SHUTDOWN_TIMEOUT_MS));
    // pluginSection.string(
    //     "Which level AWS libraries should log at",
    //     AWS_LIB_LOG_LEVEL_FIELD,
    //     DEFAULT_AWS_LIB_LOG_LEVEL.toString());

    // boolean sendAsync = ui.yesno(DEFAULT_SEND_ASYNC, "Should send messages asynchronously?");
    // pluginSection.set(SEND_ASYNC_FIELD, Boolean.toString(sendAsync));
  }
}
