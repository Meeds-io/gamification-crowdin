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
package io.meeds.crowdin.gamification.service;

import static io.meeds.crowdin.gamification.utils.Utils.AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS;
import static io.meeds.crowdin.gamification.utils.Utils.CROWDIN_EVENTS;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.crowdin.gamification.model.RemoteProject;
import io.meeds.crowdin.gamification.model.WebHook;
import io.meeds.crowdin.gamification.services.WebhookService;
import io.meeds.crowdin.gamification.storage.CrowdinConsumerStorage;
import io.meeds.crowdin.gamification.storage.WebHookStorage;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

@SpringBootTest(classes = { WebhookService.class, })
class WebhookServiceTest {

  private static final String    ADMIN_USER = "root";

  private static final String    USER       = "user";

  @MockBean
  private CrowdinConsumerStorage crowdinConsumerStorage;

  @MockBean
  private WebHookStorage         webHookStorage;

  @Autowired
  private WebhookService         webhookService;

  @Test
  void testCreateWebhook() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.createWebhook(1L, "organizationName", "accessToken", USER));
    assertEquals("The user is not authorized to create Crowdin hook", exception.getMessage());

    WebHook existsWebHook = new WebHook();
    when(webHookStorage.getWebhookByProjectId(2L)).thenReturn(existsWebHook);
    assertThrows(ObjectAlreadyExistsException.class,
                 () -> webhookService.createWebhook(2L, "organizationName", "accessToken", ADMIN_USER));

    // When
    WebHook webhook = new WebHook();
    webhook.setId(1L);
    webhook.setWebhookId(1245L);
    webhook.setProjectId(11245L);
    webhook.setProjectName("projectName");
    webhook.setTriggers(Arrays.asList(CROWDIN_EVENTS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");
    // When
    WebHook createdWebHook = webhookService.createWebhook(11245L, "organizationName", "accessToken", ADMIN_USER);
    // Then
    assertNull(createdWebHook);

    // When
    when(crowdinConsumerStorage.createWebhook(11245L, CROWDIN_EVENTS, "accessToken")).thenReturn(webhook);
    webhook.setWatchedBy(ADMIN_USER);
    when(webHookStorage.saveWebHook(webhook)).thenReturn(webhook);
    createdWebHook = webhookService.createWebhook(11245L, "organizationName", "accessToken", ADMIN_USER);

    // Then
    assertNotNull(createdWebHook);
  }

  @Test
  void testGetWebhookId() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class,
            () -> webhookService.getWebhookId(1245L, USER));
    assertEquals(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS, exception.getMessage());

    exception =
            assertThrows(IllegalArgumentException.class, () -> webhookService.getWebhookId(-1245L, ADMIN_USER));
    assertEquals("Webhook id is mandatory", exception.getMessage());

    exception =
            assertThrows(ObjectNotFoundException.class, () -> webhookService.getWebhookId(1245L, ADMIN_USER));
    assertEquals("Webhook doesn't exist", exception.getMessage());

    WebHook webhook = new WebHook();
    webhook.setWebhookId(1245L);
    webhook.setProjectId(11245L);
    webhook.setProjectName("projectName");
    webhook.setTriggers(Arrays.asList(CROWDIN_EVENTS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");

    // When
    when(webHookStorage.getWebHookById(1245L)).thenReturn(webhook);
    webhookService.getWebhookId(1245L, ADMIN_USER);

    // Then
    verify(webHookStorage, times(2)).getWebHookById(1245L);
  }

  @Test
  void testGetWebhooks() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class, () -> webhookService.getWebhooks(USER, 0, -1, false));
    assertEquals(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS, exception.getMessage());

    WebHook webhook = new WebHook();
    webhook.setWebhookId(1245L);
    webhook.setProjectId(11245L);
    webhook.setProjectName("projectName");
    webhook.setTriggers(Arrays.asList(CROWDIN_EVENTS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");
    WebHook webhook1 = new WebHook();
    webhook1.setWebhookId(222545L);
    webhook1.setProjectId(888655L);
    webhook1.setProjectName("projectName1");
    webhook1.setTriggers(Arrays.asList(CROWDIN_EVENTS));
    webhook1.setEnabled(true);
    webhook1.setToken("accessToken");
    webhook1.setSecret("secret");
    when(crowdinConsumerStorage.createWebhook(11245L, CROWDIN_EVENTS, "accessToken")).thenReturn(webhook);
    webhook.setWatchedBy(ADMIN_USER);
    when(webHookStorage.saveWebHook(webhook)).thenReturn(webhook);
    when(crowdinConsumerStorage.createWebhook(888655L, CROWDIN_EVENTS, "accessToken")).thenReturn(webhook1);

    // When
    WebHook webHook = webhookService.createWebhook(11245L, "projectName", "accessToken", ADMIN_USER);
    webhookService.createWebhook(888655L, "projectName1", "accessToken", ADMIN_USER);

    // Then
    assertNotNull(webhookService.getWebhooks(ADMIN_USER, 0, 10, false));

    // When
    exception = assertThrows(IllegalAccessException.class, () -> webhookService.deleteWebhook(webHook.getProjectId(), USER));
    assertEquals("The user is not authorized to delete Crowdin hook", exception.getMessage());
    assertThrows(ObjectNotFoundException.class, () -> webhookService.deleteWebhook(1000, ADMIN_USER));

    when(crowdinConsumerStorage.deleteWebhook(webHook)).thenReturn("response");
    when(webHookStorage.deleteWebHook(11245L)).thenReturn(webHook);
    when(webHookStorage.getWebhookByProjectId(11245L)).thenReturn(webHook);
    WebHook deletedWebhook = webhookService.deleteWebhook(webHook.getProjectId(), ADMIN_USER);

    // Then
    assertNotNull(deletedWebhook);
  }

  @Test
  void testGetProjects() throws Exception {
    Throwable exception = assertThrows(ObjectNotFoundException.class, () -> webhookService.getProjectsFromWebhookId(1L));
    assertEquals("webhook with id : 1 wasn't found", exception.getMessage());

    WebHook webhook = new WebHook();
    webhook.setWebhookId(1245L);
    webhook.setProjectId(11245L);
    webhook.setProjectName("projectName");
    webhook.setTriggers(Arrays.asList(CROWDIN_EVENTS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");

    RemoteProject remoteProject = new RemoteProject(11245L, "remoteProject", "remoteProject", "remoteProject", "avatarUrl", null);
    RemoteProject remoteProject1 = new RemoteProject(888655L,
                                                     "remoteProject1",
                                                     "remoteProject1",
                                                     "remoteProject1",
                                                     "avatarUrl1",
                                                     null);
    when(webHookStorage.getWebHookById(1245L)).thenReturn(webhook);
    when(crowdinConsumerStorage.getProjects("accessToken")).thenReturn(List.of(remoteProject, remoteProject1));
    assertNotNull(webhookService.getProjectsFromWebhookId(1245L));
    assertNotNull(webhookService.getProjects("accessToken"));

  }

  @Test
  void testUpdateWebHookAccessToken() {
    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.updateWebHookAccessToken(1L, "accessToken", USER));
    assertEquals(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS, exception.getMessage());

    exception = assertThrows(IllegalArgumentException.class,
                             () -> webhookService.updateWebHookAccessToken(-1L, "accessToken", ADMIN_USER));
    assertEquals("webHook id must be positive", exception.getMessage());

    exception = assertThrows(ObjectNotFoundException.class,
                             () -> webhookService.updateWebHookAccessToken(2L, "accessToken", ADMIN_USER));
    assertEquals("webhook with id : 2 wasn't found", exception.getMessage());

    WebHook webhook = new WebHook();
    webhook.setWebhookId(1245L);
    webhook.setProjectId(11245L);
    webhook.setProjectName("projectName");
    webhook.setTriggers(Arrays.asList(CROWDIN_EVENTS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");
  }

  @Test
  void testGetProjectDirectories() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.getProjectDirectories(11245L, USER, 0, 10));
    assertEquals("The user is not authorized to access project directories", exception.getMessage());

    exception =
              assertThrows(ObjectNotFoundException.class, () -> webhookService.getProjectDirectories(11245L, ADMIN_USER, 0, 10));
    assertEquals("Webhook with project id '11245' doesn't exist", exception.getMessage());

    WebHook webhook = new WebHook();
    webhook.setWebhookId(1245L);
    webhook.setProjectId(11245L);
    webhook.setProjectName("projectName");
    webhook.setTriggers(Arrays.asList(CROWDIN_EVENTS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");

    // When
    when(webHookStorage.getWebhookByProjectId(11245L)).thenReturn(webhook);
    webhookService.getProjectDirectories(11245L, ADMIN_USER, 0, 10);

    // Then
    verify(crowdinConsumerStorage, times(1)).getProjectDirectories(11245L, 0, 10, "accessToken");
  }
}
