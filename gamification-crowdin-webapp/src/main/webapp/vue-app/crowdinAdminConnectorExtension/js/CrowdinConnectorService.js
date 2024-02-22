/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

export function getProjects(accessToken) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks/get-projects?accessToken=${accessToken}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else if (resp.status === 404 || resp.status === 401) {
      return resp.text().then((text) => {
        console.log(text);
        throw new Error(text);
      });
    } else {
      throw new Error('Error when getting crowdin projects');
    }
  });
}

export function getCrowdinWebHooks(offset, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks?offset=${offset || 0}&limit=${limit|| 10}&returnSize=true`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting crowdin webhooks');
    }
  });
}

export function getCrowdinWebHookById(hookId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks/${hookId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting crowdin webhook');
    }
  });
}

export function saveCrowdinWebHook(project, accessToken) {
  const formData = new FormData();
  formData.append('projectId', project.id);
  formData.append('projectName', project.name);
  formData.append('projectLogo', project.logo);
  formData.append('accessToken', accessToken);
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      if (resp.status === 404 || resp.status === 401) {
        return resp.text().then((text) => {
          throw new Error(text);
        });
      } else {
        throw new Error('Error when saving crowdin webhook');
      }
    }
  });
}

export function updateWebHookAccessToken(webHookId, accessToken) {
  const formData = new FormData();
  formData.append('webHookId', webHookId);
  formData.append('accessToken', accessToken);
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when saving crowdin webhook');
    }
  });
}

export function deleteCrowdinWebHook(organizationId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks/${organizationId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when deleting crowdin webhook');
    }
  });
}

export function getWebHookRepos(organizationId, page, perPage, keyword) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks/${organizationId}/repos?page=${page || 0}&perPage=${perPage|| 10}&keyword=${keyword || ''}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting crowdin webhooks');
    }
  });
}

export function saveRepositoryStatus(repositoryId, organizationId, enabled) {
  const formData = new FormData();
  formData.append('repositoryId', repositoryId);
  formData.append('organizationId', organizationId);
  formData.append('enabled', enabled);

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks/repo/status`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function enableDisableWatchScope(organizationId, enabled) {
  const formData = new FormData();
  formData.append('organizationId', organizationId);
  formData.append('enabled', enabled);
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks/watchScope/status`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function forceUpdateWebhooks() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/crowdin/hooks/forceUpdate`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when updating crowdin webhooks');
    }
  });
}