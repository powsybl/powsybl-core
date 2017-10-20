/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.google.common.collect.ImmutableMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class UnderOverVoltageSecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "underovervoltage";

    private static final double INDEX_THRESHOLD = 1.0;
    private final double indexValue;

    public static UnderOverVoltageSecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.END_ELEMENT:
                    if ("vx".equals(xmlsr.getLocalName())) {
                        return new UnderOverVoltageSecurityIndex(contingencyId, Double.parseDouble(text));
                    }
                    break;
                default:
                    break;
            }
        }
        throw new AssertionError("vx element not found");
    }

    public UnderOverVoltageSecurityIndex(String contingencyId, double indexValue) {
        super(contingencyId, SecurityIndexType.OVERUNDERVOLTAGE);
        this.indexValue = indexValue;
    }

    public double getIndexValue() {
        return indexValue;
    }

    @Override
    public boolean isOk() {
        return indexValue <= INDEX_THRESHOLD;
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);
        xmlWriter.writeStartElement("vx");
        xmlWriter.writeCharacters(Double.toString(indexValue));
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("vx", Double.toString(indexValue));
    }
}
