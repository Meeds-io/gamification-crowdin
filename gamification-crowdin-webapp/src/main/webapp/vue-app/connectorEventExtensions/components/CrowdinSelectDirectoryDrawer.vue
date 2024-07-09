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
  <exo-drawer
    ref="directorySelectionDrawer"
    right>
    <template slot="title">
      {{ $t('gamification.crowdin.event.form.selectFolders') }}
    </template>
    <template slot="content">
      <v-treeview
        v-model="selected"
        :items="treeDirectories"
        item-key="path"
        selection-type="all"
        selectable
        return-object />
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('gamification.crowdin.event.form.back') }}
        </v-btn>
        <v-btn
          :disabled="disableApply"
          class="btn btn-primary"
          @click="apply">
          {{ $t('gamification.crowdin.event.form.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    directories: {
      type: Array,
      default: () => [],
    },
    selectedDirectories: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      selected: [],
    };
  },
  computed: {
    treeDirectories() {
      return this.$crowdinUtils.buildTree(this.directories);
    },
    disableApply() {
      return this.selectedDirectories.toString() === this.selected.toString();
    }
  },
  created() {
    this.$root.$on('directory-selection-drawer-open', this.open);
  },
  methods: {
    open() {
      this.selected = this.directories.filter(dir => this.selectedDirectories.includes(dir.id));
      if (this.$refs.directorySelectionDrawer) {
        this.$refs.directorySelectionDrawer.open();
      }
    },
    close() {
      if (this.$refs.directorySelectionDrawer) {
        this.$refs.directorySelectionDrawer.close();
      }
    },
    apply() {
      this.$emit('apply', this.selected?.map(repo => repo.id));
      this.close();
    }
  }
};
</script>