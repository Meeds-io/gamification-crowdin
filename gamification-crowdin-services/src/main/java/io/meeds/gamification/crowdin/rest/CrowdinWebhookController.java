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
package io.meeds.gamification.crowdin.rest;

import io.meeds.gamification.crowdin.services.CrowdinTriggerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/crowdin/webhooks")
@Tag(name = "webhooks", description = "An endpoint to receive crowdin webhooks")
public class CrowdinWebhookController {

  @Autowired
  private CrowdinTriggerService crowdinTriggerService;

  @PostMapping
  @Operation(summary = "Project webhook for Remote Crowdin connector.", description = "Project webhook for Remote Crowdin connector.", method = "POST")
  @ApiResponse(responseCode = "200", description = "Ok")
  @ApiResponse(responseCode = "509", description = "Internal Server Error")
  public ResponseEntity<Object> crowdinEvent(@RequestHeader("authorization") String bearerToken, @RequestBody String payload) {
    try {
      crowdinTriggerService.handleTriggerAsync(bearerToken, payload);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
