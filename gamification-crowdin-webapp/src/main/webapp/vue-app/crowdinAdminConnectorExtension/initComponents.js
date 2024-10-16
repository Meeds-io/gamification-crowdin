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
 */
import CrowdinAdminConnectorItem from './components/CrowdinAdminConnectorItem.vue';
import CrowdinAdminHookFormDrawer from './components/CrowdinAdminHookFormDrawer.vue';
import CrowdinAdminConnectionSettingDrawer from './components/CrowdinAdminConnectionSettingDrawer.vue';
import CrowdinAdminConnectorHookList from './components/CrowdinAdminConnectorHookList.vue';
import CrowdinAdminConnectorHook from './components/CrowdinAdminConnectorHook.vue';
import CrowdinAdminConnectorHookDetail from './components/CrowdinAdminConnectorHookDetail.vue';
import CrowdinAdminConnectorEventItem from './components/CrowdinAdminConnectorEventItem.vue';
import CrowdinAdminConnectorEventList from './components/CrowdinAdminConnectorEventList.vue';

const components = {
  'crowdin-admin-connector-item': CrowdinAdminConnectorItem,
  'crowdin-admin-hook-form-drawer': CrowdinAdminHookFormDrawer,
  'crowdin-admin-connection-setting-drawer': CrowdinAdminConnectionSettingDrawer,
  'crowdin-admin-connector-hook-list': CrowdinAdminConnectorHookList,
  'crowdin-admin-connector-hook': CrowdinAdminConnectorHook,
  'crowdin-admin-connector-hook-detail': CrowdinAdminConnectorHookDetail,
  'crowdin-admin-connector-event-item': CrowdinAdminConnectorEventItem,
  'crowdin-admin-connector-event-list': CrowdinAdminConnectorEventList,
};

for (const key in components) {
  Vue.component(key, components[key]);
}