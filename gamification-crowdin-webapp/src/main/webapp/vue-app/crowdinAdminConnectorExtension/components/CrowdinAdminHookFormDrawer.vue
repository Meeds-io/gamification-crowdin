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
  <exo-drawer
    ref="crowdinHookFormDrawer"
    v-model="drawer"
    right
    @opened="stepper = 1"
    @closed="clear">
    <template #title>
      {{ drawerTitle }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="ProjectForm"
        v-model="isValidForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveHook">
        <v-stepper
          v-model="stepper"
          class="ma-0 py-0 d-flex flex-column"
          vertical
          flat>
          <div class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              step="1"
              class="ma-0">
              <span class="font-weight-bold text-color text-subtitle-1">{{ $t('crowdinConnector.admin.label.addAccessToken') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="stepper === 1" class="px-6">
                <div class="pb-4 d-flex flex-column text-color">
                  <v-card-text class="ps-0 text-color">
                    {{ $t('crowdinConnector.admin.label.accessToken.instructions.stepOne') }}
                    <a href="https://crowdin.com/settings#api-key" target="_blank">{{ $t('crowdinConnector.admin.label.accessToken.instructions.stepOneLink') }}
                      <v-icon size="14" class="pb-1 pe-1">fas fa-external-link-alt</v-icon>
                    </a>
                  </v-card-text>
                  <v-card-text class="pt-0 pb-1 ps-0 text-color">
                    {{ $t('crowdinConnector.admin.label.accessToken.instructions.stepThree') }} :
                  </v-card-text>
                  <span class="pt-0 ps-0 text-color">
                    - {{ $t('crowdinConnector.admin.label.accessTokenScopeOne') }}
                  </span>
                  <span class="pt-0 ps-0 text-color">
                    - {{ $t('crowdinConnector.admin.label.accessTokenScopeTwo') }}
                  </span>
                  <span class="pt-0 ps-0 text-color">
                    - {{ $t('crowdinConnector.admin.label.accessTokenScopeThree') }}
                  </span>
                </div>
                <div class="d-flex flex-column py-0">
                  <v-card-text class="pt-0 pb-1 ps-0 text-color subtitle-1">
                    {{ $t('crowdinConnector.admin.label.accessToken') }}
                  </v-card-text>
                  <v-text-field
                    ref="accessTokenInput"
                    v-model="accessTokenInput"
                    :readonly="!isTokenEditing"
                    :placeholder="$t('crowdinConnector.admin.label.accessToken.placeholder')"
                    class="pa-0"
                    type="text"
                    outlined
                    required
                    dense
                    @keyup.enter="handleToken">
                    <template #append-outer>
                      <v-slide-x-reverse-transition mode="out-in">
                        <v-icon
                          :key="`icon-${isTokenEditing}`"
                          :color="isTokenEditing ? 'success' : 'info'"
                          :disabled="(!accessTokenInput && isTokenEditing) || isFetchingProjects"
                          class="text-header-title"
                          @click="editAccessToken"
                          v-text="isTokenEditing ? 'fas fa-check' : (isFetchingProjects) ? 'fas fa-spinner fa-spin' : 'fas fa fa-edit'"
                          :style="{ 'transform-origin': 'unset !important'}" />
                      </v-slide-x-reverse-transition>
                    </template>
                  </v-text-field>
                  <span v-if="isTokenEditing && accessTokenInput" class="text-caption text-color">{{ $t('crowdinConnector.webhook.message.confirmBeforeProceeding') }} ↵</span>
                </div>
              </div>
            </v-slide-y-transition>
          </div>
          <div class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              :complete="stepper > 2"
              step="2"
              class="ma-0">
              <span class="font-weight-bold text-color text-subtitle-1">{{ $t('crowdinConnector.admin.label.identifyProject') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="stepper > 1" class="px-6">
                <v-card-text class="d-flex py-0 ps-0">
                  <v-select
                    v-model="projectId"
                    :items="projects"
                    item-text="name"
                    item-value="id"
                    :placeholder="$t('crowdinConnector.admin.label.project.placeholder')"
                    class="ignore-vuetify-classes flex-grow-1"
                    required
                    @change="hookEdited = true">
                    <template v-if="data" #selection="data">
                      <!-- HTML that describe how select should render selected items -->
                      {{ data.item.name }} / {{ data.item.identifier }}
                    </template>
                    <template v-if="data" #item="data">
                      <!-- HTML that describe how select should render items when the select is open -->
                      {{ data.item.name }} / {{ data.item.identifier }}
                    </template>
                  </v-select>
                </v-card-text>
              </div>
            </v-slide-y-transition>
          </div>
        </v-stepper>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          v-if="stepper === 2"
          class="btn me-2"
          @click="previousStep">
          {{ $t('crowdinConnector.webhook.form.label.button.back') }}
        </v-btn>
        <v-btn
          v-else
          class="btn me-2"
          @click="close">
          {{ $t('crowdinConnector.webhook.form.label.button.cancel') }}
        </v-btn>
        <v-btn
          v-if="stepper === 1 && !isFetchingProjects"
          :disabled="disableNextStepButton"
          class="btn btn-primary"
          @click="nextStep">
          {{ $t('crowdinConnector.webhook.form.label.button.next') }}
        </v-btn>
        <v-btn
          v-if="stepper === 1 && isFetchingProjects"
          :disabled="disableNextStepButton"
          class="btn btn-primary"
          @click="nextStep">
          <i class="fas fa-spinner fa-spin"></i>
        </v-btn>
        <v-btn
          v-if="stepper === 2"
          :disabled="disabledSave"
          :loading="loading"
          class="btn btn-primary"
          @click="saveHook">
          {{ $t('crowdinConnector.webhook.form.label.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    hook: {},
    stepper: 0,
    isValidForm: false,
    drawer: false,
    projectId: null,
    accessToken: null,
    isTokenEditing: false,
    isFetchingProjects: false,
    accessTokenInput: null,
    accessTokenStored: false,
    hookEdited: false,
    loading: false,
    projects: null,
  }),
  created() {
    this.$root.$on('crowdin-hook-form-drawer', this.open);
  },
  computed: {
    hookId() {
      return this.hook?.id;
    },
    disabledSave() {
      return !this.hookEdited || !this.projectId;
    },
    disableNextStepButton() {
      return (this.hookId && !this.accessTokenStored) || (this.hookId && !this.accessToken && this.isTokenEditing) || this.isTokenEditing || this.isFetchingProjects;
    },
    drawerTitle() {
      return this.hookId ? this.$t('crowdinConnector.admin.label.editProject') : this.$t('crowdinConnector.admin.label.addProject');
    },
  },
  methods: {
    open(hook) {
      if (hook) {
        this.hook = hook;
        this.projectId = hook?.projectId || null;
        this.accessTokenInput = '*'.repeat(8);
        this.accessTokenStored = true;
        this.getProjects(hook?.id || null);
      } else {
        this.accessTokenInput = null;
        this.isTokenEditing = true;
      }
      if (this.$refs.crowdinHookFormDrawer) {
        this.$refs.crowdinHookFormDrawer.open();
      }
    },
    close() {
      if (this.$refs.crowdinHookFormDrawer) {
        this.$refs.crowdinHookFormDrawer.close();
      }
    },
    saveHook() {
      this.loading = true;
      if (!this.hookId) {
        // Get project object from projects using projectId
        const project = this.projects.find(p => p.id === this.projectId);
        return this.$crowdinConnectorService.saveCrowdinWebHook(project, this.accessToken).then(() => {
          this.$root.$emit('crowdin-hooks-updated');
          this.close();
        }).catch(error => {
          this.displayNotificationAlert(error.message, 'error');
        }).finally(() => this.loading = false);
      } else {
        return this.$crowdinConnectorService.updateWebHookAccessToken(this.hookId, this.accessToken).then(() => {
          this.$root.$emit('crowdin-hooks-updated');
          this.close();
        }).finally(() => this.loading = false);
      }

    },
    editAccessToken() {
      if (this.isTokenEditing) {
        this.accessToken = this.accessTokenInput;
        this.accessTokenInput = '*'.repeat(16);
        this.isTokenEditing = false;
        this.hookEdited = true;
        this.getProjects();
      } else {
        this.accessTokenInput =null;
        this.isTokenEditing = true;
        this.$nextTick(() => {
          const $input = this.$refs['accessTokenInput'];
          if ($input) {
            $input.focus();
          }
        });
      }
    },
    getProjects(hookId) {
      this.isFetchingProjects = true;
      return this.$crowdinConnectorService.getProjects(this.accessToken, hookId)
        .then(projects => {
          this.projects = projects;
        })
        .catch(error => {
          this.displayNotificationAlert(error.message, 'error');
          this.editAccessToken();
        })
        .finally(() => this.isFetchingProjects = false);
    },

    displayNotificationAlert(message, type){
      if (['crowdin.tokenExpiredOrInvalid', 'crowdin.tokenRateLimitReached'].indexOf(message ) !== -1) {
        document.dispatchEvent(new CustomEvent('notification-alert', {
          detail: {
            message: this.$t(`crowdinConnector.webhook.${message}`),
            type: type,
          }
        }));
      }

    },
    previousStep() {
      this.stepper--;
      this.$forceUpdate();
    },
    nextStep(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.stepper++;
    },
    clear() {
      this.stepper = 0;
      this.accessToken = null;
      this.projectId = null;
      this.hook = {};
    },
    handleToken() {
      if (this.accessTokenInput) {
        this.accessToken = this.accessTokenInput;
        this.accessTokenInput = '*'.repeat(16);
        this.isTokenEditing = false;
        this.hookEdited = true;
        this.getProjects();
      }
    }
  }
};
</script>