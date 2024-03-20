package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import io.meeds.gamification.crowdin.model.RemoteTranslation;
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

import static io.meeds.gamification.crowdin.utils.Utils.*;

@Component
public class SuggestionApprovedTriggerPlugin extends CrowdinTriggerPlugin {
    private static final Log LOG                = ExoLogger.getLogger(SuggestionAddedTriggerPlugin.class);
    protected String EVENT_PAYLOAD_OBJECT_NAME = "translation";
    protected String CROWDIN_EVENT_TRIGGER =  "suggestion.approved";
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

        List<Event> eventList = new ArrayList<>();
        eventList.add(new Event(APPROVE_SUGGESTION_EVENT_NAME,
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "user", "username"),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "user", "username"),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "string", "url"),
                EVENT_PAYLOAD_OBJECT_NAME,
                getProjectId(payload),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "targetLanguage", "id"),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "provider") == null,
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "string", "file", "directoryId"),
                trigger.equals(CANCELLING_EVENT_TRIGGER)));

        if (object == null) {
            return eventList;
        }

        List<RemoteTranslation> remoteTranslations = (List<RemoteTranslation>) object;
        LOG.debug("remoteTranslations: " + remoteTranslations.size());
        String translationId = extractSubItem(payload, getPayloadObjectName(), "id");
        if (translationId != null) {

            RemoteTranslation translationById = null;

            for (RemoteTranslation translation : remoteTranslations) {
                if (translation.getId() == Long.parseLong(translationId)) {
                    translationById = translation;
                    break;
                }
            }

            if (translationById != null) {
                eventList.add(new Event(SUGGESTION_APPROVED_EVENT_NAME,
                        translationById.getUsername(),
                        translationById.getUsername(),
                        extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "string", "url"),
                        EVENT_PAYLOAD_OBJECT_NAME,
                        getProjectId(payload),
                        extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "targetLanguage", "id"),
                        extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "provider") == null,
                        extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "string", "file", "directoryId"),
                        trigger.equals(CANCELLING_EVENT_TRIGGER)));
            }
        }

        return eventList;
    }

    @Override
    public String getEventName() {
        return CROWDIN_EVENT_TRIGGER;
    }

    @Override
    public String getCancellingEventName() {
        return CANCELLING_EVENT_TRIGGER;
    }

    @Override
    public String getPayloadObjectName() {
        return EVENT_PAYLOAD_OBJECT_NAME;
    }

    @Override
    public String getProjectId(Map<String, Object> payload) {
        return extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, "string", "project", "id");
    }

    @Override
    public boolean batchQueryRemoteTranslations() {
        return true;
    }
}
