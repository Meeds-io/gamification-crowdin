/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Lab
 * contact@meedslab.com
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
package io.meeds.gamification.crowdin.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WebHookRestEntity {

  private long         id;

  private long         webhookId;

  private long         projectId;

  private List<String> triggers;

  private Boolean      enabled;

  private String       watchedDate;

  private String       watchedBy;

  private String       updatedDate;

  private String       refreshDate;

  private String       name;

  private String       title;

  private String       description;

  private String       avatarUrl;

  private boolean watchScopeLimited;

  public WebHookRestEntity(long id, // NOSONAR
                           long webhookId,
                           long projectId,
                           List<String> triggers,
                           boolean enabled,
                           String watchedDate,
                           String watchedBy,
                           String updatedDate,
                           String refreshDate,
                           String name,
                           String title,
                           String description,
                           String avatarUrl,
                           boolean watchScopeLimited) {

    this.id = id;
    this.webhookId = webhookId;
    this.projectId = projectId;
    this.triggers = triggers;
    this.enabled = enabled;
    this.name = name;
    this.watchedDate = watchedDate;
    this.watchedBy = watchedBy;
    this.refreshDate = refreshDate;
    this.updatedDate = updatedDate;
    this.title = title;
    this.description = description;
    this.avatarUrl = avatarUrl;
    this.watchScopeLimited = watchScopeLimited;
  }
}
