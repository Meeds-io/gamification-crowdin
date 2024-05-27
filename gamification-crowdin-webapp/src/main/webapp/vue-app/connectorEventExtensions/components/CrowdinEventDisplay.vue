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
  <v-app>
    <template>
      <v-progress-linear
        v-if="loading"
        indeterminate
        height="2"
        color="primary" />
    </template>
    <template v-if="items.length && !loading">
      <div class="subtitle-1 font-weight-bold mb-2">
        {{ $t('gamification.event.display.goThere') }}
      </div>
      <crowdin-connector-event-display-project-item
        initialized="initialized"
        v-for="item in items"
        :key="item"
        :item="item" />
    </template>
  </v-app>
</template>

<script>

export default {
  props: {
    properties: {
      type: Object,
      default: null
    },
    trigger: {
      type: String,
      default: null
    },
  },
  data() {
    return {
      project: null,
      directories: [],
      loading: false
    };
  },
  computed: {
    projectId() {
      return this.properties?.projectId;
    },
    directoryIds() {
      return this.properties?.directoryIds?.split(',').map(Number);
    },
    languages() {
      return this.properties?.languageIds?.split(',');
    },
    items() {
      const objects = [];
      const hasLanguages = Array.isArray(this.languages) && this.languages.length > 0;
      const hasDirectories = Array.isArray(this.directories) && this.directories.length > 0;
      if (hasLanguages && hasDirectories) {
        this.languages.forEach(lang => {
          this.directories.forEach(dir => {
            objects.push({
              directory: dir.path,
              project: this.project,
              language: lang,
            });
          });
        });
      } else if (hasLanguages) {
        this.languages.forEach(lang => {
          objects.push({
            project: this.project,
            language: lang,
          });
        });
      } else if (hasDirectories) {
        this.directories.forEach(dir => {
          objects.push({
            directory: dir.path,
            project: this.project,
          });
        });
      } else {
        objects.push({ project: this.project });
      }
      return objects.sort((a, b) => `${a?.directory}/${a?.language}`.localeCompare(`${b?.directory}/${b?.language}`));
    }
  },
  created() {
    this.getCrowdInProject();
  },
  methods: {
    getCrowdInProject() {
      this.loading = true;
      return this.$crowdinConnectorService.getCrowdinWebHooks(0, -1)
        .then(data => {
          this.project = data.webhooks.find(a => a.projectId === Number(this.projectId));
        }).then(() => {
          if (this.directoryIds) {
            return this.$crowdinConnectorService.getWebHookDirectories(this.projectId, this.directoryIds)
              .then(data => {
                this.directories = data;
              });
          }
        })
        .finally(() => this.loading = false);
    },
  }
};
</script>
