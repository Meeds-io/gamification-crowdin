/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package io.meeds.crowdin.gamification.service;

import static io.meeds.crowdin.gamification.utils.Utils.*;
import static org.mockito.Mockito.*;

import io.meeds.crowdin.gamification.model.Event;
import io.meeds.crowdin.gamification.services.CrowdinTriggerService;
import io.meeds.crowdin.gamification.storage.WebHookStorage;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.TriggerService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.manager.IdentityManager;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = { CrowdinTriggerService.class, })
class CrowdinTriggerServiceTest {

  private static final String    USER = "user";

  @MockBean
  private TriggerService         triggerService;

  @MockBean
  private ConnectorService       connectorService;

  @MockBean
  private IdentityManager        identityManager;

  @MockBean
  private ListenerService        listenerService;

  @MockBean
  private WebHookStorage         webHookStorage;

  @MockBean
  private ThreadPoolTaskExecutor threadPoolTaskExecutor;

  @Autowired
  private CrowdinTriggerService  crowdinTriggerService;

  @Test
  void testProcessEvents() {
    Event event = new Event("stringComment.created", USER, USER, "1", "objectType", "123", "1", true, "1", false, 3);
    Event event1 = new Event("stringComment.deleted", USER, USER, "1", "objectType", "123", "1", true, "1", false, 4);

    when(triggerService.isTriggerEnabledForAccount("stringComment.created", 123L)).thenReturn(true);
    when(triggerService.isTriggerEnabledForAccount("stringComment.deleted", 123L)).thenReturn(false);
    when(connectorService.getAssociatedUsername(CONNECTOR_NAME, USER)).thenReturn("1");

    String eventDetails = "{" + PROJECT_ID + ": " + event.getProjectId() + ", " + LANGUAGE_ID + ": " + event.getLanguageId()
        + ", " + MUST_BE_HUMAN + ": " + event.isMustBeHuman() + ", " + DIRECTORY_ID + ": " + event.getDirectoryId() + ", "
        + TOTAL_TARGET_ITEM + ": " + event.getTotalWords() + "}";

    crowdinTriggerService.processEvents(List.of(event1, event), "123");
    Map<String, String> gam = new HashMap<>();
    gam.put("senderId", "1");
    gam.put("receiverId", "1");
    gam.put("objectId", event.getObjectId());
    gam.put("objectType", event.getObjectType());
    gam.put("eventDetails", eventDetails);
    gam.put("ruleTitle", event.getName());

    verify(listenerService, times(1)).broadcast(GAMIFICATION_GENERIC_EVENT, gam, "");

  }
}
