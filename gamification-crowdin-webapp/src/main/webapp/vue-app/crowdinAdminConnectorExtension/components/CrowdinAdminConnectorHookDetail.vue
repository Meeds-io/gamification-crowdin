<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-card flat>
    <div class="py-2 px-4 py-sm-5 d-flex align-center">
      <v-tooltip :disabled="$root.isMobile" bottom>
        <template #activator="{ on }">
          <v-card
            class="d-flex align-center"
            flat
            v-on="on"
            @click="backToSetting">
            <v-btn
              class="width-auto ms-n3"
              icon>
              <v-icon size="18" class="icon-default-color mx-2">fa-arrow-left</v-icon>
            </v-btn>
            <div class="text-header-title">{{ title }}</div>
          </v-card>
        </template>
        <span>{{ $t('gamification.connectors.settings.BackToDetail') }}</span>
      </v-tooltip>
    </div>
    <div class="disabled-background">
      <v-tabs
        v-model="selectedTab"
        slider-size="4" />
      <v-tabs-items v-model="selectedTab" class="px-4 mt-2 ignore-vuetify-classes">
        <v-tab-item
          id="Events"
          value="Events"
          eager>
          <crowdin-admin-connector-event-list :hook="hook" />
        </v-tab-item>
      </v-tabs-items>
    </div>
  </v-card>
</template>
<script>

export default {
  props: {
    hook: {
      type: Object,
      default: null
    },
  },
  data() {
    return {
      selectedTab: 'Events',
    };
  },
  computed: {
    title() {
      return this.hook?.title;
    },
  },
  methods: {
    backToSetting() {
      this.$emit('close');
    },
  }
};
</script>