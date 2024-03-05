package io.meeds.gamification.crowdin.rest;

import io.meeds.gamification.crowdin.services.CrowdinTriggerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/crowdin/webhooks")
@Tag(name = "webhooks", description = "An endpoint to receive crowdin webhooks")
public class CrowdinWebhookController {

    private static final Log LOG                = ExoLogger.getLogger(CrowdinWebhookController.class);

    @Autowired
    private CrowdinTriggerService crowdinTriggerService;
    @PostMapping
    @Operation(summary = "Project webhook for Remote Crowdin connector.", description = "Project webhook for Remote Crowdin connector.", method = "POST")
    @ApiResponse(responseCode = "200", description = "Ok")
    @ApiResponse(responseCode = "509", description = "Internal Server Error")
    public ResponseEntity<Object> crowdinEvent(
                                @RequestHeader("authorization") String bearerToken,
                                @RequestBody String payload) {
        try {
            LOG.info("bearerToken: " + bearerToken);
            LOG.info("payload: " + payload);
            crowdinTriggerService.handleTriggerAsync(bearerToken, payload);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
