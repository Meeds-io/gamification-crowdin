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
package io.meeds.crowdin.gamification.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import io.meeds.crowdin.gamification.model.RemoteLanguage;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WebHookRestEntity {

  private long                 id;

  private long                 webhookId;

  private long                 projectId;

  private List<String>         triggers;

  private Boolean              enabled;

  private String               watchedDate;

  private String               watchedBy;

  private String               updatedDate;

  private String               refreshDate;

  private String               name;

  private String               title;

  private String               description;

  private String               avatarUrl;

  private List<RemoteLanguage> languages;

  private boolean              watchScopeLimited;

  private boolean              isTokenValid;
}
