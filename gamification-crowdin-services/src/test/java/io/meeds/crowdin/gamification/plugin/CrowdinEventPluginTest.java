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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package io.meeds.crowdin.gamification.plugin;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.meeds.crowdin.gamification.plugin.CrowdinEventPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.meeds.crowdin.gamification.plugin.CrowdinEventPlugin.EVENT_TYPE;
import static io.meeds.crowdin.gamification.utils.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = { CrowdinEventPlugin.class, })
public class CrowdinEventPluginTest {

  @Test
  public void testIsValidEvent() {
    CrowdinEventPlugin crowdinEventPlugin = new CrowdinEventPlugin();
    assertEquals(EVENT_TYPE, crowdinEventPlugin.getEventType());
    assertEquals(List.of(STRING_COMMENT_CREATED_EVENT_NAME,
                         SUGGESTION_ADDED_EVENT_NAME,
                         SUGGESTION_APPROVED_EVENT_NAME,
                         APPROVE_SUGGESTION_EVENT_NAME),
                 crowdinEventPlugin.getTriggers());

    Map<String, String> eventProperties = new HashMap<>();
    eventProperties.put(PROJECT_ID, "132452");
    eventProperties.put(DIRECTORY_IDS, "1115454,2225454");
    eventProperties.put(MUST_BE_HUMAN, "true");
    assertFalse(crowdinEventPlugin.isValidEvent(eventProperties,
                                                "{" + PROJECT_ID + ": " + 132452 + ", " + DIRECTORY_ID + ": " + "221545" + ", "
                                                    + MUST_BE_HUMAN + ": " + "true" + "}"));

    assertFalse(crowdinEventPlugin.isValidEvent(eventProperties,
                                                "{" + PROJECT_ID + ": " + 132453 + ", " + DIRECTORY_ID + ": " + "221545" + ", "
                                                    + MUST_BE_HUMAN + ": " + "true" + "}"));
    assertFalse(crowdinEventPlugin.isValidEvent(eventProperties,
                                                "{" + PROJECT_ID + ": " + 132452 + ", " + DIRECTORY_ID + ": " + "2225454" + ", "
                                                    + MUST_BE_HUMAN + ": " + "false" + "}"));
    assertTrue(crowdinEventPlugin.isValidEvent(eventProperties,
                                               "{" + PROJECT_ID + ": " + 132452 + ", " + DIRECTORY_ID + ": " + "1115454" + ", "
                                                   + MUST_BE_HUMAN + ": " + "true" + "}"));

  }
}
