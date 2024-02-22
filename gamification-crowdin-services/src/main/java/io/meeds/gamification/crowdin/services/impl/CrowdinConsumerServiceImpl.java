package io.meeds.gamification.crowdin.services.impl;

import io.meeds.gamification.crowdin.model.RemoteProject;
import io.meeds.gamification.crowdin.model.WebHook;
import io.meeds.gamification.crowdin.services.CrowdinConsumerService;
import io.meeds.gamification.crowdin.storage.CrowdinConsumerStorage;

import java.util.List;

public class CrowdinConsumerServiceImpl implements CrowdinConsumerService {

    private CrowdinConsumerStorage crowdinConsumerStorage;

    public CrowdinConsumerServiceImpl(CrowdinConsumerStorage crowdinConsumerStorage) {
        this.crowdinConsumerStorage = crowdinConsumerStorage;
    }

    @Override
    public List<RemoteProject> getProjects(String accessToken) throws IllegalAccessException {
        return crowdinConsumerStorage.getProjects(accessToken);
    }

    @Override
    public WebHook createWebhook(long projectId, String[] triggers, String accessToken) throws IllegalAccessException {
        return crowdinConsumerStorage.createWebhook(projectId, triggers, accessToken);
    }

    @Override
    public String deleteWebhook(WebHook webHook) {
        return crowdinConsumerStorage.deleteWebhookHook(webHook);
    }

    @Override
    public RemoteProject retrieveRemoteProject(long projectRemoteId, String accessToken) throws IllegalAccessException {
        return crowdinConsumerStorage.retrieveRemoteProject(projectRemoteId, accessToken);
    }

    @Override
    public void clearCache() {
        crowdinConsumerStorage.clearCache();
    }
}
