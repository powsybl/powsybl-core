/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import javax.xml.stream.XMLStreamReader;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlReader {

    private XMLStreamReader reader;

    public XmlReader(XMLStreamReader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    public double readDoubleAttribute(String name) {
        return XmlUtil.readDoubleAttribute(reader, name);
    }

    public double readDoubleAttribute(String name, double defaultValue) {
        return XmlUtil.readOptionalDoubleAttribute(reader, name, defaultValue);
    }

    public String readStringAttribute(String name) {
        return reader.getAttributeValue(null, name);
    }

    public int readIntAttribute(String name, int defaultValue) {
        return XmlUtil.readOptionalIntegerAttribute(reader, name, defaultValue);
    }

    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        return XmlUtil.readOptionalBoolAttribute(reader, name, defaultValue);
    }

    public String getElementName() {
        return reader.getLocalName();
    }
}
