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
import CrowdinConnectorEvent from './components/CrowdinEvent.vue';
import CrowdinConnectorEventForm from './components/CrowdinEventForm.vue';
import CrowdinConnectorEventDisplay from './components/CrowdinEventDisplay.vue';
import CrowdinEventDisplayProjectItem from './components/CrowdinEventDisplayProjectItem.vue';
import CrowdinProjectItem from './components/CrowdinProjectItem.vue';
import CrowdinSelectDirectoryDrawer from './components/CrowdinSelectDirectoryDrawer.vue';

const components = {
  'crowdin-connector-event': CrowdinConnectorEvent,
  'crowdin-connector-event-form': CrowdinConnectorEventForm,
  'crowdin-connector-event-display': CrowdinConnectorEventDisplay,
  'crowdin-connector-event-display-project-item': CrowdinEventDisplayProjectItem,
  'crowdin-connector-project-item': CrowdinProjectItem,
  'crowdin-connector-select-directory-drawer': CrowdinSelectDirectoryDrawer
};

for (const key in components) {
  Vue.component(key, components[key]);
}