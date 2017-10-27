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
public class TransientSecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "transient";

    private static final double TRANSIENT_THRESHOLD = 1.0;

    private final double j;

    public static TransientSecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.END_ELEMENT:
                    if ("j".equals(xmlsr.getLocalName())) {
                        return new TransientSecurityIndex(contingencyId, Double.parseDouble(text));
                    }
                    break;
                default:
                    break;
            }
        }
        throw new AssertionError("j element not found");
    }

    public TransientSecurityIndex(String contingencyId, double j) {
        super(contingencyId, SecurityIndexType.TRANSIENT);
        this.j = j;
    }

    public double getJ() {
        return j;
    }

    @Override
    public boolean isOk() {
        return j < TRANSIENT_THRESHOLD;
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);
        xmlWriter.writeStartElement("j");
        xmlWriter.writeCharacters(Double.toString(j));
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("j", Double.toString(j));
    }

}
