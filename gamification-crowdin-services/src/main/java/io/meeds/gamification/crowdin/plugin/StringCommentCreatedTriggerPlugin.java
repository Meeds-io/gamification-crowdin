package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import io.meeds.gamification.crowdin.services.CrowdinTriggerService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.meeds.gamification.crowdin.utils.Utils.extractSubItem;

@Component
public class StringCommentCreatedTriggerPlugin extends CrowdinTriggerPlugin {

    protected String EVENT_PAYLOAD_OBJECT_NAME = "comment";
    protected String EVENT_TITLE =  "stringCommentCreated";
    protected String EVENT_TRIGGER =  "stringComment.created";
    protected String CANCELLING_EVENT_TRIGGER =  "stringComment.deleted";

    @Autowired
    private CrowdinTriggerService crowdinTriggerService;

    @PostConstruct
    public void initData() {
        crowdinTriggerService.addPlugin(this);
    }

    @Override
    public List<Event> getEvents(String trigger, Map<String, Object> payload, Object object) {
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

    @Override
    public boolean batchQueryRemoteTranslations() {
        return false;
    }
}
