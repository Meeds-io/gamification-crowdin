package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import io.meeds.gamification.crowdin.services.CrowdinTriggerService;
import jakarta.annotation.PostConstruct;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.meeds.gamification.crowdin.utils.Utils.extractSubItem;

@Component
public class SuggestionApprovedTriggerPlugin extends CrowdinTriggerPlugin {
    private static final Log LOG                = ExoLogger.getLogger(SuggestionAddedTriggerPlugin.class);
    protected String EVENT_PAYLOAD_OBJECT_NAME = "translation";
    protected String SUGGESTION_APPROVED_EVENT_TITLE =  "suggestionApproved";
    protected String APPROVE_SUGGESTION_EVENT_TITLE =  "approveSuggestion";
    protected String EVENT_TRIGGER =  "suggestion.approved";
    protected String CANCELLING_EVENT_TRIGGER =  "suggestion.disapproved";

    @Autowired
    private CrowdinTriggerService crowdinTriggerService;

    @PostConstruct
    public void initData() {
        crowdinTriggerService.addPlugin(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> getEvents(String trigger, Map<String, Object> payload, Object object) {
        if (extractSubItem(payload, getPayloadObjectName(), "provider") != null) {
            LOG.warn("Crowdin event {} translation provider is TM or MT", EVENT_TRIGGER);
            return Collections.emptyList();
        }

        List<Event> eventList = new ArrayList<>();
        eventList.add(new Event(APPROVE_SUGGESTION_EVENT_TITLE,
                extractSubItem(payload, getPayloadObjectName(), "user", "username"),
                extractSubItem(payload, getPayloadObjectName(), "user", "username"),
                extractSubItem(payload, getPayloadObjectName(), "id"),
                EVENT_PAYLOAD_OBJECT_NAME,
                extractSubItem(payload, getPayloadObjectName(), "string", "project", "id"),
                trigger.equals(CANCELLING_EVENT_TRIGGER)));

        if (object == null) {
            return eventList;
        }

        Map<String, Object> translationAuthorsMap = (Map<String, Object>) object;
        LOG.debug("translationAuthorsMap size: " + translationAuthorsMap.size());
        Map<Long, String> translationsAuthors = (Map<Long, String>) translationAuthorsMap.get("translationIdUsernameMap");
        String translationId = extractSubItem(payload, getPayloadObjectName(), "id");
        if (translationId != null) {
            String authorUsername = translationsAuthors.get(Long.parseLong(translationId));

            if (authorUsername != null) {
                eventList.add(new Event(SUGGESTION_APPROVED_EVENT_TITLE,
                        authorUsername,
                        authorUsername,
                        extractSubItem(payload, getPayloadObjectName(), "id"),
                        EVENT_PAYLOAD_OBJECT_NAME,
                        extractSubItem(payload, getPayloadObjectName(), "string", "project", "id"),
                        trigger.equals(CANCELLING_EVENT_TRIGGER)));
            }
        }

        return eventList;
    }

    @Override
    public String getEventName() {
        return EVENT_TRIGGER;
    }

    @Override
    public String getCancellingEventName() {
        return CANCELLING_EVENT_TRIGGER;
    }

    @Override
    public String getPayloadObjectName() {
        return EVENT_PAYLOAD_OBJECT_NAME;
    }
}
