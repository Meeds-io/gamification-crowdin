/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.meeds.gamification.crowdin.services.WebhookService;
import io.meeds.gamification.crowdin.storage.CrowdinConsumerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { HooksManagementRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class HooksManagementRestTest {

  private static final String    HOOKS_PATH    = "/crowdin/hooks"; // NOSONAR

  private static final String    SIMPLE_USER   = "simple";

  private static final String    ADMIN_USER    = "admin";

  private static final String    TEST_PASSWORD = "testPassword";

  @MockBean
  private WebhookService         webhookService;

  @MockBean
  private CrowdinConsumerStorage crowdinConsumerStorage;

  @Autowired
  private SecurityFilterChain    filterChain;

  @Autowired
  private WebApplicationContext  context;

  private MockMvc                mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filterChain.getFilters().toArray(new Filter[0])).build();
  }

  @Test
  void getWebhooksAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "?offset=0&limit=10&includeLanguages=false"));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWebhooksSimpleUser() throws Exception {
    ResultActions response =
                           mockMvc.perform(get(HOOKS_PATH + "?offset=0&limit=10&includeLanguages=false").with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWebhooksAdmin() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "?offset=0&limit=10&includeLanguages=false").with(testAdminUser()));
    response.andExpect(status().isOk());
  }

  @Test
  void getWebhookByIdAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/1"));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWebhookByIdSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/1").with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWebhookByIdAdmin() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/0").with(testAdminUser()));
    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(get(HOOKS_PATH + "/1").with(testAdminUser()));
    response.andExpect(status().isOk());
  }

  @Test
  void getProjectDirectoriesAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/4/directories").param("offset", "0").param("limit", "10"));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getProjectDirectoriesSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/4/directories").param("offset", "0")
                                                                               .param("limit", "10")
                                                                               .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getProjectDirectoriesAdmin() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/4/directories").param("offset", "0")
                                                                               .param("limit", "10")
                                                                               .with(testAdminUser()));
    response.andExpect(status().isOk());
  }

  @Test
  void getProjectsAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/projects").param("hookId", "hookId")
                                                                          .param("accessToken", "accessToken"));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getProjectsSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/projects").param("hookId", "hookId")
                                                                          .param("accessToken", "accessToken")
                                                                          .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getProjectsAdmin() throws Exception {
    ResultActions response = mockMvc.perform(get(HOOKS_PATH + "/projects").param("accessToken", "accessToken")
                                                                          .param("hookId", "1232")
                                                                          .with(testAdminUser()));
    response.andExpect(status().isOk());
  }

  @Test
  void createWebhookAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(post(HOOKS_PATH).param("projectId", "1")
                                                             .param("projectName", "projectName")
                                                             .param("accessToken", "accessToken")
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void createWebhookSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(post(HOOKS_PATH).param("projectId", "1")
                                                             .param("projectName", "projectName")
                                                             .param("accessToken", "accessToken")
                                                             .with(testSimpleUser())
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void createWebhookAdmin() throws Exception {
    ResultActions response = mockMvc.perform(post(HOOKS_PATH).param("projectName", "projectName")
                                                             .param("accessToken", "accessToken")
                                                             .with(testAdminUser())
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(post(HOOKS_PATH).param("projectId", "1")
                                               .param("accessToken", "accessToken")
                                               .with(testAdminUser())
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(post(HOOKS_PATH).param("projectId", "1")
                                               .param("projectName", "projectName")
                                               .with(testAdminUser())
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(post(HOOKS_PATH).param("projectId", "1")
                                               .param("projectName", "projectName")
                                               .param("accessToken", "accessToken")
                                               .with(testAdminUser())
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isCreated());
  }

  @Test
  void updateWebHookAccessTokenAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(patch(HOOKS_PATH).param("webHookId", "1")
                                                              .param("accessToken", "accessToken")
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void updateWebHookAccessTokenSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(patch(HOOKS_PATH).param("webHookId", "1")
                                                              .param("accessToken", "accessToken")
                                                              .with(testSimpleUser())
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void updateWebHookAccessTokenAdmin() throws Exception {
    ResultActions response = mockMvc.perform(patch(HOOKS_PATH).param("webHookId", "1")
                                                              .param("accessToken", "accessToken")
                                                              .with(testAdminUser())
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk());
  }

  @Test
  void deleteWebhookHookAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(delete(HOOKS_PATH + "/4")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void deleteWebhookHookSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(delete(HOOKS_PATH + "/4")
            .with(testSimpleUser())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void deleteWebhookHookTokenAdmin() throws Exception {
    ResultActions response = mockMvc.perform(delete(HOOKS_PATH + "/4")
            .with(testAdminUser())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk());
  }

  private RequestPostProcessor testAdminUser() {
    return user(ADMIN_USER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("rewarding"));
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("users"));
  }

}
