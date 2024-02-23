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
package io.meeds.gamification.crowdin.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.commons.utils.StringListConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity(name = "CrowdinWebhooks")
@ExoEntity
@Table(name = "CROWDIN_WEBHOOKS")
@NamedQuery(name = "CrowdinWebhooks.getWebhookByProjectId",
            query = "SELECT crowdinWebhook FROM CrowdinWebhooks crowdinWebhook"
                    + " WHERE crowdinWebhook.projectId = :projectId")
@NamedQuery(name = "CrowdinWebhooks.getWebhookIds",
            query = "SELECT crowdinWebhook.id FROM CrowdinWebhooks crowdinWebhook"
                    + " ORDER BY crowdinWebhook.id ASC")
@Data
public class WebhookEntity implements Serializable {

  private static final long serialVersionUID = 2607146513663056421L;

  @Id
  @SequenceGenerator(name = "SEQ_CROWDIN_WEBHOOKS_ID", sequenceName = "SEQ_CROWDIN_WEBHOOKS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_CROWDIN_WEBHOOKS_ID")
  @Column(name = "ID")
  private Long              id;

  @Column(name = "WEBHOOK_ID")
  private Long              webhookId;

  @Column(name = "PROJECT_ID", nullable = false)
  private Long              projectId;

  @Column(name = "PROJECT_NAME", nullable = false)
  private String              projectName;

  @Convert(converter = StringListConverter.class)
  @Column(name = "TRIGGERS", nullable = false)
  private List<String>      triggers;

  @Column(name = "ENABLED", nullable = false)
  private Boolean           enabled;

  @Column(name = "WATCHED_DATE", nullable = false)
  private Date              watchedDate;

  @Column(name = "WATCHED_BY", nullable = false)
  private Long              watchedBy;

  @Column(name = "UPDATED_DATE", nullable = false)
  private Date              updatedDate;

  @Column(name = "REFRESH_DATE", nullable = false)
  private Date              refreshDate;

  @Column(name = "SECRET", nullable = false)
  private String            secret;

  @Column(name = "TOKEN", nullable = false)
  private String            token;
}
