package io.meeds.gamification.crowdin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteDirectory {
    private long         id;
    private long       projectId;
    private String       path;
}
