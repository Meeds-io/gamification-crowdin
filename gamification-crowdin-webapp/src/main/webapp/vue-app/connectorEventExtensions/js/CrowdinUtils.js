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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

export function buildTree(directories) {
  const tree = [];
  const lookup = {};
  directories?.forEach((dir) => {
    const parts = dir.path.split('/').filter(Boolean);
    let currentLevel = tree;
    let fullPath = '';
    parts.forEach((part) => {
      fullPath = fullPath ? `${fullPath}/${part}` : part;
      if (!lookup[fullPath]) {
        const node = {
          id: dir.id,
          path: `/${fullPath}`,
          name: part,
          children: [],
        };
        lookup[fullPath] = node;
        currentLevel.push(node);
      }
      currentLevel = lookup[fullPath].children;
    });
  });
  return tree;
}

export function processItems(items, idList) {
  const idSet = new Set(idList);
  const result = [];
  this.buildTree(items).filter(item => (idSet.has(item.id) || item.children.length > 0)).forEach(item => {
    result.push(...this.processItem(item, idSet));
  });
  return result;
}

export function hasAllChildrenListed(item, idSet) {
  if (item.children && item.children.length > 0) {
    return item.children.every(child => idSet.has(child.id));
  }
  return true;
}

export function processItem(item, idSet) {
  if (this.hasAllChildrenListed(item, idSet)) {
    return [{ id: item.id, path: item.path }];
  } else {
    const results = [];
    item.children.filter(item => (idSet.has(item.id) || item.children.length > 0)).forEach(child => {
      if (this.hasAllChildrenListed(child, idSet)) {
        results.push({ id: child.id, path: child.path });
      } else {
        results.push(...this.processItem(child, idSet));
      }
    });
    return results;
  }
}