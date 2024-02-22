package io.meeds.gamification.crowdin.services;

import io.meeds.gamification.crowdin.model.RemoteProject;
import io.meeds.gamification.crowdin.model.WebHook;

import java.util.List;

public interface CrowdinConsumerService {

    List<RemoteProject> getProjects(String accessToken) throws IllegalAccessException;

    WebHook createWebhook(long projectId, String[] crowdinTriggers, String accessToken) throws IllegalAccessException;


    /**
     * delete crowdin webhook
     *
     * @param webHook github webHook
     */
    String deleteWebhook(WebHook webHook);
    
    /**
     * Retrieve available crowdin project info.
     *
     * @param projectRemoteId crowdin project remote Id
     * @param accessToken crowdin access token
     * @return {@link RemoteProject}
     */
    RemoteProject retrieveRemoteProject(long projectRemoteId, String accessToken) throws IllegalAccessException;


    /**
     * clear remote webhook entities cache
     */
    void clearCache();
}
