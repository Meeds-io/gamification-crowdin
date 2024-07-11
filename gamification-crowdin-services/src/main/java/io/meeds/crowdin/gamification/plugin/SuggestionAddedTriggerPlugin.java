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
package io.meeds.crowdin.gamification.plugin;

import io.meeds.crowdin.gamification.model.Event;
import io.meeds.crowdin.gamification.services.CrowdinTriggerService;
import io.meeds.gamification.model.RealizationDTO;
import io.meeds.gamification.service.RealizationService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.meeds.crowdin.gamification.utils.Utils.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SuggestionAddedTriggerPlugin extends CrowdinTriggerPlugin {

  @Autowired
  private CrowdinTriggerService crowdinTriggerService;

  @Autowired
  private RealizationService    realizationService;

  @PostConstruct
  public void init() {
    crowdinTriggerService.addPlugin(this);
  }

  @Override
  public List<Event> getEvents(String trigger, Map<String, Object> payload) {
    String objectId = constructObjectIdAsJsonString(payload, TRANSLATION);

    if (trigger.equals(SUGGESTION_ADDED_TRIGGER)) {
      return Collections.singletonList(new Event(SUGGESTION_ADDED_EVENT_NAME,
                                                 extractSubItem(payload, TRANSLATION, USER, USERNAME),
                                                 extractSubItem(payload, TRANSLATION, USER, USERNAME),
                                                 objectId,
                                                 TRANSLATION,
                                                 getProjectId(payload),
                                                 extractSubItem(payload, TRANSLATION, TARGET_LANGUAGE, ID),
                                                 extractSubItem(payload, TRANSLATION, PROVIDER) == null,
                                                 extractSubItem(payload, TRANSLATION, STRING, FILE, DIRECTORY_ID),
                                                 false));
    } else if (trigger.equals(SUGGESTION_DELETED_TRIGGER)) {
      List<RealizationDTO> realizations = realizationService.findRealizationsByObjectIdAndObjectType(objectId, TRANSLATION);
      if (CollectionUtils.isEmpty(realizations)) {
        return Collections.emptyList();
      }
      String earnerId = realizations.get(0).getEarnerId();
      return Collections.singletonList(new Event(SUGGESTION_ADDED_EVENT_NAME,
                                                 earnerId,
                                                 earnerId,
                                                 objectId,
                                                 TRANSLATION,
                                                 getProjectId(payload),
                                                 extractSubItem(payload, TRANSLATION, TARGET_LANGUAGE, ID),
                                                 extractSubItem(payload, TRANSLATION, PROVIDER) == null,
                                                 extractSubItem(payload, TRANSLATION, STRING, FILE, DIRECTORY_ID),
                                                 true));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public String getEventName() {
    return SUGGESTION_ADDED_TRIGGER;
  }

  @Override
  public String getCancellingEventName() {
    return SUGGESTION_DELETED_TRIGGER;
  }

  @Override
  public String getProjectId(Map<String, Object> payload) {
    return extractSubItem(payload, TRANSLATION, STRING, PROJECT, ID);
  }
}
