package io.meeds.gamification.crowdin.services;

import io.meeds.gamification.crowdin.model.RemoteProject;
import io.meeds.gamification.crowdin.model.WebHook;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import java.util.List;

public interface WebhookService {

    /**
     * Get available crowdin projects
     *
     * @param accessToken crowdin personal access token
     * @return {@link List} of {@link RemoteProject}
     * @throws IllegalAccessException when user tokes invalid or expired
     */
    List<RemoteProject> getProjects(String accessToken) throws IllegalAccessException;

    /**
     * create crowdin project hook.
     *
     * @param projectId crowdin project id
     * @param projectName crowdin project name
     * @param accessToken crowdin personal access token
     * @param currentUser user name attempting to create crowdin hook
     * @throws ObjectAlreadyExistsException when webhook already exists
     * @throws IllegalAccessException when user is not authorized to create crowdin
     *           webhook
     * @throws ObjectNotFoundException when the crowdin project identified by
     *           its id is not found
     */
    void createWebhook(long projectId, String projectName, String accessToken, String currentUser) throws ObjectAlreadyExistsException,
            IllegalAccessException,
            ObjectNotFoundException;

    /**
     * Get available crowdin hooks using offset and limit.
     *
     * @param currentUser user name attempting to access connector hooks
     * @param offset Offset of result
     * @param limit Limit of result
     * @param forceUpdate force Load remote webhook or not.
     * @return {@link List} of {@link WebHook}
     * @throws IllegalAccessException when user is not authorized to access crowdin
     *           hooks
     */
    List<WebHook> getWebhooks(String currentUser, int offset, int limit, boolean forceUpdate) throws IllegalAccessException;

    /**
     * Count all crowdin webhooks
     *
     * @param currentUser User name accessing webhooks
     * @param forceUpdate force Load remote webhooks count or not.
     * @return webhooks count
     * @throws IllegalAccessException when user is not authorized to get github
     *           webhooks
     */
    int countWebhooks(String currentUser, boolean forceUpdate) throws IllegalAccessException;
    

    /**
     * Check if webhook watch limit is enabled
     *
     * @param projectRemoteId crowdin project remote Id
     * @return true if webHook watch limit is enabled, else false.
     */
    boolean isWebHookWatchLimitEnabled(long projectRemoteId);
    
    /**
     * Limit webhook watch scope or not
     *
     * @param projectRemoteId crowdin project remote Id
     * @param enabled true to enabled, else false
     * @param currentUser user name attempting to enables/disables webHook watch
     *          limit.
     * @throws IllegalAccessException when user is not authorized Limit webhook
     *           watch scope
     */
    void setWebHookWatchLimitEnabled(long projectRemoteId, boolean enabled, String currentUser) throws IllegalAccessException;


    /**
     * delete crowdin webhook
     *
     * @param projectId github remote project id
     * @param currentUser user name attempting to delete crowdin hook
     * @throws IllegalAccessException when user is not authorized to delete the
     *           crowdin hook
     */
    void deleteWebhookHook(long projectId, String currentUser) throws IllegalAccessException, ObjectNotFoundException;
    
    /**
     * Force update the stored crowdin project webhooks if there is a change to
     * the remote webhooks, such as an entity deletion or the update event
     **/
    void forceUpdateWebhooks();
}
