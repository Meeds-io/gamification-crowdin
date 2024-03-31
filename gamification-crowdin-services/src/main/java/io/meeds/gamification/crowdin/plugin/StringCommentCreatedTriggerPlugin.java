package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import io.meeds.gamification.crowdin.services.CrowdinTriggerService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.meeds.gamification.crowdin.utils.Utils.*;

@Component
public class StringCommentCreatedTriggerPlugin extends CrowdinTriggerPlugin {

    protected String EVENT_PAYLOAD_OBJECT_NAME = "comment";
    protected String EVENT_TRIGGER =  "stringComment.created";
    protected String CANCELLING_EVENT_TRIGGER =  "stringComment.deleted";

    @Autowired
    private CrowdinTriggerService crowdinTriggerService;

    @PostConstruct
    public void initData() {
        crowdinTriggerService.addPlugin(this);
    }

    @Override
    public List<Event> getEvents(String trigger, Map<String, Object> payload) {
        return Collections.singletonList(new Event(STRING_COMMENT_CREATED_EVENT_NAME,
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, USER, USERNAME),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, USER, USERNAME),
                constructObjectIdAsJsonString(payload, EVENT_PAYLOAD_OBJECT_NAME),
                EVENT_PAYLOAD_OBJECT_NAME,
                getProjectId(payload),
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, TARGET_LANGUAGE, ID),
                true,
                extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, STRING, FILE, DIRECTORY_ID),
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
    public String getProjectId(Map<String, Object> payload) {
        return extractSubItem(payload, EVENT_PAYLOAD_OBJECT_NAME, STRING, PROJECT, ID);
    }
}
