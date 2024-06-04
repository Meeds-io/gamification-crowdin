/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.crowdin.gamification.storage;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.crowdin.gamification.exception.CrowdinConnectionException;
import io.meeds.crowdin.gamification.model.RemoteDirectory;
import io.meeds.crowdin.gamification.model.RemoteLanguage;
import io.meeds.crowdin.gamification.model.RemoteProject;
import io.meeds.crowdin.gamification.model.WebHook;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

import static io.meeds.crowdin.gamification.utils.Utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CrowdinConsumerStorage {

  public static final String TOKEN_EXPIRED_OR_INVALID = "crowdin.tokenExpiredOrInvalid";

  private HttpClient         client;

  private static final Log   LOG                      = ExoLogger.getLogger(CrowdinConsumerStorage.class);

  public List<RemoteProject> getProjects(String accessToken) throws IllegalAccessException {
    try {

      URI uri = URI.create(CROWDIN_API_URL + PROJECTS + "?hasManagerAccess=1");
      String response = processGet(uri, accessToken);
      JSONArray jsonArray = new JSONObject(response).getJSONArray("data");

      List<RemoteProject> projects = new ArrayList<>();

      // loop through the array and parse the projects
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObjectData = jsonArray.getJSONObject(i);
        JSONObject jsonObject = jsonObjectData.getJSONObject("data");
        // parse the project
        RemoteProject project = new RemoteProject();
        project.setId(jsonObject.getInt("id"));
        project.setName(jsonObject.getString("name"));
        project.setIdentifier(jsonObject.getString("identifier"));

        projects.add(project);
      }
      return projects;
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e);
    } catch (CrowdinConnectionException e) {
      throw new IllegalAccessException(TOKEN_EXPIRED_OR_INVALID);
    }
  }

  public WebHook createWebhook(long projectId, String[] triggers, String accessToken) throws IllegalAccessException {
    try {
      URI uri = URI.create(CROWDIN_API_URL + PROJECTS + projectId + "/webhooks");
      String secret = generateRandomSecret(8);
      JSONArray events = new JSONArray();
      Arrays.stream(triggers).forEach(events::put);

      JSONObject requestJson = new JSONObject();
      requestJson.put("name", "Meeds");
      requestJson.put("url", CommonsUtils.getCurrentDomain() + "/gamification-crowdin/rest/crowdin/webhooks");
      requestJson.put("events", events);
      requestJson.put("requestType", "POST");
      requestJson.put("isActive", true);
      requestJson.put("batchingEnabled", true);
      requestJson.put("contentType", "application/json");

      JSONObject headers = new JSONObject();
      headers.put("Authorization", "Bearer " + secret);
      requestJson.put("headers", headers);

      String response = processPost(uri, requestJson.toString(), accessToken);

      JSONObject responseJson = new JSONObject(response);

      JSONObject dataJson = responseJson.getJSONObject("data");

      WebHook localWebHook = new WebHook();
      localWebHook.setWebhookId(dataJson.getLong("id"));
      localWebHook.setProjectId(dataJson.getLong("projectId"));
      localWebHook.setTriggers(List.of(triggers));
      localWebHook.setToken(accessToken);
      localWebHook.setSecret(secret);
      return localWebHook;

    } catch (IllegalArgumentException e) {
      LOG.error(e);
      throw new IllegalArgumentException(e);
    } catch (CrowdinConnectionException e) {
      LOG.error(e);
      throw new IllegalAccessException(TOKEN_EXPIRED_OR_INVALID);
    }
  }

  private String processGet(URI uri, String accessToken) throws CrowdinConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpGet request = new HttpGet(uri);
    try {
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new CrowdinConnectionException(CROWDIN_CONNECTION_ERROR, e);
    }
  }

  private String processPost(URI uri, String jsonString, String accessToken) throws CrowdinConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpPost request = new HttpPost(uri);
    StringEntity entity = new StringEntity(jsonString, org.apache.http.entity.ContentType.APPLICATION_JSON);
    try {
      request.setHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      request.setEntity(entity);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new CrowdinConnectionException(CROWDIN_CONNECTION_ERROR, e);
    }
  }

  private String processDelete(URI uri, String accessToken) throws CrowdinConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpDelete request = new HttpDelete(uri);
    try {
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new CrowdinConnectionException(CROWDIN_CONNECTION_ERROR, e);
    }
  }

  private String processRequest(HttpClient httpClient, HttpRequestBase request) throws IOException, CrowdinConnectionException {
    HttpResponse response = httpClient.execute(request);
    boolean isSuccess = response != null
        && (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300);
    if (isSuccess) {
      return processSuccessResponse(response);
    } else if (response != null && response.getStatusLine().getStatusCode() == 404) {
      return null;
    } else {
      processErrorResponse(response);
      return null;
    }
  }

  private String processSuccessResponse(HttpResponse response) throws IOException {
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
      return String.valueOf(HttpStatus.SC_NO_CONTENT);
    } else if ((response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED
        || response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) && response.getEntity() != null
        && response.getEntity().getContentLength() != 0) {
      try (InputStream is = response.getEntity().getContent()) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    } else {
      return null;
    }
  }

  private void processErrorResponse(HttpResponse response) throws CrowdinConnectionException, IOException {
    if (response == null) {
      throw new CrowdinConnectionException("Error when connecting crowdin");
    } else if (response.getEntity() != null) {
      try (InputStream is = response.getEntity().getContent()) {
        String errorMessage = IOUtils.toString(is, StandardCharsets.UTF_8);
        if (StringUtils.contains(errorMessage, "")) {
          throw new CrowdinConnectionException(errorMessage);
        } else {
          throw new CrowdinConnectionException(CROWDIN_CONNECTION_ERROR + errorMessage);
        }
      }
    } else {
      throw new CrowdinConnectionException(CROWDIN_CONNECTION_ERROR + response.getStatusLine().getStatusCode());
    }
  }

  private HttpClient getHttpClient() {
    if (client == null) {
      HttpClientConnectionManager clientConnectionManager = getClientConnectionManager();
      HttpClientBuilder httpClientBuilder = HttpClients.custom()
                                                       .setConnectionManager(clientConnectionManager)
                                                       .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy());
      client = httpClientBuilder.build();
    }
    return client;
  }

  private HttpClientConnectionManager getClientConnectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(10);
    return connectionManager;
  }

  public RemoteProject retrieveRemoteProject(long projectRemoteId,
                                             boolean includeLanguages,
                                             String accessToken) throws IllegalAccessException {
    try {

      URI uri = URI.create(CROWDIN_API_URL + PROJECTS + projectRemoteId);
      String response = processGet(uri, accessToken);
      if (response == null) {
        return null;
      }
      return getRemoteProject(response, includeLanguages);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e);
    } catch (CrowdinConnectionException e) {
      throw new IllegalAccessException(TOKEN_EXPIRED_OR_INVALID);
    }
  }

  private static RemoteProject getRemoteProject(String response, boolean includeLanguages) {
    JSONObject responseJson = new JSONObject(response);
    JSONObject jsonObject = responseJson.getJSONObject("data");

    RemoteProject project = new RemoteProject();
    project.setId(jsonObject.getInt("id"));
    project.setName(jsonObject.getString("name"));
    project.setIdentifier(jsonObject.getString("identifier"));
    if (jsonObject.get("logo") instanceof String) {
      project.setAvatarUrl(jsonObject.getString("logo"));
    }

    if (includeLanguages) {
      JSONArray languages = jsonObject.getJSONArray("targetLanguages");
      List<RemoteLanguage> remoteLanguages = new ArrayList<>();
      for (int i = 0; i < languages.length(); i++) {
        JSONObject language = languages.getJSONObject(i);
        RemoteLanguage remoteLanguage = new RemoteLanguage();
        remoteLanguage.setId(language.getString("id"));
        remoteLanguage.setName(language.getString("name"));
        remoteLanguages.add(remoteLanguage);
      }
      project.setLanguages(remoteLanguages);
    }

    return project;
  }

  public String deleteWebhook(WebHook webHook) {
    URI uri = URI.create(CROWDIN_API_URL + PROJECTS + webHook.getProjectId() + WEBHOOKS + webHook.getWebhookId());
    try {
      return processDelete(uri, webHook.getToken());
    } catch (CrowdinConnectionException e) {
      throw new IllegalStateException("Unable to delete Crowdin hook");
    }
  }

  public List<RemoteDirectory> getProjectDirectories(long remoteProjectId,
                                                     int offset,
                                                     int limit,
                                                     String accessToken) throws IllegalAccessException {
    try {
      URI uri = URI.create(CROWDIN_API_URL + PROJECTS + remoteProjectId + "/directories?offset=" + offset + "&limit=" + limit);
      String response = processGet(uri, accessToken);
      JSONArray jsonArray = new JSONObject(response).getJSONArray("data");

      List<RemoteDirectory> directories = new ArrayList<>();

      // loop through the array and parse the projects
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObjectData = jsonArray.getJSONObject(i);
        JSONObject jsonObject = jsonObjectData.getJSONObject("data");
        // parse the project
        RemoteDirectory directory = new RemoteDirectory();
        directory.setId(jsonObject.getInt("id"));
        directory.setProjectId(jsonObject.getLong("projectId"));
        directory.setPath(jsonObject.getString("path"));

        directories.add(directory);
      }
      return directories;
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e);
    } catch (CrowdinConnectionException e) {
      throw new IllegalAccessException(TOKEN_EXPIRED_OR_INVALID);
    }
  }

  public void clearCache() {
    // implemented in cached storage
  }
}
