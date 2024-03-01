package io.meeds.gamification.crowdin.plugin;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.service.EventRegistry;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SuggestionTriggerPlugin extends BaseComponentPlugin {

    private static final Log LOG  = ExoLogger.getLogger(SuggestionTriggerPlugin.class);

    protected String EVENT_TITLE = "suggestionAdded";
    protected String EVENT_TYPE = "crowdin";
    protected String EVENT_TRIGGER =  "suggestion.added";
    protected List<String> EVENT_CANCELLER_EVENTS = Collections.singletonList("suggestionDeleted");


}
