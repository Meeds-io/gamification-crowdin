package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import io.meeds.gamification.crowdin.services.CrowdinTriggerService;
import jakarta.annotation.PostConstruct;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.meeds.gamification.crowdin.utils.Utils.extractSubItem;

@Component
public class SuggestionAddedTriggerPlugin extends CrowdinTriggerPlugin {
    private static final Log LOG                = ExoLogger.getLogger(SuggestionAddedTriggerPlugin.class);
    protected String EVENT_PAYLOAD_OBJECT_NAME = "translation";
    protected String EVENT_TITLE =  "suggestionAdded";
    protected String EVENT_TRIGGER =  "suggestion.added";
    protected String CANCELLING_EVENT_TRIGGER =  "suggestion.deleted";

    @Autowired
    private CrowdinTriggerService crowdinTriggerService;

    @PostConstruct
    public void initData() {
        crowdinTriggerService.addPlugin(this);
    }

    @Override
    public List<Event> getEvents(String trigger, Map<String, Object> payload, Object object) {
        if (extractSubItem(payload, getPayloadObjectName(), "provider") != null) {
            LOG.warn("Crowdin event {} translation provider is TM or MT", EVENT_TRIGGER);
            return Collections.emptyList();
        }

        return Collections.singletonList(new Event(EVENT_TITLE,
                extractSubItem(payload, getPayloadObjectName(), "user", "username"),
                extractSubItem(payload, getPayloadObjectName(), "user", "username"),
                extractSubItem(payload, getPayloadObjectName(), "string", "url"),
                EVENT_PAYLOAD_OBJECT_NAME,
                extractSubItem(payload, getPayloadObjectName(), "string", "project", "id"),
                trigger.equals(CANCELLING_EVENT_TRIGGER)));
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
