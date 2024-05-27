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
package io.meeds.gamification.crowdin.storage;

import io.meeds.gamification.crowdin.dao.WebHookDAO;
import io.meeds.gamification.crowdin.entity.WebhookEntity;
import io.meeds.gamification.crowdin.model.WebHook;
import io.meeds.gamification.crowdin.storage.mapper.WebHookMapper;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static io.meeds.gamification.crowdin.storage.mapper.WebHookMapper.fromEntity;
import static io.meeds.gamification.crowdin.storage.mapper.WebHookMapper.toEntity;

@Repository
public class WebHookStorage {

  @Autowired
  private WebHookDAO webHookDAO;

  public WebHook saveWebHook(WebHook webHook) throws ObjectAlreadyExistsException {
    WebHook existsWebHook = getWebhookByProjectId(webHook.getProjectId());
    if (existsWebHook == null) {
      WebhookEntity webhookEntity = toEntity(webHook);
      webhookEntity.setWatchedDate(new Date());
      webhookEntity.setUpdatedDate(new Date());
      webhookEntity.setRefreshDate(new Date());
      webhookEntity.setEnabled(true);
      webhookEntity = webHookDAO.save(webhookEntity);
      return fromEntity(webhookEntity);
    } else {
      throw new ObjectAlreadyExistsException(existsWebHook);
    }
  }

  public void updateWebHookAccessToken(long webhookId, String accessToken) {
    WebhookEntity webhookEntity = webHookDAO.findById(webhookId).orElse(null);
    if (webhookEntity != null) {
      webhookEntity.setToken(accessToken);
      webHookDAO.save(webhookEntity);
    }
  }

  public WebHook getWebHookById(Long id) {
    return fromEntity(webHookDAO.findById(id).orElse(null));
  }

  public List<WebHook> getWebhooks(int offset, int limit) {
    if (limit > 0) {
      PageRequest pageable = PageRequest.of(Math.toIntExact(offset / limit), limit, Sort.by(Sort.Direction.ASC, "id"));
      return webHookDAO.findAll(pageable).getContent().stream().map(WebHookMapper::fromEntity).toList();
    } else {
      return webHookDAO.findAll().stream().map(WebHookMapper::fromEntity).toList();
    }
  }

  public WebHook getWebhookByProjectId(long projectId) {
    WebhookEntity connectorHookEntity = webHookDAO.findWebhookEntityByProjectId(projectId);
    return fromEntity(connectorHookEntity);
  }

  public WebHook deleteWebHook(long projectId) {
    WebhookEntity webhookEntity = webHookDAO.findWebhookEntityByProjectId(projectId);
    if (webhookEntity != null) {
      webHookDAO.delete(webhookEntity);
    }
    return fromEntity(webhookEntity);
  }
}
