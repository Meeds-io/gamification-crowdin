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
    <v-card-text class="px-0 dark-grey-color font-weight-bold">
      {{ $t('gamification.event.form.project') }}
    </v-card-text>
    <v-progress-circular
      v-if="loadingProjects"
      indeterminate
      color="primary"
      size="20"
      class="ms-3 my-auto" />
    <v-chip-group
      v-model="value"
      :show-arrows="false"
      active-class="primary white--text">
      <crowdin-connector-project-item
        v-for="project in projects"
        :key="project.id"
        :project="project"
        @handle="selectProject(project)" />
    </v-chip-group>
    <template v-if="selected">
      <div class="d-flex flex-row">
        <v-card-text class="px-0 dark-grey-color font-weight-bold">
          {{ $t('gamification.event.form.directory') }}
        </v-card-text>
        <div class="d-flex flex-row">
          <div class="ma-auto"> {{ $t('gamification.event.form.any') }}</div>
          <v-checkbox
            v-model="anyDir"
            class="mt-0 pt-0 align-center"
            color="primary"
            dense
            hide-details
            @click="changeDirectorySelection" />
        </div>
      </div>
      <v-autocomplete
        v-if="!anyDir"
        id="directoryAutoComplete"
        ref="directoryAutoComplete"
        v-model="selectedDirectories"
        :items="directories"
        :disabled="anyDir"
        :placeholder="$t('gamification.event.form.directory.placeholder')"
        class="pa-0"
        background-color="white"
        item-value="id"
        item-text="path"
        dense
        flat
        outlined
        multiple
        chips
        deletable-chips
        @change="readySelection" />
    </template>
    <template v-if="selected">
      <div class="d-flex flex-row">
        <v-card-text class="px-0 dark-grey-color font-weight-bold">
          {{ $t('gamification.event.form.language') }}
        </v-card-text>
        <div class="d-flex flex-row">
          <div class="ma-auto"> {{ $t('gamification.event.form.any') }}</div>
          <v-checkbox
            v-model="anyLanguage"
            class="mt-0 pt-0 align-center"
            color="primary"
            dense
            hide-details
            @click="changeLanguageSelection" />
        </div>
      </div>
      <v-autocomplete
        v-if="!anyLanguage"
        id="directoryAutoComplete"
        ref="directoryAutoComplete"
        v-model="selectedLanguages"
        :items="languages"
        :disabled="anyLanguage"
        :placeholder="$t('gamification.event.form.language.placeholder')"
        class="pa-0"
        background-color="white"
        item-value="id"
        item-text="name"
        dense
        flat
        outlined
        multiple
        chips
        deletable-chips
        @change="readySelection" />
    </template>
    <div class="d-flex flex-row" v-if="selected && needVerificationIsHuman">
      <v-card-text class="px-0 dark-grey-color font-weight-bold">
        {{ $t('gamification.event.form.human') }}
      </v-card-text>
      <div class="d-flex flex-row">
        <v-switch
          v-model="allowOnlyHuman"
          color="primary"
          class="ma-auto"
          hide-details
          @change="readySelection" />
      </div>
    </div>
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
      offset: 0,
      limit: 25,
      projects: [],
      selected: null,
      directories: [],
      selectedDirectories: [],
      languages: [],
      selectedLanguages: [],
      value: null,
      loadingProjects: true,
      anyDir: false,
      anyLanguage: false,
      hasMore: false,
      allowOnlyHuman: true,
    };
  },
  computed: {
    needVerificationIsHuman() {
      return this.trigger !== 'stringCommentCreated';
    }
  },
  watch: {
    value() {
      this.selected = this.projects[this.value];
      if (this.selected) {
        this.retrieveDirectories();
      }
    },
  },
  created() {
    this.retrieveProjects();
  },
  methods: {
    retrieveProjects() {
      this.loadingProjects = true;
      return this.$crowdinConnectorService.getCrowdinWebHooks(null, null, true)
        .then(data => {
          this.projects = data.webhooks;
        }).finally(() => {
          if (this.properties) {
            this.selected = this.projects.find(r => Number(r.projectId) === Number(this.properties.projectId));
            this.value = this.projects.indexOf(this.selected);
            this.anyDir = !this.properties?.directoryIds;
            this.anyLanguage = !this.properties?.languageIds;
            this.allowOnlyHuman = this.properties?.mustBeHuman === 'true' || true;
          } else if (this.projects.length > 0) {
            this.selected = this.projects[0];
            this.value = this.projects.indexOf(this.selected);
            this.anyDir = true;
            this.anyLanguage = true;
            this.readySelection();
          }

          this.languages = this.selected?.languages;

          if (this.properties?.languageIds) {
            const selectedLanguagesArray = this.properties?.languageIds?.split(',');
            this.languages.forEach(rep => {
              if (selectedLanguagesArray.includes(rep.id)) {
                if (!this.selectedLanguages.some(c => (c?.id === rep.id) || (c === rep.id))) {
                  this.selectedLanguages.push(rep.id);
                }
              }
            });
          }

          this.loadingProjects = false;
        });
    },
    retrieveDirectories() {
      const offset = this.offset || 0;
      const limit = this.limit || 25;
      return this.$crowdinConnectorService.getWebHookDirectories(this.selected?.projectId, null, offset, limit)
        .then(data => {
          this.directories.push(...data);

          if (this.properties?.directoryIds) {
            const selectedDirsArray = this.properties?.directoryIds?.split(',');
            this.directories.forEach(rep => {
              if (selectedDirsArray.map(str => Number(str)).includes(Number(rep.id))) {
                if (!this.selectedDirectories.some(c => (c?.id === rep.id) || (c === rep.id))) {
                  this.selectedDirectories.push(rep.id);
                }
              }
            });
          }
        });
    },
    readySelection() {
      const eventProperties = {
        projectId: this.selected?.projectId.toString(),
        mustBeHuman: this.allowOnlyHuman
      };
      if (this.selectedDirectories.length) {
        eventProperties.directoryIds = this.selectedDirectories.toString();
      }
      if (this.selectedLanguages.length) {
        eventProperties.languageIds = this.selectedLanguages.toString();
      }
      if ((this.anyDir || this.selectedDirectories.length) && (this.anyLanguage || this.selectedLanguages.length)) {
        document.dispatchEvent(new CustomEvent('event-form-filled', {detail: eventProperties}));
      } else {
        document.dispatchEvent(new CustomEvent('event-form-unfilled'));
      }
    },
    changeDirectorySelection() {
      this.selectedDirectories = [];
      if (this.anyDir) {
        this.readySelection();
      } else {
        this.retrieveDirectories();
        document.dispatchEvent(new CustomEvent('event-form-unfilled'));
      }
    },
    changeLanguageSelection() {
      this.selectedLanguages = [];
      if (this.anyLanguage) {
        this.readySelection();
      } else {
        document.dispatchEvent(new CustomEvent('event-form-unfilled'));
      }
    },
    selectProject(project) {
      this.allowOnlyHuman = true;
      this.offset = 0;
      this.directories = [];
      this.selectedDirectories = [];
      this.selectedLanguages = [];
      this.anyDir = true;
      this.anyLanguage = true;
      this.selected = project;
      this.readySelection();
    }
  }
};
</script>