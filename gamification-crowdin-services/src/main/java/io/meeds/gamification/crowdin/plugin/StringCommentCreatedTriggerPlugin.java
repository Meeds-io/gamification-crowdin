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
    protected String EVENT_NAME =  "stringComment.created";

    @Autowired
    private CrowdinTriggerService crowdinTriggerService;

    @PostConstruct
    public void initData() {
        crowdinTriggerService.addPlugin(this);
    }

    @Override
    public List<Event> getEvents(Map<String, Object> payload) {
        return Collections.singletonList(new Event(EVENT_NAME,
                null,
                extractSubItem(payload, getPayloadObjectName(), "user", "username"),
                extractSubItem(payload, getPayloadObjectName(), "id"),
                null,
                extractSubItem(payload, getPayloadObjectName(), "string", "project", "id")));
    }

    @Override
    public String getName() {
        return EVENT_NAME;
    }

    @Override
    public String getPayloadObjectName() {
        return EVENT_PAYLOAD_OBJECT_NAME;
    }
}