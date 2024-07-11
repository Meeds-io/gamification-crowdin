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
package io.meeds.crowdin.gamification.storage;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;

import io.meeds.crowdin.gamification.dao.WebHookDAO;
import io.meeds.crowdin.gamification.entity.WebhookEntity;
import io.meeds.crowdin.gamification.model.WebHook;
import io.meeds.crowdin.gamification.storage.WebHookStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = { WebHookStorage.class, })
@ExtendWith(MockitoExtension.class)
public class WebHookStorageTest { // NOSONAR

  private static final Long ID = 2L;

  private static final Long PROJECT_ID = 1232L;

  @Autowired
  private WebHookStorage    webHookStorage;

  @MockBean
  private WebHookDAO        webHookDAO;

  @BeforeEach
  void setup() {
    when(webHookDAO.save(any())).thenAnswer(invocation -> {
      WebhookEntity entity = invocation.getArgument(0);
      if (entity.getId() == null) {
        entity.setId(ID);
      }
      when(webHookDAO.findById(ID)).thenReturn(Optional.of(entity));
      when(webHookDAO.findWebhookEntityByProjectId(PROJECT_ID)).thenReturn(entity);
      return entity;
    });
    doAnswer(invocation -> {
      WebhookEntity entity = invocation.getArgument(0);
      when(webHookDAO.findById(entity.getId())).thenReturn(Optional.empty());
      return null;
    }).when(webHookDAO).delete(any());
  }

  @Test
  public void testSaveWebHook() throws Exception { // NOSONAR
    // Given
    WebHook webHook = createWebHookInstance();

    // When
    WebHook createdWebhook = webHookStorage.saveWebHook(webHook);

    // Then
    assertNotNull(createdWebhook);
    assertEquals(webHook.getProjectId(), createdWebhook.getProjectId());
    assertEquals(webHook.getProjectName(), createdWebhook.getProjectName());
    assertEquals(webHook.getEnabled(), createdWebhook.getEnabled());
    assertEquals(webHook.getWebhookId(), createdWebhook.getWebhookId());

    assertThrows(ObjectAlreadyExistsException.class,
            () -> webHookStorage.saveWebHook(createdWebhook));
  }

  @Test
  public void testGetWebHookById() throws Exception { // NOSONAR
    // Given
    WebHook createdWebHook = webHookStorage.saveWebHook(createWebHookInstance());

    // When
    WebHook webHook = webHookStorage.getWebHookById(createdWebHook.getId());

    // Then
    assertNotNull(webHook);
    assertEquals(createdWebHook.getId(), webHook.getId());
    assertEquals(createdWebHook.getProjectName(), webHook.getProjectName());
    assertEquals(createdWebHook.getEnabled(), webHook.getEnabled());
    assertEquals(createdWebHook.getWebhookId(), webHook.getWebhookId());
  }

  @Test
  public void testDeleteWebHook() throws Exception { // NOSONAR
    // Given
    WebHook createdWebHook = webHookStorage.saveWebHook(createWebHookInstance());

    // When
    WebHook webHook = webHookStorage.deleteWebHook(createdWebHook.getProjectId());

    // Then
    assertNotNull(webHook);
  }

  @Test
  public void testGetWebHookByProjectId() throws Exception { // NOSONAR
    // Given
    WebHook createdWebHook = webHookStorage.saveWebHook(createWebHookInstance());

    // When
    WebHook webHook = webHookStorage.getWebhookByProjectId(createdWebHook.getProjectId());

    // Then
    assertNotNull(webHook);
    assertEquals(createdWebHook.getId(), webHook.getId());
    assertEquals(createdWebHook.getProjectName(), webHook.getProjectName());
    assertEquals(createdWebHook.getEnabled(), webHook.getEnabled());
    assertEquals(createdWebHook.getWebhookId(), webHook.getWebhookId());
  }

  protected WebHook createWebHookInstance() {
    WebHook webHook = new WebHook();
    webHook.setProjectId(PROJECT_ID);
    webHook.setProjectName("projectName");
    webHook.setWebhookId(1444L);
    webHook.setEnabled(true);
    webHook.setToken("token");
    webHook.setSecret("secret");
    return webHook;
  }
}
