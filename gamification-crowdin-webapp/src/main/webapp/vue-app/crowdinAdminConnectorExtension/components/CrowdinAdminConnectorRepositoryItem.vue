<template>
  <tr>
    <td class="ps-0 no-border-bottom text-color text-truncate">
      {{ name }}
    </td>
    <td class="no-border-bottom text-color text-truncate">
      {{ description }}
    </td>
    <td class="no-border-bottom">
      <div class="d-flex flex-column align-center">
        <v-switch
          v-model="repository.enabled"
          :ripple="false"
          color="primary"
          class="connectorSwitcher my-auto"
          @change="enableDisableRepository" />
      </div>
    </td>
  </tr>
</template>

<script>
export default {
  props: {
    repository: {
      type: Object,
      default: null
    },
    organizationId: {
      type: String,
      default: null
    },
  },
  computed: {
    id() {
      return this.repository?.id;
    },
    name() {
      return this.repository?.name;
    },
    description() {
      return this.repository?.description;
    },
  },
  methods: {
    enableDisableRepository() {
      this.$crowdinConnectorService.saveRepositoryStatus(this.id, this.organizationId, this.repository.enabled);
    },
  }
};
</script>