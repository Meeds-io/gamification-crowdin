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
  <tr>
    <td class="ps-0 no-border-bottom">
      <gamification-admin-connector-trigger
        :trigger="trigger"
        class="py-2" />
    </td>
    <td class="no-border-bottom d-flex justify-center py-2">
      <div class="d-flex flex-column align-center">
        <v-switch
          v-model="enabled"
          :ripple="false"
          color="primary"
          class="my-auto"
          @change="enableDisableTrigger" />
      </div>
    </td>
  </tr>
</template>

<script>
export default {
  props: {
    trigger: {
      type: Object,
      default: null
    },
    projectId: {
      type: String,
      default: null
    },
  },
  computed: {
    title() {
      return this.trigger?.title;
    },
    disabledAccounts() {
      return this.trigger?.disabledAccounts;
    },
    enabled() {
      return !this.disabledAccounts.includes(this.projectId);
    },
  },
  methods: {
    enableDisableTrigger() {
      this.$gamificationConnectorService.saveTriggerStatus(this.title, this.projectId, !this.enabled);
    },
  }
};
</script>