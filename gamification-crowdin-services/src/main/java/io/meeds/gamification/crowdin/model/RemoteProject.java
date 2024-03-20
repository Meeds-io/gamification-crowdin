package io.meeds.gamification.crowdin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteProject implements Cloneable {
    private long                 id;
    private String               identifier;
    private String               name;
    private String               description;
    private String               avatarUrl;
    private List<RemoteLanguage> languages;

    @Override
    public RemoteProject clone() {
        return new RemoteProject(id, identifier, name, description, avatarUrl, languages);
    }
}
