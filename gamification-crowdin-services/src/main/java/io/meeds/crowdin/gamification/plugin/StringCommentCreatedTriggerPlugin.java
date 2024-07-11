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

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.crowdin.gamification.model.Event;
import io.meeds.crowdin.gamification.services.CrowdinTriggerService;

import static io.meeds.crowdin.gamification.utils.Utils.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class StringCommentCreatedTriggerPlugin extends CrowdinTriggerPlugin {

  @Autowired
  private CrowdinTriggerService crowdinTriggerService;

  @PostConstruct
  public void init() {
    crowdinTriggerService.addPlugin(this);
  }

  @Override
  public List<Event> getEvents(String trigger, Map<String, Object> payload) {
    return Collections.singletonList(new Event(STRING_COMMENT_CREATED_EVENT_NAME,
                                               extractSubItem(payload, COMMENT, USER, USERNAME),
                                               extractSubItem(payload, COMMENT, USER, USERNAME),
                                               constructObjectIdAsJsonString(payload, COMMENT),
                                               COMMENT,
                                               getProjectId(payload),
                                               extractSubItem(payload, COMMENT, TARGET_LANGUAGE, ID),
                                               true,
                                               extractSubItem(payload, COMMENT, STRING, FILE, DIRECTORY_ID),
                                               trigger.equals(COMMENT_DELETED_TRIGGER)));
  }

  @Override
  public String getEventName() {
    return COMMENT_CREATED_TRIGGER;
  }

  @Override
  public String getCancellingEventName() {
    return COMMENT_DELETED_TRIGGER;
  }

  @Override
  public String getProjectId(Map<String, Object> payload) {
    return extractSubItem(payload, COMMENT, STRING, PROJECT, ID);
  }
}
