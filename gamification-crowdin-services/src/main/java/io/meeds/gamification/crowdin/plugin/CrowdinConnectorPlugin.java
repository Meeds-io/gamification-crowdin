/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.crowdin.plugin;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import io.meeds.gamification.crowdin.oauth.CrowdinApi;
import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.plugin.ConnectorPlugin;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

@Service
public class CrowdinConnectorPlugin extends ConnectorPlugin {

  private static final Log LOG                = ExoLogger.getLogger(CrowdinConnectorPlugin.class);

  private static final String           CONNECTOR_NAME = "crowdin";

  private static final String           CONNECTOR_REST_API = "https://api.crowdin.com/api/v2/user";

  private static final String           CONNECTOR_SCOPE    = "notification";

  private OAuth20Service                oAuthService;

  private long                          remoteConnectorId;

  private ConnectorSettingService connectorSettingService;

  public CrowdinConnectorPlugin(ConnectorSettingService connectorSettingsService) {
    this.connectorSettingService = connectorSettingsService;
  }

  @Override
  public String getConnectorName() {
    return CONNECTOR_NAME;
  }

  @Override
  public String validateToken(String code) throws OAuthException {
    LOG.warn("validateToken: code: " + code);
    RemoteConnectorSettings remoteConnectorSettings = connectorSettingService.getConnectorSettings(CONNECTOR_NAME);
    remoteConnectorSettings.setSecretKey(connectorSettingService.getConnectorSecretKey(CONNECTOR_NAME));
    if (StringUtils.isBlank(remoteConnectorSettings.getApiKey()) || StringUtils.isBlank(remoteConnectorSettings.getSecretKey())) {
      LOG.warn("Missing '{}' connector settings", CONNECTOR_NAME);
      return null;
    }

    if (StringUtils.isNotBlank(code)) {
      try {
        OAuth2AccessToken oAuth2AccessToken = getOAuthService(remoteConnectorSettings).getAccessToken(code);
        //CrowdinAccessTokenContext accessTokenContext = new CrowdinAccessTokenContext(oAuth2AccessToken);
        String crowdinIdentifier = fetchUsernameFromAccessToken(oAuth2AccessToken.getAccessToken());

        if (StringUtils.isBlank(crowdinIdentifier)) {
          throw new OAuthException(OAuthExceptionCode.INVALID_STATE, "User Crowdin identifier is empty");
        }
        return crowdinIdentifier;
      } catch (InterruptedException | IOException e) { // NOSONAR
        throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
      } catch (ExecutionException e) {
        throw new OAuthException(OAuthExceptionCode.UNKNOWN_ERROR, e);
      }
    } else {
      throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, "User denied scope on Crowdin authorization page");
    }
  }

  private static String fetchUsernameFromAccessToken(String accessToken) throws IOException {
    LOG.warn("fetchUsernameFromAccessToken: accessToken: " + accessToken);
    URL url = new URL(CONNECTOR_REST_API);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Authorization", "Bearer " + accessToken);
    connection.setRequestProperty("Accept", "application/json");
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
        return response.toString().split("\"username\":")[1].split(",")[0].replace("\"", "").trim();
      }
    } else {
      throw new IOException("Error retrieving user information from Crowdin. Response code: " + responseCode);
    }
  }

  public OAuth20Service getOAuthService(RemoteConnectorSettings remoteConnectorSettings) {
    if (oAuthService == null || remoteConnectorSettings.hashCode() != remoteConnectorId) {
      remoteConnectorId = remoteConnectorSettings.hashCode();
      oAuthService = new ServiceBuilder(remoteConnectorSettings.getApiKey()).apiSecret(remoteConnectorSettings.getSecretKey())
              .callback(remoteConnectorSettings.getRedirectUrl())
              .defaultScope(CONNECTOR_SCOPE)
              .build(CrowdinApi.instance());
    }
    return oAuthService;
  }
}
