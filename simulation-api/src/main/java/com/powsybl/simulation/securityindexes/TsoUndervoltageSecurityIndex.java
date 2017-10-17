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
public class TsoUndervoltageSecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "tso-undervoltage";

    private final boolean computationSucceed;

    private final int undervoltageCount;

    public static TsoUndervoltageSecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
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

                        case "undervoltage-count":
                            return new TsoUndervoltageSecurityIndex(contingencyId, Integer.parseInt(text), computationSucceed);

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
        throw new AssertionError("undervoltage-count element not found");
    }

    public TsoUndervoltageSecurityIndex(String contingencyId, int undervoltageCount) {
        this(contingencyId, undervoltageCount, true);
    }

    public TsoUndervoltageSecurityIndex(String contingencyId, int undervoltageCount, boolean computationSucceed) {
        super(contingencyId, SecurityIndexType.TSO_UNDERVOLTAGE);
        this.undervoltageCount = undervoltageCount;
        this.computationSucceed = computationSucceed;
    }

    public boolean isComputationSucceed() {
        return computationSucceed;
    }

    public int getUndervoltageCount() {
        return undervoltageCount;
    }

    @Override
    public boolean isOk() {
        return computationSucceed && undervoltageCount == 0;
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);

        xmlWriter.writeStartElement("computation-succeed");
        xmlWriter.writeCharacters(Boolean.toString(computationSucceed));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("undervoltage-count");
        xmlWriter.writeCharacters(Integer.toString(undervoltageCount));
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("computationSucceed", Boolean.toString(computationSucceed),
                               "undervoltageCount", Integer.toString(undervoltageCount));
    }

}
