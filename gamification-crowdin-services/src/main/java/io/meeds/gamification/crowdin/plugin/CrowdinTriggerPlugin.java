package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import org.exoplatform.container.component.BaseComponentPlugin;

import java.util.List;
import java.util.Map;

public abstract class CrowdinTriggerPlugin extends BaseComponentPlugin {

    /**
     * Gets List of triggered events
     *
     * @param payload                      payload The raw payload of the webhook request.
     * @param object                       additional processing object
     * @return List of triggered events
     */
    public abstract List<Event> getEvents(
            String trigger,
            Map<String, Object> payload,
            Object object);

    public abstract String getEventName();
    public abstract String getCancellingEventName();

    /**
     * Gets json object event key name in the payload
     *
     * @return object name
     */
    public abstract String getPayloadObjectName();
    public abstract boolean batchQueryRemoteTranslations();
}
