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
    <v-subheader class="px-0 py-3">
      <v-card-text class="text-color text-subtitle-1 pa-0 py-2">{{ $t('gamification.label.events.placeholder') }}</v-card-text>
      <v-spacer />
      <v-card
        width="220"
        max-width="100%"
        flat>
        <v-text-field
          v-model="keyword"
          :placeholder="$t('gamification.label.filter.filterEvents')"
          prepend-inner-icon="fa-filter icon-default-color"
          clear-icon="fa-times fa-1x"
          class="pa-0 me-3 my-auto"
          clearable
          hide-details />
      </v-card>
    </v-subheader>
    <v-data-table
      :headers="triggersHeaders"
      :items="triggersToDisplay"
      :options.sync="options"
      :server-items-length="pageSize"
      :show-rows-border="false"
      :loading="loading"
      mobile-breakpoint="0"
      hide-default-footer
      disable-sort>
      <template slot="item" slot-scope="props">
        <crowdin-admin-connector-event-item :trigger="props.item" :project-id="projectId" />
      </template>
    </v-data-table>
    <div v-if="hasMoreTriggers" class="d-flex justify-center py-4">
      <v-btn
        :loading="loading"
        min-width="95%"
        class="btn"
        text
        @click="loadMore">
        {{ $t('rules.loadMore') }}
      </v-btn>
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
      options: {
        page: 1,
        itemsPerPage: 10,
      },
      triggers: [],
      pageSize: 10,
      loading: true,
      keyword: ''
    };
  },
  computed: {
    projectId() {
      return this.hook?.projectId;
    },
    triggersHeaders() {
      return [
        {text: this.$t('crowdinConnector.webhook.details.event'), align: 'start', width: '80%' , class: 'dark-grey-color text-font-size ps-0'},
        {text: this.$t('crowdinConnector.webhook.details.status'), align: 'center', width: '20%', class: 'dark-grey-color text-font-size'},];
    },
    hasMoreTriggers() {
      return this.keyword ? this.sortedTriggers.length > this.pageSize : this.triggersSize > this.pageSize;
    },
    sortedTriggers() {
      let filteredTriggers = this.triggers;
      if (this.keyword) {
        filteredTriggers = this.triggers.filter(item =>
          this.getTriggerLabel(item).toLowerCase().includes(this.keyword.toLowerCase())
        );
      }
      return filteredTriggers.sort((a, b) => this.getTriggerLabel(a).localeCompare(this.getTriggerLabel(b)));
    },
    triggersToDisplay() {
      return this.sortedTriggers.slice(0, this.pageSize);
    },
    triggersSize() {
      return this.triggers?.length;
    },
  },
  created() {
    this.retrieveWebHookTriggers();
  },
  methods: {
    retrieveWebHookTriggers() {
      this.$gamificationConnectorService.getTriggers('crowdin', 'disabledAccounts')
        .then(data => {
          this.triggers = data;
        })
        .finally(() => this.loading = false);
    },
    loadMore() {
      this.pageSize += this.pageSize;
    },
    getTriggerLabel(trigger) {
      return this.$t(`gamification.event.title.${trigger?.title}`);
    }
  }
};
</script>