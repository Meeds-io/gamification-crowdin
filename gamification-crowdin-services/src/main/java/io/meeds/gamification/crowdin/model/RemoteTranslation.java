package io.meeds.gamification.crowdin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteTranslation {
    private long         id;
    private String       projectId;
    private String       username;
}
