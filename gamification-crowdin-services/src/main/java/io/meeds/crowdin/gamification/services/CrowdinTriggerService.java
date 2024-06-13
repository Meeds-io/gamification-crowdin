/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
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

import io.meeds.common.ContainerTransactional;
import io.meeds.crowdin.gamification.model.Event;
import io.meeds.crowdin.gamification.model.WebHook;
import io.meeds.crowdin.gamification.plugin.CrowdinTriggerPlugin;
import io.meeds.crowdin.gamification.storage.WebHookStorage;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.TriggerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import static io.meeds.crowdin.gamification.utils.Utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrowdinTriggerService {

  private static final Log                        LOG            = ExoLogger.getLogger(CrowdinTriggerService.class);

  private final Map<String, CrowdinTriggerPlugin> triggerPlugins = new HashMap<>();

  @Autowired
  private TriggerService                          triggerService;

  @Autowired
  private ConnectorService                        connectorService;

  @Autowired
  private IdentityManager                         identityManager;

  @Autowired
  private ListenerService                         listenerService;

  @Autowired
  private WebHookStorage                          webHookStorage;

  @Autowired
  private ThreadPoolTaskExecutor                  threadPoolTaskExecutor;

  public void handleTriggerAsync(String bearerToken, String payload) {
    threadPoolTaskExecutor.execute(() -> handleTrigger(bearerToken, payload));
  }

  @ContainerTransactional
  @SuppressWarnings("unchecked")
  public void handleTrigger(String bearerToken, String payload) {

    Map<String, Object> payloadMap = fromJsonStringToMap(payload);

    Object eventsObj = payloadMap.get("events");
    List<Map<String, Object>> eventsListMap = (List<Map<String, Object>>) eventsObj;
    LOG.info("Total Events: " + eventsListMap.size());

    for (Map<String, Object> eventMap : eventsListMap) {
      String trigger = extractSubItem(eventMap, "event");
      CrowdinTriggerPlugin triggerPlugin = getCrowdinTriggerPlugin(trigger);
      if (triggerPlugin == null) {
        LOG.error("Trigger plugin for trigger : " + trigger + " wasn't found");
      } else {
        String projectId = triggerPlugin.getProjectId(eventMap);
        if (projectId == null) {
          LOG.error("Project id is not found in the payload");
          continue;
        }
        WebHook webHook = webHookStorage.getWebhookByProjectId(Long.parseLong(projectId));
        if (webHook == null) {
          LOG.error("Crowdin hook for project id : " + projectId + " wasn't found");
          continue;
        }
        if (!verifyWebhookSecret(bearerToken.substring(7), webHook.getSecret())) {
          LOG.error("Verifying Crowdin webhook secret failed");
          continue;
        }
        processEvents(triggerPlugin.getEvents(trigger, eventMap), projectId);
      }
    }
  }

  public void processEvents(List<Event> events, String projectId) {
    events.stream().filter(event -> isTriggerEnabled(event.getName(), projectId)).forEach(this::processEvent);
  }

  private boolean isTriggerEnabled(String trigger, String projectId) {
    return triggerService.isTriggerEnabledForAccount(trigger, Long.parseLong(projectId));
  }

  private void processEvent(Event event) {
    String receiverId = NumberUtils.isDigits(event.getReceiver()) ? event.getReceiver()
                                                                  : connectorService.getAssociatedUsername(CONNECTOR_NAME,
                                                                                                           event.getReceiver());
    String senderId;
    if (!NumberUtils.isDigits(event.getSender()) && event.getSender() != null
        && !StringUtils.equals(event.getReceiver(), event.getSender())) {
      senderId = connectorService.getAssociatedUsername(CONNECTOR_NAME, event.getSender());
    } else {
      senderId = receiverId;
    }
    LOG.info("processEvent: senderId: " + senderId);
    if (StringUtils.isNotBlank(senderId)) {
      if (NumberUtils.isDigits(senderId)) {
        broadcastCrowdinEvent(event, senderId, receiverId);
      } else {
        Identity socialIdentity = identityManager.getOrCreateUserIdentity(senderId);
        LOG.info("processEvent: socialIdentity: " + socialIdentity);
        if (socialIdentity != null) {
          broadcastCrowdinEvent(event, senderId, receiverId);
        }
      }
    }
  }

  private void broadcastCrowdinEvent(Event event, String senderId, String receiverId) {
    try {
      String eventDetails = "{" + PROJECT_ID + ": " + event.getProjectId() + ", " + LANGUAGE_ID + ": " + event.getLanguageId()
          + ", " + MUST_BE_HUMAN + ": " + event.isMustBeHuman() + ", " + DIRECTORY_ID + ": " + event.getDirectoryId() + ", "
          + TOTAL_TARGET_ITEM + ": " + event.getTotalWords() + "}";
      Map<String, String> gam = new HashMap<>();
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("objectId", event.getObjectId());
      gam.put("objectType", event.getObjectType());
      gam.put("eventDetails", eventDetails);
      gam.put("ruleTitle", event.getName());
      if (!event.isCancelling()) {
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, "");
        LOG.info("Crowdin action {} broadcast for user {}", event.getName(), senderId);
      } else {
        listenerService.broadcast(GAMIFICATION_CANCEL_EVENT, gam, "");
        LOG.info("Crowdin cancelling action {} broadcast for user {}", event.getName(), senderId);
      }

    } catch (Exception e) {
      LOG.error("Cannot broadcast crowdin event", e);
    }
  }

  private CrowdinTriggerPlugin getCrowdinTriggerPlugin(String trigger) {
    return triggerPlugins.get(trigger);
  }

  public void addPlugin(CrowdinTriggerPlugin crowdinTriggerPlugin) {
    triggerPlugins.put(crowdinTriggerPlugin.getEventName(), crowdinTriggerPlugin);
    triggerPlugins.put(crowdinTriggerPlugin.getCancellingEventName(), crowdinTriggerPlugin);
  }

}
