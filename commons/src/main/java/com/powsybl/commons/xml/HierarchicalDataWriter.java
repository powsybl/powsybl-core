/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface HierarchicalDataWriter {

    void writeStartElement(String ns, String name);

    void writeEmptyElement(String ns, String name);

    void writeEndElement();

    void writeElementContent(String value);

    void writeStringAttribute(String name, String value);

    void writeFloatAttribute(String name, float value);

    void writeDoubleAttribute(String name, double value);

    void writeDoubleAttribute(String name, double value, double absentValue);

    void writeIntAttribute(String name, int value);

    void writeIntAttribute(String name, int value, int absentValue);

    <E extends Enum<E>> void writeEnumAttribute(String name, E value);

    void writeBooleanAttribute(String name, boolean value);

    void writeBooleanAttribute(String name, boolean value, boolean absentValue);

    void close();
}
