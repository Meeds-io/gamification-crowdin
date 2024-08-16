/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.meeds.crowdin.gamification.utils.Utils.CONNECTOR_NAME;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = { CrowdinConnectorPlugin.class, })
public class CrowdinConnectorPluginTest {

  @Test
  public void testValidateToken() {
    // When
    CrowdinConnectorPlugin crowdinConnectorPlugin = new CrowdinConnectorPlugin();

    // Then
    assertEquals(CONNECTOR_NAME, crowdinConnectorPlugin.getConnectorName());
    assertEquals(CONNECTOR_NAME, crowdinConnectorPlugin.getName());
  }
}
