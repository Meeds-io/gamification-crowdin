<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Lab contact@meeds.io

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
  <v-list-item
    :href="projectUrl"
    class="clickable mb-2"
    target="_blank">
    <v-list-item-icon class="me-3 my-auto">
      <v-avatar
        size="24"
        rounded
        left>
        <v-img :width="24" :src="avatarUrl" />
      </v-avatar>
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title class="text-color body-2">
        <p
          class="ma-auto text-truncate"
          v-sanitized-html="title"></p>
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>

<script>
export default {
  props: {
    item: {
      type: Object,
      default: () => ({}),
    },
  },
  computed: {
    project() {
      return this.item?.project;
    },
    projectTitle() {
      return this.project?.title;
    },
    projectName() {
      return this.project?.name;
    },
    language() {
      return this.item?.language;
    },
    directory() {
      return this.item?.directory?.startsWith('/') ? this.item?.directory?.substring(1) : this.item?.directory;
    },
    avatarUrl() {
      if (this.project?.avatarUrl) {
        return `${this.project?.avatarUrl}?version=${new Date().getTime()}`;
      } else {
        return '/gamification-crowdin/images/crowdin.png';
      }
    },
    projectUrl() {
      return this.project && `https://crowdin.com/project/${this.projectTitle}/${this.language ? this.language : 'en'}#${this.directory ? `/${this.directory}` : ''}`;
    },
    title() {
      return `${this.projectName || this.projectTitle} ${this.directory ? `- ${this.directory}` : ''} ${this.language ? `/ ${this.language?.toUpperCase()}` : '' }`;
    },
  },
};
</script>
