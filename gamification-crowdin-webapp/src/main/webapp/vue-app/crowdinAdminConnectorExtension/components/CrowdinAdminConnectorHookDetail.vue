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
          slider-size="4">
<!--        <v-tab key="Events" href="#Events">-->
<!--          {{ $t('gamification.label.events') }}-->
<!--        </v-tab>-->
<!--        <v-tab key="Repositories" href="#Repositories">-->
<!--          {{ $t('crowdinConnector.admin.label.repositories') }}-->
<!--        </v-tab>-->
      </v-tabs>
      <v-tabs-items v-model="selectedTab" class="px-4 mt-2 ignore-vuetify-classes">
        <v-tab-item
            id="Events"
            value="Events"
            eager>
          <crowdin-admin-connector-event-list :hook="hook" />
        </v-tab-item>
<!--        <v-tab-item-->
<!--            id="Repositories"-->
<!--            value="Repositories"-->
<!--            eager>-->
<!--          <crowdin-admin-connector-repository-list :hook="hook" />-->
<!--        </v-tab-item>-->
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