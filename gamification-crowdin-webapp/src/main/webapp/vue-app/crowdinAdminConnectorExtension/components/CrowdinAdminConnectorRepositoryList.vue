<template>
  <v-card flat class="pb-8">
    <v-subheader class="px-0 py-3">
      <v-card-text class="text-color text-subtitle-1 pa-0 py-2">{{ $t('crowdinConnector.admin.label.repositories.placeholder') }}</v-card-text>
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
      :headers="repositoriesHeaders"
      :items="repositories"
      :options.sync="options"
      :server-items-length="pageSize"
      :show-rows-border="false"
      :loading="loading"
      mobile-breakpoint="0"
      hide-default-footer
      disable-sort>
      <template slot="item" slot-scope="props">
        <crowdin-admin-connector-repository-item :repository="props.item" :organization-id="organizationId" />
      </template>
    </v-data-table>
    <div v-if="hasMore" class="d-flex justify-center py-4">
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
      repositories: [],
      options: {
        page: 1,
        itemsPerPage: 10,
      },
      totalSize: 0,
      pageSize: 10,
      loading: false,
      hasMore: false,
      keyword: '',
      startSearchAfterInMilliseconds: 300,
      endTypingKeywordTimeout: 50,
      startTypingKeywordTimeout: 0,
      typing: false,
    };
  },
  computed: {
    title() {
      return this.hook?.title;
    },
    organizationId() {
      return this.hook?.organizationId;
    },
    repositoriesHeaders() {
      return [
        {text: this.$t('crowdinConnector.webhook.details.repository'), value: 'title', align: 'start', width: '20%' , class: 'dark-grey-color text-font-size ps-0',},
        {text: this.$t('crowdinConnector.webhook.details.description'), value: 'description', align: 'start', width: '60%', class: 'dark-grey-color text-font-size'},
        {text: this.$t('crowdinConnector.webhook.details.status'), value: 'enabled', align: 'center', width: '20%', class: 'dark-grey-color text-font-size'},];
    },
  },
  watch: {
    keyword() {
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
      }
    },
  },
  created() {
    this.retrieveHookRepositories();
  },
  methods: {
    retrieveHookRepositories() {
      this.loading = true;
      const page = this.options?.page || 0;
      const itemsPerPage = this.options?.itemsPerPage || 10;
      return this.$crowdinConnectorService.getWebHookRepos(this.organizationId, page, itemsPerPage, this.keyword)
        .then(data => {
          this.repositories.push(...data.remoteRepositories);
          if (data.remoteRepositories.length <= itemsPerPage) {
            return this.$crowdinConnectorService.getWebHookRepos(this.organizationId, page + 1, itemsPerPage, this.keyword)
              .then(nextData => {
                this.hasMore = nextData.remoteRepositories.length > 0;
              });
          }
          return this.$nextTick();
        }).finally(() => this.loading = false);
    },
    loadMore() {
      this.options.page += 1;
      this.retrieveHookRepositories();
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.repositories = [];
          this.options.page = 1;
          this.options.itemsPerPage = 10;
          this.retrieveHookRepositories();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  }
};
</script>