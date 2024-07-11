/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
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
package io.meeds.crowdin.gamification.rest;

import io.meeds.crowdin.gamification.model.RemoteDirectory;
import io.meeds.crowdin.gamification.model.RemoteProject;
import io.meeds.crowdin.gamification.model.WebHook;
import io.meeds.crowdin.gamification.rest.builder.WebHookBuilder;
import io.meeds.crowdin.gamification.rest.model.WebHookList;
import io.meeds.crowdin.gamification.rest.model.WebHookRestEntity;
import io.meeds.crowdin.gamification.services.WebhookService;
import io.meeds.crowdin.gamification.storage.CrowdinConsumerStorage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

import static io.meeds.gamification.utils.Utils.getCurrentUser;

@RestController
@RequestMapping("crowdin/hooks")
@Tag(name = "hooks", description = "An endpoint to manage crowdin webhooks")
public class HooksManagementRest {

  public static final String     CROWDIN_HOOK_NOT_FOUND = "The Crowdin hook doesn't exit";

  @Autowired
  private WebhookService         webhookService;

  @Autowired
  private CrowdinConsumerStorage crowdinConsumerStorage;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Retrieves the list Crowdin webHooks", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public WebHookList getWebHooks(@Parameter(description = "Offset") @RequestParam("offset") int offset,
                                 @Parameter(description = "Query results limit", required = true) @RequestParam("limit") int limit,
                                 @Parameter(description = "force update remote project info") @RequestParam(value = "forceUpdate", defaultValue = "false", required = false) boolean forceUpdate,
                                 @Parameter(description = "Include languages") @Schema(defaultValue = "false") @RequestParam("includeLanguages") boolean includeLanguages) {

    List<WebHookRestEntity> webHookRestEntities;
    WebHookList webHookList = new WebHookList();
    webHookRestEntities = getWebHookRestEntities(includeLanguages, forceUpdate);
    webHookList.setWebhooks(webHookRestEntities);
    webHookList.setOffset(offset);
    webHookList.setLimit(limit);
    return webHookList;
  }

  @GetMapping("{webHookId}")
  @Secured("rewarding")
  @Operation(summary = "Retrieves a webHook by its technical identifier", method = "GET")
  @ApiResponse(responseCode = "200", description = "Request fulfilled")
  @ApiResponse(responseCode = "404", description = "Not found")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "503", description = "Service unavailable")
  public WebHook getWebHookById(@Parameter(description = "WebHook technical identifier", required = true) @PathVariable("webHookId") long webHookId) {
    if (webHookId == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WebHook Id must be not null");
    }
    String currentUser = getCurrentUser();
    try {
      return webhookService.getWebhookId(webHookId, currentUser);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @GetMapping("{projectId}/directories")
  @Secured("users")
  @Operation(summary = "Retrieves a list of project directories from crowdin", method = "GET")
  @ApiResponse(responseCode = "200", description = "Request fulfilled")
  @ApiResponse(responseCode = "404", description = "Not found")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "503", description = "Service unavailable")
  public List<RemoteDirectory> getProjectDirectories(HttpServletRequest request,
                                                     @Parameter(description = "Remote project identifier", required = true) @PathVariable("projectId") long projectId,
                                                     @Parameter(description = "Query Offset") @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                                                     @Parameter(description = "Query results limit") @RequestParam(value = "limit", required = false, defaultValue = "0") int limit) {
    try {
      return webhookService.getProjectDirectories(projectId, request.getRemoteUser(), offset, limit);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @GetMapping("projects")
  @Secured("rewarding")
  @Operation(summary = "Retrieves a list of projects from crowdin", method = "GET")
  @ApiResponse(responseCode = "200", description = "Request fulfilled")
  @ApiResponse(responseCode = "404", description = "Not found")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "503", description = "Service unavailable")
  public List<RemoteProject> getProjects(@Parameter(description = "Crowdin access token") @RequestParam("accessToken") String accessToken,
                                         @Parameter(description = "WebHook technical identifier") @RequestParam("hookId") String webHookId) {
    try {
      if (webHookId != null && !webHookId.isEmpty()) {
        return webhookService.getProjectsFromWebhookId(Long.parseLong(webHookId));
      } else {
        return webhookService.getProjects(accessToken);
      }
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @PostMapping
  @Secured("rewarding")
  @Operation(summary = "Create a project webhook for Remote Crowdin connector.", description = "Create a project webhook for Remote Crowdin connector.", method = "POST")
  @ApiResponse(responseCode = "201", description = "Created")
  @ApiResponse(responseCode = "404", description = "Not found")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "409", description = "Conflict")
  @ApiResponse(responseCode = "503", description = "Service unavailable")
  public ResponseEntity<Object> createWebhookHook(HttpServletRequest request,
                                                  @Parameter(description = "Crowdin project id", required = true) @RequestParam("projectId") Long projectId,
                                                  @Parameter(description = "Crowdin project name", required = true) @RequestParam("projectName") String projectName,
                                                  @Parameter(description = "Crowdin personal access token", required = true) @RequestParam("accessToken") String accessToken) {

    if (projectId == null || StringUtils.isBlank(projectName)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'projectId' and 'projectName' parameter are mandatory");
    }
    if (StringUtils.isBlank(accessToken)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'accessToken' parameter is mandatory");
    }
    try {
      webhookService.createWebhook(projectId, projectName, accessToken, request.getRemoteUser());
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectAlreadyExistsException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

  @PatchMapping
  @Secured("rewarding")
  @Operation(summary = "Update a project webhook personal access token.", description = "Update a project webhook personal access token.", method = "PATCH")
  @ApiResponse(responseCode = "201", description = "Updated")
  @ApiResponse(responseCode = "404", description = "Not found")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "409", description = "Conflict")
  @ApiResponse(responseCode = "503", description = "Service unavailable")
  public Response updateWebHookAccessToken(HttpServletRequest request,
                                           @Parameter(description = "webHook id", required = true) @RequestParam("webHookId") long webHookId,
                                           @Parameter(description = "Crowdin personal access token", required = true) @RequestParam("accessToken") String accessToken) {

    if (webHookId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'webHookId' must be positive");
    }
    if (StringUtils.isBlank(accessToken)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'accessToken' parameter is mandatory");
    }
    try {
      webhookService.updateWebHookAccessToken(webHookId, accessToken, request.getRemoteUser());
      return Response.status(Response.Status.CREATED).build();
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, CROWDIN_HOOK_NOT_FOUND);
    }
  }

  @DeleteMapping("{projectId}")
  @Secured("rewarding")
  @Operation(summary = "Deletes crowdin project webhook", description = "Deletes crowdin project webhook", method = "DELETE")
  @ApiResponse(responseCode = "200", description = "Request fulfilled")
  @ApiResponse(responseCode = "404", description = "Not found")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "503", description = "Service unavailable")
  public void deleteWebhookHook(HttpServletRequest request,
                                @Parameter(description = "Crowdin project id", required = true) @PathVariable("projectId") long projectId) {
    if (projectId <= 0) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "'hookName' parameter is mandatory");
    }
    try {
      webhookService.deleteWebhook(projectId, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, CROWDIN_HOOK_NOT_FOUND);
    }
  }

  private List<WebHookRestEntity> getWebHookRestEntities(boolean includeLanguages,
                                                         boolean forceUpdate) {
    Collection<WebHook> webHooks = webhookService.getWebhooks(0, 20, forceUpdate);
    return WebHookBuilder.toRestEntities(crowdinConsumerStorage, webHooks, includeLanguages);
  }

}
