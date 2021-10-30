/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Project {

    private final String name;

    private final Instant creationTime;

    private final Map<String, String> properties;

    private final DataObject rootObject;

    private final Map<Long, DataObject> objectsById;

    private Map<Long, List<DataObject>> backwardLinks;

    private Map<String, List<DataObject>> objectsByClass;

    public Project(String name, Instant creationTime, Map<String, String> properties, DataObject rootObject,
                   Map<Long, DataObject> objectsById) {
        this.name = Objects.requireNonNull(name);
        this.creationTime = Objects.requireNonNull(creationTime);
        this.properties = Objects.requireNonNull(properties);
        this.rootObject = Objects.requireNonNull(rootObject);
        this.objectsById = Objects.requireNonNull(objectsById);
        for (DataObject object : objectsById.values()) {
            object.setProject(this);
        }
    }

    public String getName() {
        return name;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public DataObject getRootObject() {
        return rootObject;
    }

    public Optional<DataObject> getObjectById(long id) {
        return Optional.ofNullable(objectsById.get(id));
    }

    private void indexBackwardLinks(DataObject dataObject) {
        for (DataAttribute attribute : dataObject.getDataClass().getAttributes()) {
            if (attribute.getType() == DataAttributeType.OBJECT) {
                dataObject.findObjectAttributeValue(attribute.getName())
                        .ifPresent(link -> backwardLinks.computeIfAbsent(link.getId(), k -> new ArrayList<>())
                                .add(dataObject));
            } else if (attribute.getType() == DataAttributeType.OBJECT_VECTOR) {
                dataObject.findObjectVectorAttributeValue(attribute.getName())
                        .ifPresent(links -> {
                            for (DataObject link : links) {
                                if (link != null) {
                                    backwardLinks.computeIfAbsent(link.getId(), k -> new ArrayList<>())
                                            .add(dataObject);
                                }
                            }
                        });
            }
        }
    }

    private void indexBackwardLinks() {
        if (backwardLinks == null) {
            backwardLinks = new LinkedHashMap<>();
            for (Map.Entry<Long, DataObject> e : objectsById.entrySet()) {
                DataObject dataObject = e.getValue();
                indexBackwardLinks(dataObject);
            }
        }
    }

    public List<DataObject> getBackwardLinks(long id) {
        indexBackwardLinks();
        return backwardLinks.getOrDefault(id, Collections.emptyList());
    }

    private void indexObjectsByClass() {
        if (objectsByClass == null) {
            objectsByClass = new HashMap<>();
            for (DataObject object : objectsById.values()) {
                objectsByClass.computeIfAbsent(object.getDataClassName(), k -> new ArrayList<>())
                        .add(object);
            }
        }
    }

    public List<DataObject> getObjectsByClass(String className) {
        Objects.requireNonNull(className);
        indexObjectsByClass();
        return objectsByClass.getOrDefault(className, Collections.emptyList());
    }

    public Map<String, Integer> getObjectCountByClass() {
        indexObjectsByClass();
        return objectsByClass.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size(), (v1, v2) -> v2, TreeMap::new));
    }

    public List<StudyCase> getStudyCases() {
        return getObjectsByClass("IntCase").stream().map(StudyCase::new).collect(Collectors.toList());
    }

    public Optional<StudyCase> findActiveStudyCase() {
        return rootObject.findObjectAttributeValue("pCase").map(StudyCase::new);
    }
}
