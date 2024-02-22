package io.meeds.gamification.crowdin.services.impl;

import io.meeds.gamification.crowdin.model.RemoteProject;
import io.meeds.gamification.crowdin.model.WebHook;
import io.meeds.gamification.crowdin.services.CrowdinConsumerService;
import io.meeds.gamification.crowdin.services.WebhookService;
import io.meeds.gamification.crowdin.storage.WebHookStorage;
import io.meeds.gamification.model.RuleDTO;
import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.gamification.utils.Utils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import static io.meeds.gamification.crowdin.utils.Utils.*;

import java.util.List;

public class WebhookServiceImpl implements WebhookService {

    private static final Context CROWDIN_WEBHOOK_CONTEXT = Context.GLOBAL.id("crowdinWebhook");

    private static final Scope WATCH_LIMITED_SCOPE    = Scope.APPLICATION.id("watchLimited");

    private final CrowdinConsumerService crowdinConsumerService;

    private final SettingService settingService;

    private final RuleService ruleService;

    private static final String[]       CROWDIN_TRIGGERS        = new String[] { "file.added", "file.updated",
            "file.reverted", "file.deleted", "file.translated", "file.approved", "project.translated",
            "project.approved", "project.built", "translation.updated", "string.added", "string.updated",
            "string.deleted", "stringComment.created", "stringComment.updated", "stringComment.deleted",
            "stringComment.restored", "suggestion.added", "suggestion.updated", "suggestion.deleted",
            "suggestion.approved", "suggestion.disapproved", "task.added", "task.statusChanged", "task.deleted" };

    private final WebHookStorage webHookStorage;

    public WebhookServiceImpl(CrowdinConsumerService crowdinConsumerService, SettingService settingService, RuleService ruleService, WebHookStorage webHookStorage) {
        this.crowdinConsumerService = crowdinConsumerService;
        this.settingService = settingService;
        this.ruleService = ruleService;
        this.webHookStorage = webHookStorage;
    }

    @Override
    public List<RemoteProject> getProjects(String accessToken) throws IllegalAccessException {
        return crowdinConsumerService.getProjects(accessToken);
    }

    @Override
    public void createWebhook(long projectId, String projectName, String accessToken, String currentUser) throws ObjectAlreadyExistsException, IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException("The user is not authorized to create Crowdin hook");
        }

        WebHook existsWebHook = webHookStorage.getWebhookByProjectId(projectId);
        if (existsWebHook != null) {
            throw new ObjectAlreadyExistsException(existsWebHook);
        }

        WebHook webHook = crowdinConsumerService.createWebhook(projectId, CROWDIN_TRIGGERS, accessToken);

        if (webHook != null) {
            webHook.setProjectName(projectName);
            webHook.setWatchedBy(currentUser);
            webHookStorage.saveWebHook(webHook);
        }
    }

    @Override
    public List<WebHook> getWebhooks(String currentUser, int offset, int limit, boolean forceUpdate) throws IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS);
        }
        return getWebhooks(offset, limit, forceUpdate);
    }

    @Override
    public int countWebhooks(String currentUser, boolean forceUpdate) throws IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS);
        }
        if (forceUpdate) {
            forceUpdateWebhooks();
        }
        return webHookStorage.countWebhooks();
    }

    @Override
    public boolean isWebHookWatchLimitEnabled(long projectRemoteId) {
        return false;
    }

    @Override
    public void setWebHookWatchLimitEnabled(long projectRemoteId, boolean enabled, String currentUser) throws IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException("The user is not authorized to update webHook watch limit status");
        }
        settingService.set(CROWDIN_WEBHOOK_CONTEXT, WATCH_LIMITED_SCOPE, String.valueOf(projectRemoteId), SettingValue.create(enabled));
    }

    @Override
    public void deleteWebhookHook(long projectId, String currentUser) throws IllegalAccessException, ObjectNotFoundException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException("The user is not authorized to delete GitHub hook");
        }
        WebHook webHook = webHookStorage.getWebhookByProjectId(projectId);
        if (webHook == null) {
            throw new ObjectNotFoundException("Crowdin hook for project id : " + projectId + " wasn't found");
        }
        String response = crowdinConsumerService.deleteWebhook(webHook);
        if (response != null) {
            deleteWebhook(projectId);
        }
    }

    public void deleteWebhook(long projectId) {
        webHookStorage.deleteWebHook(projectId);
        RuleFilter ruleFilter = new RuleFilter(true);
        ruleFilter.setEventType(CONNECTOR_NAME);
        ruleFilter.setIncludeDeleted(true);
        List<RuleDTO> rules = ruleService.getRules(ruleFilter, 0, -1);
        rules.stream()
                .filter(r -> !r.getEvent().getProperties().isEmpty()
                        && r.getEvent().getProperties().get(PROJECT_ID).equals(String.valueOf(projectId)))
                .map(RuleDTO::getId)
                .forEach(ruleService::deleteRuleById);
    }
    
    public List<WebHook> getWebhooks(int offset, int limit, boolean forceUpdate) {
        if (forceUpdate) {
            forceUpdateWebhooks();
        }
        return getWebhooks(offset, limit);
    }

    @Override
    public void forceUpdateWebhooks() {
        crowdinConsumerService.clearCache();
        List<WebHook> webHook = getWebhooks(0, -1);
        webHook.forEach(this::forceUpdateWebhook);
    }

    public List<WebHook> getWebhooks(int offset, int limit) {
        List<Long> hooksIds = webHookStorage.getWebhookIds(offset, limit);
        return hooksIds.stream().map(webHookStorage::getWebHookById).toList();
    }

    private void forceUpdateWebhook(WebHook webHook) {
        // TODO
    }
}
