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
 *
 */
export function init() {
  extensionRegistry.registerExtension('engagementCenterConnectors', 'connector-extensions', {
    id: 'crowdin',
    name: 'crowdin',
    image: '/gamification-crowdin/images/crowdin.png',
    title: 'Crowdin',
    description: 'crowdinConnector.admin.label.description',
    rank: 40,
    init: () => {
      const lang = window.eXo?.env?.portal?.language || 'en';
      const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.CrowdinWebHookManagement-${lang}.json`;
      return exoi18n.loadLanguageAsync(lang, url);
    }
  });
}