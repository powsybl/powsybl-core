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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TsoDisconnectedGenerator extends AbstractSecurityIndex {

    static final String XML_NAME = "tso-disconnected-generator";

    private static final float GENERATION_LOST_THRESHOLD = 0;

    private static final String ELEM_GENERATOR = "generator";

    private final Map<String, Float> disconnectedGenerators;

    public static TsoDisconnectedGenerator fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        Map<String, Float> disconnectedGenerators = new HashMap<>();
        String id = null;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;

                case XMLEvent.START_ELEMENT:
                    if (Objects.equals(ELEM_GENERATOR, xmlsr.getLocalName())) {
                        id = xmlsr.getAttributeValue(null, "id");
                    }
                    break;

                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case ELEM_GENERATOR:
                            if (id == null) {
                                throw new AssertionError();
                            }
                            float p = Float.parseFloat(text);
                            disconnectedGenerators.put(id, p);
                            id = null;
                            break;
                        case "index":
                            return new TsoDisconnectedGenerator(contingencyId, disconnectedGenerators);
                        default:
                            throw new AssertionError("Unexpected element: " + xmlsr.getLocalName());
                    }
                    break;

                default:
                    break;
            }
        }
        throw new AssertionError("Should not happen");
    }

    public TsoDisconnectedGenerator(String contingencyId, Map<String, Float> disconnectedGenerators) {
        super(contingencyId, SecurityIndexType.TSO_DISCONNECTED_GENERATOR);
        this.disconnectedGenerators = disconnectedGenerators;
    }

    public Map<String, Float> getDisconnectedGenerators() {
        return disconnectedGenerators;
    }

    @Override
    public boolean isOk() {
        return disconnectedGenerators.entrySet().stream().mapToDouble(Map.Entry::getValue).sum() <= GENERATION_LOST_THRESHOLD;
    }

    @Override
    protected void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);
        for (Map.Entry<String, Float> e : disconnectedGenerators.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement(ELEM_GENERATOR);
            xmlWriter.writeAttribute("id", id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("disconnectedGenerators", disconnectedGenerators.toString());
    }
}
