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
    organizationId: {
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
      return !this.disabledAccounts.includes(this.organizationId);
    },
  },
  methods: {
    enableDisableTrigger() {
      this.$gamificationConnectorService.saveTriggerStatus(this.title, this.organizationId, !this.enabled);
    },
  }
};
</script>