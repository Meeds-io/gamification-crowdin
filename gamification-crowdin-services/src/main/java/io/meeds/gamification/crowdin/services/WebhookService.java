/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.crowdin.services;

import io.meeds.gamification.crowdin.model.RemoteProject;
import io.meeds.gamification.crowdin.model.WebHook;
import io.meeds.gamification.crowdin.storage.CrowdinConsumerStorage;
import io.meeds.gamification.crowdin.storage.WebHookStorage;
import io.meeds.gamification.utils.Utils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.meeds.gamification.crowdin.utils.Utils.AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS;


@Service
public class WebhookService {

    private static final Context CROWDIN_WEBHOOK_CONTEXT = Context.GLOBAL.id("crowdinWebhook");

    private static final Scope WATCH_LIMITED_SCOPE    = Scope.APPLICATION.id("watchLimited");

    @Autowired
    private CrowdinConsumerStorage crowdinConsumerStorage;

    @Autowired
    private  SettingService settingService;


    @Autowired
    private  WebHookStorage webHookStorage;

    private static final String[]       CROWDIN_TRIGGERS        = new String[] { "file.added", "file.updated",
            "file.reverted", "file.deleted", "file.translated", "file.approved", "project.translated",
            "project.approved", "project.built", "translation.updated", "string.added", "string.updated",
            "string.deleted", "stringComment.created", "stringComment.updated", "stringComment.deleted",
            "stringComment.restored", "suggestion.added", "suggestion.updated", "suggestion.deleted",
            "suggestion.approved", "suggestion.disapproved", "task.added", "task.statusChanged", "task.deleted" };


    public List<RemoteProject> getProjects(String accessToken) throws IllegalAccessException {
        return crowdinConsumerStorage.getProjects(accessToken);
    }


    public void createWebhook(long projectId, String projectName, String accessToken, String currentUser) throws ObjectAlreadyExistsException, IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException("The user is not authorized to create Crowdin hook");
        }

        WebHook existsWebHook = webHookStorage.getWebhookByProjectId(projectId);
        if (existsWebHook != null) {
            throw new ObjectAlreadyExistsException(existsWebHook);
        }

        WebHook webHook = crowdinConsumerStorage.createWebhook(projectId, CROWDIN_TRIGGERS, accessToken);

        if (webHook != null) {
            webHook.setProjectName(projectName);
            webHook.setWatchedBy(currentUser);
            webHookStorage.saveWebHook(webHook);
        }
    }

    public List<WebHook> getWebhooks(String currentUser, int offset, int limit, boolean forceUpdate) throws IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS);
        }
        return getWebhooks(offset, limit, forceUpdate);
    }


    public int countWebhooks(String currentUser, boolean forceUpdate) throws IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS);
        }
        if (forceUpdate) {
            forceUpdateWebhooks();
        }
        return webHookStorage.countWebhooks();
    }


    public boolean isWebHookWatchLimitEnabled(long projectRemoteId) {
        return false;
    }


    public void setWebHookWatchLimitEnabled(long projectRemoteId, boolean enabled, String currentUser) throws IllegalAccessException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException("The user is not authorized to update webHook watch limit status");
        }
        settingService.set(CROWDIN_WEBHOOK_CONTEXT, WATCH_LIMITED_SCOPE, String.valueOf(projectRemoteId), SettingValue.create(enabled));
    }


    public void deleteWebhookHook(long projectId, String currentUser) throws IllegalAccessException, ObjectNotFoundException {
        if (!Utils.isRewardingManager(currentUser)) {
            throw new IllegalAccessException("The user is not authorized to delete GitHub hook");
        }
        WebHook webHook = webHookStorage.getWebhookByProjectId(projectId);
        if (webHook == null) {
            throw new ObjectNotFoundException("Crowdin hook for project id : " + projectId + " wasn't found");
        }
        String response = crowdinConsumerStorage.deleteWebhook(webHook);
        if (response != null) {
            deleteWebhook(projectId);
        }
    }

    public void deleteWebhook(long projectId) {
        webHookStorage.deleteWebHook(projectId);
    }

    public List<WebHook> getWebhooks(int offset, int limit, boolean forceUpdate) {
        if (forceUpdate) {
            forceUpdateWebhooks();
        }
        return getWebhooks(offset, limit);
    }


    public void forceUpdateWebhooks() {
        crowdinConsumerStorage.clearCache();
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
