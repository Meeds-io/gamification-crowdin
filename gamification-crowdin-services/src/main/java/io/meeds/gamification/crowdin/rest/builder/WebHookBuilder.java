/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.crowdin.rest.builder;

import io.meeds.gamification.crowdin.model.RemoteProject;
import io.meeds.gamification.crowdin.model.WebHook;
import io.meeds.gamification.crowdin.rest.model.WebHookRestEntity;
import io.meeds.gamification.crowdin.services.WebhookService;
import io.meeds.gamification.crowdin.storage.CrowdinConsumerStorage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class WebHookBuilder {

  private static final Log LOG                = ExoLogger.getLogger(WebHookBuilder.class);

  @Autowired
  private CrowdinConsumerStorage crowdinConsumerStorage;

  private WebHookBuilder() {
    // Class with static methods
  }

  public WebHookRestEntity toRestEntity(WebhookService webhookService, WebHook webHook) {
    if (webHook == null) {
      return null;
    }
    RemoteProject remoteProject = null;
    try {
      remoteProject = crowdinConsumerStorage.retrieveRemoteProject(webHook.getProjectId(), webHook.getToken());
    } catch (IllegalAccessException e) {
      LOG.error(e);
    }

    return new WebHookRestEntity(webHook.getId(),
                                 webHook.getWebhookId(),
                                 webHook.getProjectId(),
                                 webHook.getTriggers(),
                                 webHook.getEnabled(),
                                 webHook.getWatchedDate(),
                                 webHook.getWatchedBy(),
                                 webHook.getUpdatedDate(),
                                 webHook.getRefreshDate(),
                                 webHook.getProjectName(),
                                 remoteProject != null ? remoteProject.getIdentifier() : null,
                                 remoteProject != null ? remoteProject.getDescription() : null,
                                 remoteProject != null ? remoteProject.getAvatarUrl() : null,
                                 webhookService.isWebHookWatchLimitEnabled(webHook.getProjectId()));
  }

  public List<WebHookRestEntity> toRestEntities(WebhookService webhookService,  Collection<WebHook> webHooks) {
    return webHooks.stream().map(webHook -> toRestEntity(webhookService, webHook)).toList();
  }
}
