package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import org.exoplatform.container.component.BaseComponentPlugin;

import java.util.List;
import java.util.Map;

public abstract class CrowdinTriggerPlugin extends BaseComponentPlugin {

    /**
     * Gets List of triggered events
     *
     * @param payload payload The raw payload of the webhook request.
     * @return List of triggered events
     */
    public abstract List<Event> getEvents(Map<String, Object> payload);

    public abstract String getName();

    public abstract String getPayloadObjectName();
}
