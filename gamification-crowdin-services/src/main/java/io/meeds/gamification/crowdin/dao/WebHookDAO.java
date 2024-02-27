/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Lab contact@meedslab.com
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.crowdin.dao;

import io.meeds.gamification.crowdin.entity.WebhookEntity;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class WebHookDAO extends GenericDAOJPAImpl<WebhookEntity, Long> {

  public static final String PROJECT_ID = "projectId";

  public WebhookEntity getWebhookByProjectId(long projectId) {
    TypedQuery<WebhookEntity> query = getEntityManager().createNamedQuery("CrowdinWebhooks.getWebhookByProjectId",
                                                                          WebhookEntity.class);
    query.setParameter(PROJECT_ID, projectId);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public List<Long> getWebhookIds(int offset, int limit) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("CrowdinWebhooks.getWebhookIds", Long.class);
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }
}
