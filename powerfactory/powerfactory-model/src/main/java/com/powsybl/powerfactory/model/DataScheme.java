/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DataScheme {

    private final Map<String, DataClass> classesByName = new HashMap<>();

    public static DataScheme build(DataObject root) {
        return build(List.of(root));
    }

    public static DataScheme build(List<DataObject> roots) {
        Objects.requireNonNull(roots);
        DataScheme scheme = new DataScheme();
        roots.forEach(root -> root.traverse(object -> {
            DataClass clazz = object.getDataClass();
            if (!scheme.classExists(clazz.getName())) {
                scheme.addClass(clazz);
            }
        }));
        return scheme;
    }

    public void addClass(DataClass clazz) {
        Objects.requireNonNull(clazz);
        if (classesByName.containsKey(clazz.getName())) {
            throw new PowerFactoryException("Class '" + clazz.getName() + "' already exists");
        }
        classesByName.put(clazz.getName(), clazz);
    }

    public Optional<DataClass> findClassByName(String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(classesByName.get(name));
    }

    public DataClass getClassByName(String name) {
        return findClassByName(name).orElseThrow(() -> new PowerFactoryException("Class '" + name + "' not found"));
    }

    public boolean classExists(String name) {
        return findClassByName(name).isPresent();
    }

    static DataScheme parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        DataScheme scheme = new DataScheme();
        JsonUtil.parseObjectArray(parser, scheme::addClass, DataClass::parseJson);
        return scheme;
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeFieldName("classes");
        generator.writeStartArray();
        for (DataClass clazz : classesByName.values()) {
            clazz.writeJson(generator);
        }
        generator.writeEndArray();
    }
}
