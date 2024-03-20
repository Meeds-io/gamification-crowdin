package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.plugin.EventPlugin;
import io.meeds.gamification.service.EventService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.collections.CollectionUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.meeds.gamification.crowdin.utils.Utils.*;
@Component
public class CrowdinEventPlugin extends EventPlugin {

    private static final Log LOG                = ExoLogger.getLogger(CrowdinEventPlugin.class);

    public static final String EVENT_TYPE = "crowdin";

    @Autowired
    private EventService eventService;

    @PostConstruct
    public void initData() {
        eventService.addPlugin(this);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public List<String> getTriggers() {
        return List.of(
                STRING_COMMENT_CREATED_EVENT_NAME,
                SUGGESTION_ADDED_EVENT_NAME,
                SUGGESTION_APPROVED_EVENT_NAME,
                APPROVE_SUGGESTION_EVENT_NAME
        );
    }

    @Override
    public boolean isValidEvent(Map<String, String> eventProperties, String triggerDetails) {
        LOG.info("isValidEvent: started");
        LOG.info("eventProperties: " + eventProperties);
        LOG.info("triggerDetails: " + triggerDetails);

        String desiredProjectId = eventProperties.get(PROJECT_ID);

        String desiredMustBeHuman = eventProperties.get(MUST_BE_HUMAN);

        List<String> desiredDirectoryIds =
                eventProperties.get(DIRECTORY_IDS) != null ? Arrays.asList(eventProperties.get(DIRECTORY_IDS)
                        .split(","))
                        : Collections.emptyList();

        List<String> desiredLanguageIds =
                eventProperties.get(LANGUAGE_IDS) != null ? Arrays.asList(eventProperties.get(LANGUAGE_IDS)
                        .split(","))
                        : Collections.emptyList();

        LOG.info("desiredDirectoryIds: " + desiredDirectoryIds);
        LOG.info("desiredLanguageIds: " + desiredLanguageIds);


        Map<String, String> triggerDetailsMop = stringToMap(triggerDetails);
        LOG.info("triggerDetailsMop: " + triggerDetailsMop);

        return desiredProjectId.equals(triggerDetailsMop.get(PROJECT_ID))
                && desiredMustBeHuman.equals(triggerDetailsMop.get(MUST_BE_HUMAN))
                && (CollectionUtils.isEmpty(desiredDirectoryIds) || desiredDirectoryIds.contains(triggerDetailsMop.get(DIRECTORY_ID)))
                && (CollectionUtils.isEmpty(desiredLanguageIds) || desiredLanguageIds.contains(triggerDetailsMop.get(LANGUAGE_ID)));
    }

    private static Map<String, String> stringToMap(String mapAsString) {
        Map<String, String> map = new HashMap<>();
        mapAsString = mapAsString.substring(1, mapAsString.length() - 1);
        String[] pairs = mapAsString.split(", ");
        for (String pair : pairs) {
            String[] keyValue = pair.split(": ");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            map.put(key, value);
        }
        return map;
    }
}
