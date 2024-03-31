package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import io.meeds.gamification.crowdin.services.CrowdinTriggerService;
import io.meeds.gamification.model.RealizationDTO;
import io.meeds.gamification.service.RealizationService;
import jakarta.annotation.PostConstruct;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    @Autowired
    private RealizationService realizationService;

    @PostConstruct
    public void initData() {
        crowdinTriggerService.addPlugin(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> getEvents(String trigger, Map<String, Object> payload) {
        String objectId = constructObjectIdAsJsonString(payload, EVENT_PAYLOAD_OBJECT_NAME);

        List<Event> eventList = new ArrayList<>();
        eventList.add(new Event(APPROVE_SUGGESTION_EVENT_NAME,
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, USER, USERNAME),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, USER, USERNAME),
                objectId,
                EVENT_PAYLOAD_OBJECT_NAME,
                getProjectId(payload),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, TARGET_LANGUAGE, ID),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, PROVIDER) == null,
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, STRING, FILE, DIRECTORY_ID),
                trigger.equals(CANCELLING_EVENT_TRIGGER)));

        List<RealizationDTO> realizations = realizationService.
                findRealizationsByObjectIdAndObjectType(objectId, EVENT_PAYLOAD_OBJECT_NAME);

        if ( ! realizations.isEmpty()) {
            String earnerId = realizations.get(0).getEarnerId();
            eventList.add(new Event(SUGGESTION_APPROVED_EVENT_NAME,
                    earnerId,
                    earnerId,
                    objectId,
                    EVENT_PAYLOAD_OBJECT_NAME,
                    getProjectId(payload),
                    extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, TARGET_LANGUAGE, ID),
                    extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, PROVIDER) == null,
                    extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, STRING, FILE, DIRECTORY_ID),
                    trigger.equals(CANCELLING_EVENT_TRIGGER)));

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
    public String getProjectId(Map<String, Object> payload) {
        return extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, STRING, PROJECT, ID);
    }
}
