package io.meeds.gamification.crowdin.rest;

import io.meeds.gamification.crowdin.model.RemoteProject;
import io.meeds.gamification.crowdin.model.WebHook;
import io.meeds.gamification.crowdin.rest.builder.WebHookBuilder;
import io.meeds.gamification.crowdin.rest.model.WebHookList;
import io.meeds.gamification.crowdin.rest.model.WebHookRestEntity;
import io.meeds.gamification.crowdin.services.CrowdinConsumerService;
import io.meeds.gamification.crowdin.services.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collection;
import java.util.List;

import static io.meeds.gamification.utils.Utils.getCurrentUser;

@Path("/gamification/connectors/crowdin/hooks")
public class HooksManagementRest implements ResourceContainer {

    public static final String         CROWDIN_HOOK_NOT_FOUND = "The Crowdin hook doesn't exit";
    private final WebhookService webhookService;
    private final CrowdinConsumerService crowdinConsumerService;

    public HooksManagementRest(WebhookService webhookService, CrowdinConsumerService crowdinConsumerService) {
        this.webhookService = webhookService;
        this.crowdinConsumerService = crowdinConsumerService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users")
    @Operation(summary = "Retrieves the list Crowdin webHooks", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request fulfilled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
    public Response getWebHooks(@QueryParam("offset") int offset,
                                @Parameter(description = "Query results limit", required = true) @QueryParam("limit") int limit,
                                @Parameter(description = "WebHook total size") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {

        String currentUser = getCurrentUser();
        List<WebHookRestEntity> webHookRestEntities;
        try {
            WebHookList webHookList = new WebHookList();
            webHookRestEntities = getWebHookRestEntities(currentUser);
            if (returnSize) {
                int webHookSize = webhookService.countWebhooks(currentUser, false);
                webHookList.setSize(webHookSize);
            }
            webHookList.setWebhooks(webHookRestEntities);
            webHookList.setOffset(offset);
            webHookList.setLimit(limit);
            return Response.ok(webHookList).build();
        } catch (IllegalAccessException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get-projects")
    @RolesAllowed("users")
    @Operation(summary = "Retrieves a webHook by its technical identifier", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request fulfilled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
            @ApiResponse(responseCode = "400", description = "Invalid query input"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error"), })
    public Response getProjects(@Parameter(description = "WebHook technical identifier", required = true) @QueryParam("accessToken") String accessToken) {
        if (accessToken.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Access Token must be not empty").build();
        }

        try {
            List<RemoteProject> remoteProjectList = webhookService.getProjects(accessToken);
            return Response.ok(remoteProjectList).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalAccessException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("users")
    @Operation(summary = "Create a project webhook for Remote Crowdin connector.", description = "Create a project webhook for Remote Crowdin connector.", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request fulfilled"),
            @ApiResponse(responseCode = "400", description = "Invalid query input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response createWebhookHook(@Parameter(description = "Crowdin project id", required = true) @FormParam("projectId") Long projectId,
                                      @Parameter(description = "Crowdin project name", required = true) @FormParam("projectName") String projectName,
                                      @Parameter(description = "Crowdin project logo", required = true) @FormParam("projectLogo") String projectLogo,
                                      @Parameter(description = "Crowdin personal access token", required = true) @FormParam("accessToken") String accessToken) {

        if (projectId == null || StringUtils.isBlank(projectName)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'projectId' and 'projectName' parameter are mandatory").build();
        }
        if (StringUtils.isBlank(accessToken)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'accessToken' parameter is mandatory").build();
        }
        String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
        try {
            webhookService.createWebhook(projectId, projectName, accessToken, currentUser);
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalAccessException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (ObjectAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (ObjectNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("{organizationId}")
    @RolesAllowed("users")
    @Operation(summary = "Deletes gitHub organization webhook", description = "Deletes gitHub organization webhook", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Request fulfilled"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error"), })
    public Response deleteWebhookHook(@Parameter(description = "GitHub organization id", required = true) @PathParam("organizationId") long organizationId) {
        if (organizationId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'hookName' parameter is mandatory").build();
        }
        String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
        try {
            webhookService.deleteWebhookHook(organizationId, currentUser);
            return Response.noContent().build();
        } catch (IllegalAccessException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        } catch (ObjectNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(CROWDIN_HOOK_NOT_FOUND).build();
        }
    }

    private List<WebHookRestEntity> getWebHookRestEntities(String username) throws IllegalAccessException {
        Collection<WebHook> webHooks = webhookService.getWebhooks(username, 0, 20, false);
        return WebHookBuilder.toRestEntities(webhookService, crowdinConsumerService, webHooks);
    }
}
