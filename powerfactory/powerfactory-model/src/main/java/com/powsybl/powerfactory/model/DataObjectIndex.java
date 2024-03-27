/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DataObjectIndex {

    private final Map<Long, DataObject> objectsById = new HashMap<>();

    private final Map<String, List<DataObject>> objectsByClass = new HashMap<>();

    private Map<Long, List<DataObject>> backwardLinks;

    public void addDataObject(DataObject obj) {
        Objects.requireNonNull(obj);
        if (objectsById.containsKey(obj.getId())) {
            throw new PowerFactoryException("Object '" + obj.getId() + "' already exists");
        }
        objectsById.put(obj.getId(), obj);
        objectsByClass.computeIfAbsent(obj.getDataClassName(), k -> new ArrayList<>())
                .add(obj);
        if (backwardLinks != null) {
            indexBackwardLinks(obj);
        }
    }

    public Collection<DataObject> getDataObjects() {
        return objectsById.values();
    }

    public List<DataObject> getRootDataObjects() {
        return objectsById.values().stream().filter(obj -> obj.getParent() == null).collect(Collectors.toList());
    }

    public Optional<DataObject> getDataObjectById(long id) {
        return Optional.ofNullable(objectsById.get(id));
    }

    public List<DataObject> getDataObjectsByClass(String className) {
        Objects.requireNonNull(className);
        return objectsByClass.getOrDefault(className, Collections.emptyList());
    }

    private void indexBackwardLinks(DataObject obj) {
        for (DataAttribute attribute : obj.getDataClass().getAttributes()) {
            if (attribute.getType() == DataAttributeType.OBJECT) {
                obj.findObjectAttributeValue(attribute.getName())
                        .flatMap(DataObjectRef::resolve)
                        .ifPresent(link -> backwardLinks.computeIfAbsent(link.getId(), k -> new ArrayList<>())
                                .add(obj));
            } else if (attribute.getType() == DataAttributeType.OBJECT_VECTOR) {
                obj.findObjectVectorAttributeValue(attribute.getName())
                        .ifPresent(refs -> {
                            for (DataObjectRef ref : refs) {
                                ref.resolve().ifPresent(object ->
                                        backwardLinks.computeIfAbsent(ref.getId(), k -> new ArrayList<>())
                                                .add(obj));
                            }
                        });
            }
        }
    }

    private void indexBackwardLinks() {
        if (backwardLinks == null) {
            backwardLinks = new LinkedHashMap<>();
            for (DataObject obj : objectsById.values()) {
                indexBackwardLinks(obj);
            }
        }
    }

    public List<DataObject> getBackwardLinks(long id) {
        indexBackwardLinks();
        return backwardLinks.getOrDefault(id, Collections.emptyList());
    }
}
