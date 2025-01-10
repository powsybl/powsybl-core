/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io;

import java.util.Map;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractTreeDataReader implements TreeDataReader {

    @Override
    public TreeDataHeader readHeader() {
        return new TreeDataHeader(readRootVersion(), readExtensionVersions());
    }

    protected abstract String readRootVersion();

    protected abstract Map<String, String> readExtensionVersions();

    @Override
    public double readDoubleAttribute(String name) {
        return readDoubleAttribute(name, Double.NaN);
    }

    @Override
    public float readFloatAttribute(String name) {
        return readFloatAttribute(name, Float.NaN);
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz) {
        return readEnumAttribute(name, clazz, null);
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        String attributeValue = readStringAttribute(name);
        return attributeValue != null ? Enum.valueOf(clazz, attributeValue) : defaultValue;
    }

    @Override
    public void skipChildNodes() {
        skipSimpleValueAttributes();
        readChildNodes(elementName -> skipChildNodes());
    }

    /**
     * Skip the attributes with scalar values
     */
    protected abstract void skipSimpleValueAttributes();
}
