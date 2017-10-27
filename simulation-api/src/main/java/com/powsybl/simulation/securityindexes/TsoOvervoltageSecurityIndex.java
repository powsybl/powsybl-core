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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TsoOvervoltageSecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "tso-overvoltage";

    private final boolean computationSucceed;

    private final int overvoltageCount;

    public static TsoOvervoltageSecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        boolean computationSucceed = true;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case "computation-succeed":
                            computationSucceed = Boolean.parseBoolean(text);
                            break;

                        case "overvoltage-count":
                            return new TsoOvervoltageSecurityIndex(contingencyId, Integer.parseInt(text), computationSucceed);

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
        throw new AssertionError("overvoltage-count element not found");
    }

    public TsoOvervoltageSecurityIndex(String contingencyId, int overvoltageCount) {
        this(contingencyId, overvoltageCount, true);
    }

    public TsoOvervoltageSecurityIndex(String contingencyId, int overvoltageCount, boolean computationSucceed) {
        super(contingencyId, SecurityIndexType.TSO_OVERVOLTAGE);
        this.overvoltageCount = overvoltageCount;
        this.computationSucceed = computationSucceed;
    }

    public boolean isComputationSucceed() {
        return computationSucceed;
    }

    public int getOvervoltageCount() {
        return overvoltageCount;
    }

    @Override
    public boolean isOk() {
        return computationSucceed && overvoltageCount == 0;
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);

        xmlWriter.writeStartElement("computation-succeed");
        xmlWriter.writeCharacters(Boolean.toString(computationSucceed));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("overvoltage-count");
        xmlWriter.writeCharacters(Integer.toString(overvoltageCount));
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("computationSucceed", Boolean.toString(computationSucceed),
                               "overvoltageCount", Integer.toString(overvoltageCount));
    }
}
