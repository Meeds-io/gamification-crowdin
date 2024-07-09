/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.crowdin.gamification.services;

import io.meeds.crowdin.gamification.model.RemoteDirectory;
import io.meeds.crowdin.gamification.model.RemoteProject;
import io.meeds.crowdin.gamification.model.WebHook;
import io.meeds.crowdin.gamification.storage.CrowdinConsumerStorage;
import io.meeds.crowdin.gamification.storage.WebHookStorage;
import io.meeds.gamification.utils.Utils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.meeds.crowdin.gamification.utils.Utils.*;

import java.util.List;

@Service
public class WebhookService {

  public static final String     NOT_FOUND      = " wasn't found";

  @Autowired
  private CrowdinConsumerStorage crowdinConsumerStorage;

  @Autowired
  private WebHookStorage         webHookStorage;

  public List<RemoteProject> getProjectsFromWebhookId(long webHookId) throws IllegalAccessException, ObjectNotFoundException {
    WebHook webHook = webHookStorage.getWebHookById(webHookId);
    if (webHook == null) {
      throw new ObjectNotFoundException("webhook with id : " + webHookId + NOT_FOUND);
    }
    return crowdinConsumerStorage.getProjects(webHook.getToken());
  }

  public List<RemoteProject> getProjects(String accessToken) throws IllegalAccessException {
    return crowdinConsumerStorage.getProjects(accessToken);
  }

  public WebHook createWebhook(long projectId,
                            String projectName,
                            String accessToken,
                            String currentUser) throws ObjectAlreadyExistsException, IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to create Crowdin hook");
    }

    WebHook existsWebHook = webHookStorage.getWebhookByProjectId(projectId);
    if (existsWebHook != null) {
      throw new ObjectAlreadyExistsException(existsWebHook);
    }

    WebHook webHook = crowdinConsumerStorage.createWebhook(projectId, CROWDIN_EVENTS, accessToken);

    if (webHook != null) {
      webHook.setProjectName(projectName);
      webHook.setWatchedBy(currentUser);
      return webHookStorage.saveWebHook(webHook);
    }
    return null;
  }

  public void updateWebHookAccessToken(long webHookId, String accessToken, String currentUser) throws IllegalAccessException,
                                                                                               ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS);
    }
    if (webHookId <= 0) {
      throw new IllegalArgumentException("webHook id must be positive");
    }
    WebHook webHook = webHookStorage.getWebHookById(webHookId);
    if (webHook == null) {
      throw new ObjectNotFoundException("webhook with id : " + webHookId + NOT_FOUND);
    }
    webHookStorage.updateWebHookAccessToken(webHookId, encode(accessToken));
  }

  public List<WebHook> getWebhooks(String currentUser, int offset, int limit, boolean forceUpdate) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS);
    }
    return getWebhooks(offset, limit, forceUpdate);
  }

  public WebHook deleteWebhook(long projectId, String currentUser) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to delete Crowdin hook");
    }
    WebHook webHook = webHookStorage.getWebhookByProjectId(projectId);
    if (webHook == null) {
      throw new ObjectNotFoundException("Crowdin hook for project id : " + projectId + NOT_FOUND);
    }
    String response = crowdinConsumerStorage.deleteWebhook(webHook);
    if (response != null) {
      return deleteWebhook(projectId);
    }
    return null;
  }

  public WebHook deleteWebhook(long projectId) {
    return webHookStorage.deleteWebHook(projectId);
  }

  public List<WebHook> getWebhooks(int offset, int limit, boolean forceUpdate) {
    if (forceUpdate) {
      forceUpdateWebhooks();
    }
    return getWebhooks(offset, limit);
  }

  public List<RemoteDirectory> getProjectDirectories(long remoteProjectId,
                                                     String currentUser,
                                                     int offset,
                                                     int limit) throws IllegalAccessException, ObjectNotFoundException {

    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access project directories");
    }

    WebHook existsWebHook = webHookStorage.getWebhookByProjectId(remoteProjectId);
    if (existsWebHook == null) {
      throw new ObjectNotFoundException("Webhook with project id '" + remoteProjectId + "' doesn't exist");
    }

    return crowdinConsumerStorage.getProjectDirectories(remoteProjectId, offset, limit, existsWebHook.getToken());
  }

  public void forceUpdateWebhooks() {
    crowdinConsumerStorage.clearCache();
    List<WebHook> webHook = getWebhooks(0, -1);
    webHook.forEach(this::forceUpdateWebhook);
  }

  public List<WebHook> getWebhooks(int offset, int limit) {
    return webHookStorage.getWebhooks(offset, limit);
  }

  public WebHook getWebhookId(long webhookId, String username) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(username)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS);
    }
    WebHook webHook = getWebhookId(webhookId);
    if (webHook == null) {
      throw new ObjectNotFoundException("Webhook doesn't exist");
    }
    return webHook;
  }

  public WebHook getWebhookId(long webhookId) {
    if (webhookId <= 0) {
      throw new IllegalArgumentException("Webhook id is mandatory");
    }
    return webHookStorage.getWebHookById(webhookId);
  }

  private void forceUpdateWebhook(WebHook webHook) {
    // TODO
  }
}
