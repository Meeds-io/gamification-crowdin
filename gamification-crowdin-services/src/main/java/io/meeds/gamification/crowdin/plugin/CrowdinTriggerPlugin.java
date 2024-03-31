package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.crowdin.model.Event;
import org.exoplatform.container.component.BaseComponentPlugin;

import java.util.List;
import java.util.Map;

public abstract class CrowdinTriggerPlugin extends BaseComponentPlugin {

    /**
     * Gets List of triggered events
     *
     * @param trigger                      trigger event name
     * @param payload                      payload The raw payload of the webhook request.
     * @return List of triggered events
     */
    public abstract List<Event> getEvents(
            String trigger,
            Map<String, Object> payload);

    public abstract String getEventName();
    public abstract String getCancellingEventName();
    public abstract String getProjectId(Map<String, Object> payload);
}
