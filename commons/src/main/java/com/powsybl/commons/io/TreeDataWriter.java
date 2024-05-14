/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io;

import java.util.Collection;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TreeDataWriter extends AutoCloseable {

    void writeStartNodes();

    void writeEndNodes();

    void writeStartNode(String namespace, String name);

    void writeEndNode();

    void writeNamespace(String prefix, String namespace);

    void writeNodeContent(String value);

    void writeStringAttribute(String name, String value);

    void writeFloatAttribute(String name, float value);

    void writeDoubleAttribute(String name, double value);

    void writeDoubleAttribute(String name, double value, double absentValue);

    void writeOptionalDoubleAttribute(String name, Double value);

    void writeIntAttribute(String name, int value);

    void writeIntAttribute(String name, int value, int absentValue);

    void writeOptionalIntAttribute(String name, Integer value);

    void writeIntArrayAttribute(String name, Collection<Integer> values);

    void writeStringArrayAttribute(String name, Collection<String> values);

    <E extends Enum<E>> void writeEnumAttribute(String name, E value);

    void writeBooleanAttribute(String name, boolean value);

    void writeBooleanAttribute(String name, boolean value, boolean absentValue);

    void writeOptionalBooleanAttribute(String name, Boolean value);

    @Override
    void close();

    void setVersions(Map<String, String> extensionVersions);
}
