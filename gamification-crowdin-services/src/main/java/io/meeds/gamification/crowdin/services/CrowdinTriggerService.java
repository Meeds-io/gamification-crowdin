package io.meeds.gamification.crowdin.services;

import io.meeds.common.ContainerTransactional;
import io.meeds.gamification.crowdin.model.Event;
import io.meeds.gamification.crowdin.model.WebHook;
import io.meeds.gamification.crowdin.plugin.CrowdinTriggerPlugin;
import io.meeds.gamification.crowdin.storage.CrowdinConsumerStorage;
import io.meeds.gamification.crowdin.storage.WebHookStorage;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.EventService;
import io.meeds.gamification.service.TriggerService;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.meeds.gamification.crowdin.utils.Utils.*;

@Service
public class CrowdinTriggerService {

    private static final Log LOG                = ExoLogger.getLogger(CrowdinTriggerService.class);
    private final Map<String, CrowdinTriggerPlugin> triggerPlugins = new HashMap<>();

    @Autowired
    private TriggerService triggerService;

    @Autowired
    private ConnectorService connectorService;

    @Autowired
    private IdentityManager identityManager;

    @Autowired
    private EventService eventService;

    @Autowired
    private ListenerService listenerService;
    
    @Autowired
    private WebHookStorage webHookStorage;

    @Autowired
    private CrowdinConsumerStorage crowdinConsumerStorage;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public void handleTriggerAsync(String bearerToken, String payload) {
        threadPoolTaskExecutor.execute(() -> handleTrigger(bearerToken, payload));
    }

    @ContainerTransactional
    @SuppressWarnings("unchecked")
    private void handleTrigger(String bearerToken, String payload) {

        Map<String, Object> payloadMap = fromJsonStringToMap(payload);

        Object eventsObj = payloadMap.get("events");
        List<Map<String, Object>> eventsListMap = (List<Map<String, Object>>) eventsObj;
        LOG.info("Total Events: " + eventsListMap.size());
        List<Map<String, Object>> projectTranslationsMapList = getTranslationAuthors(eventsListMap);
        for (Map<String, Object> eventMap: eventsListMap) {
            String trigger = extractSubItem(eventMap, "event");
            CrowdinTriggerPlugin triggerPlugin = getCrowdinTriggerPlugin(trigger);
            if (triggerPlugin == null) {
                LOG.error("Trigger plugin for trigger : " + trigger + " wasn't found");
            } else {

                String projectId = extractSubItem(eventMap, triggerPlugin.getPayloadObjectName(), "string", "project", "id");

                if (projectId == null) {
                    LOG.error("Project id is not found in the payload");
                    continue;
                }

                WebHook webHook = webHookStorage.getWebhookByProjectId(Long.parseLong(projectId));
                if (webHook == null) {
                    LOG.error("Crowdin hook for project id : " + projectId + " wasn't found");
                    continue;
                }

                if (! verifyWebhookSecret(bearerToken.substring(7), webHook.getSecret())) {
                    LOG.error("Verifying Crowdin webhook secret failed");
                    continue;
                }

                Map<String, Object> projectTranslationAuthorsMap = projectTranslationsMapList.stream()
                        .filter(map -> map.containsKey("projectId") && map.get("projectId").equals(projectId))
                        .findFirst()
                        .orElseGet(Collections::emptyMap);

                processEvents(triggerPlugin.getEvents(trigger, eventMap, projectTranslationAuthorsMap), projectId);
            }
        }
    }

    private void processEvents(List<Event> events, String projectId) {
        events.stream().filter(event -> isTriggerEnabled(event.getName(), projectId)).forEach(this::processEvent);
    }

    private boolean isTriggerEnabled(String trigger, String projectId) {
        return triggerService.isTriggerEnabledForAccount(trigger, Long.parseLong(projectId));
    }

    private void processEvent(Event event) {
        String receiverId = connectorService.getAssociatedUsername(CONNECTOR_NAME, event.getReceiver());
        String senderId;
        if (event.getSender() != null && !StringUtils.equals(event.getReceiver(), event.getSender())) {
            senderId = connectorService.getAssociatedUsername(CONNECTOR_NAME, event.getSender());
        } else {
            senderId = receiverId;
        }
        LOG.info("processEvent: senderId: " + senderId);
        if (StringUtils.isNotBlank(senderId)) {
            Identity socialIdentity = identityManager.getOrCreateUserIdentity(senderId);
            LOG.info("processEvent: socialIdentity: " + socialIdentity);
            if (socialIdentity != null) {
                broadcastCrowdinEvent(event, senderId, receiverId);
            }
        }
    }

    private void broadcastCrowdinEvent(Event event, String senderId, String receiverId) {
        try {
            String eventDetails = "{" + "projectId" + ": " + event.getProjectId() + "}";
            Map<String, String> gam = new HashMap<>();
            gam.put("senderId", senderId);
            gam.put("receiverId", receiverId);
            gam.put("objectId", event.getObjectId());
            gam.put("objectType", event.getObjectType());
            gam.put("eventDetails", eventDetails);
            gam.put("ruleTitle", event.getName());
            LOG.info("processEvent: gam: " + gam);
            if ( ! event.isCancelling()) {
                listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, "");
                LOG.info("Crowdin action {} broadcast for user {}", event.getName(), senderId);
            } else {
                listenerService.broadcast(GAMIFICATION_CANCEL_EVENT, gam, "");
                LOG.info("Crowdin cancelling action {} broadcast for user {}", event.getName(), senderId);
            }

        } catch (Exception e) {
            LOG.error("Cannot broadcast crowdin event", e);
        }
    }


    private CrowdinTriggerPlugin getCrowdinTriggerPlugin(String trigger) {
        return triggerPlugins.get(trigger);
    }

    public void addPlugin(CrowdinTriggerPlugin crowdinTriggerPlugin) {
        triggerPlugins.put(crowdinTriggerPlugin.getEventName(), crowdinTriggerPlugin);
        triggerPlugins.put(crowdinTriggerPlugin.getCancellingEventName(), crowdinTriggerPlugin);
    }

    public List<Map<String, Object>> getTranslationAuthors(List<Map<String, Object>> eventsListMap) {

        List<Map<String, Object>> projectTranslationsMapList = new ArrayList<>();

        List<String> projectIds = eventsListMap.stream()
                .filter(map -> map.containsKey("event") && map.get("event").equals("suggestion.approved"))
                .map(map -> extractSubItem(map, "translation", "string", "project", "id"))
                .toList();

        for (String projectId: projectIds) {

            WebHook webHook = webHookStorage.getWebhookByProjectId(Long.parseLong(projectId));

            List<String> translationIds = eventsListMap.stream()
                    .filter(map -> map.containsKey("event") && map.get("event").equals("suggestion.approved"))
                    .filter(map -> Objects.equals(extractSubItem(map, "translation", "string", "project", "id"), projectId))
                    .map(map -> extractSubItem(map, "translation", "id"))
                    .toList();

            try {

                Map<Long, String> translationIdUsernameMap = crowdinConsumerStorage.getProjectTranslationAuthors(projectId, translationIds, webHook.getToken());
                Map<String, Object> projectTranslationAuthorsMap = new HashMap<>();
                projectTranslationAuthorsMap.put("projectId", projectId);
                projectTranslationAuthorsMap.put("translationIdUsernameMap", translationIdUsernameMap);
                projectTranslationsMapList.add(projectTranslationAuthorsMap);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return projectTranslationsMapList;
    }

}
