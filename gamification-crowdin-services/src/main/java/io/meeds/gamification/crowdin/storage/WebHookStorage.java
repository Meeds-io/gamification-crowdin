/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Lab contact@meedslab.com
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
 */
package io.meeds.gamification.crowdin.storage;

import io.meeds.gamification.crowdin.dao.WebHookDAO;
import io.meeds.gamification.crowdin.entity.WebhookEntity;
import io.meeds.gamification.crowdin.model.WebHook;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static io.meeds.gamification.crowdin.storage.mapper.WebHookMapper.fromEntity;
import static io.meeds.gamification.crowdin.storage.mapper.WebHookMapper.toEntity;
@Component
public class WebHookStorage {

  @Autowired
  private final WebHookDAO webHookDAO;

  public WebHookStorage(WebHookDAO crowdinHookDAO) {
    this.webHookDAO = crowdinHookDAO;
  }

  public WebHook saveWebHook(WebHook webHook) throws ObjectAlreadyExistsException {
    WebHook existsWebHook = getWebhookByProjectId(webHook.getProjectId());
    if (existsWebHook == null) {
      WebhookEntity webhookEntity = toEntity(webHook);
      webhookEntity.setWatchedDate(new Date());
      webhookEntity.setUpdatedDate(new Date());
      webhookEntity.setRefreshDate(new Date());
      webhookEntity.setEnabled(true);
      webhookEntity = webHookDAO.create(webhookEntity);
      return fromEntity(webhookEntity);
    } else {
      throw new ObjectAlreadyExistsException(existsWebHook);
    }
  }

  public WebHook updateWebHook(WebHook webHook, boolean forceUpdate) {
    WebhookEntity webhookEntity = webHookDAO.find(webHook.getId());
    if (forceUpdate) {
      webhookEntity.setRefreshDate(new Date());
      webhookEntity.setTriggers(webHook.getTriggers());
    }
    webhookEntity.setUpdatedDate(new Date());
    return fromEntity(webHookDAO.update(webhookEntity));
  }

  public WebHook updateWebHookAccessToken(long webhookId, String accessToken) {
    WebhookEntity webhookEntity = webHookDAO.find(webhookId);
    webhookEntity.setToken(accessToken);
    return fromEntity(webHookDAO.update(webhookEntity));
  }

  public WebHook getWebHookById(Long id) {
    return fromEntity(webHookDAO.find(id));
  }

  public List<Long> getWebhookIds(int offset, int limit) {
    return webHookDAO.getWebhookIds(offset, limit);
  }

  public int countWebhooks() {
    return webHookDAO.count().intValue();
  }

  public WebHook getWebhookByProjectId(long projectId) {
    WebhookEntity connectorHookEntity = webHookDAO.getWebhookByProjectId(projectId);
    return fromEntity(connectorHookEntity);
  }

  public WebHook deleteWebHook(long projectId) {
    WebhookEntity webhookEntity = webHookDAO.getWebhookByProjectId(projectId);
    if (webhookEntity != null) {
      webHookDAO.delete(webhookEntity);
    }
    return fromEntity(webhookEntity);
  }
}
