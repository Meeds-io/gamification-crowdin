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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.plugin.EventPlugin;
import io.meeds.gamification.service.EventService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

import static io.meeds.gamification.crowdin.utils.Utils.*;

@Component
public class CrowdinEventPlugin extends EventPlugin {

  public static final String EVENT_TYPE = "crowdin";

  @Autowired
  private EventService       eventService;

  @PostConstruct
  public void init() {
    eventService.addPlugin(this);
  }

  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }

  @Override
  public List<String> getTriggers() {
    return List.of(STRING_COMMENT_CREATED_EVENT_NAME,
                   SUGGESTION_ADDED_EVENT_NAME,
                   SUGGESTION_APPROVED_EVENT_NAME,
                   APPROVE_SUGGESTION_EVENT_NAME);
  }

  @Override
  public boolean isValidEvent(Map<String, String> eventProperties, String triggerDetails) {

    String desiredProjectId = eventProperties.get(PROJECT_ID);

    String desiredMustBeHuman = eventProperties.get(MUST_BE_HUMAN);

    List<String> desiredDirectoryIds =
                                     eventProperties.get(DIRECTORY_IDS) != null ? Arrays.asList(eventProperties.get(DIRECTORY_IDS)
                                                                                                               .split(","))
                                                                                : Collections.emptyList();

    List<String> desiredLanguageIds = eventProperties.get(LANGUAGE_IDS) != null
                                                                                ? Arrays.asList(eventProperties.get(LANGUAGE_IDS)
                                                                                                               .split(","))
                                                                                : Collections.emptyList();

    Map<String, String> triggerDetailsMop = stringToMap(triggerDetails);

    return desiredProjectId.equals(triggerDetailsMop.get(PROJECT_ID))
        && (desiredMustBeHuman.equals("false") || desiredMustBeHuman.equals(triggerDetailsMop.get(MUST_BE_HUMAN)))
        && (CollectionUtils.isEmpty(desiredDirectoryIds) || desiredDirectoryIds.contains(triggerDetailsMop.get(DIRECTORY_ID)))
        && (CollectionUtils.isEmpty(desiredLanguageIds) || desiredLanguageIds.contains(triggerDetailsMop.get(LANGUAGE_ID)));
  }

  private static Map<String, String> stringToMap(String mapAsString) {
    Map<String, String> map = new HashMap<>();
    mapAsString = mapAsString.substring(1, mapAsString.length() - 1);
    String[] pairs = mapAsString.split(", ");
    for (String pair : pairs) {
      String[] keyValue = pair.split(": ");
      String key = keyValue[0].trim();
      String value = keyValue[1].trim();
      map.put(key, value);
    }
    return map;
  }
}
