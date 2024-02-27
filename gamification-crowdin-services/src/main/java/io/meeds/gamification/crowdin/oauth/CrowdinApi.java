package io.meeds.gamification.crowdin.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;

public class CrowdinApi extends DefaultApi20 {
    protected CrowdinApi() {
    }

    private static class InstanceHolder {
        private static final CrowdinApi INSTANCE = new CrowdinApi();
    }

    public static CrowdinApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://accounts.crowdin.com/oauth/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://accounts.crowdin.com/oauth/authorize";
    }

    @Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return OAuth2AccessTokenJsonExtractor.instance();
    }
}
