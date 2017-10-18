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
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TsoSynchroLossSecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "tso-synchro-loss";

    private static final String GENERATOR = "generator";

    private final int synchroLossCount;

    private final Map<String, Float> desynchronizedGenerators;

    public static TsoSynchroLossSecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        int synchroLossCount = -1;
        Map<String, Float> desynchronizedGenerators = new HashMap<>();
        String id = null;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.START_ELEMENT:
                    if (GENERATOR.equals(xmlsr.getLocalName())) {
                        id = xmlsr.getAttributeValue(null, "id");
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case GENERATOR:
                            if (id == null) {
                                throw new AssertionError("The required attribute 'id' is missing");
                            }
                            float p = Float.parseFloat(text);
                            desynchronizedGenerators.put(id, p);
                            id = null;
                            break;
                        case "synchro-loss-count":
                            synchroLossCount = Integer.parseInt(text);
                            break;
                        case "index":
                            return new TsoSynchroLossSecurityIndex(contingencyId, synchroLossCount, desynchronizedGenerators);
                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
        throw new AssertionError("index element not found");
    }

    public TsoSynchroLossSecurityIndex(String contingencyId, int synchroLossCount, Map<String, Float> desynchronizedGenerators) {
        super(contingencyId, SecurityIndexType.TSO_SYNCHROLOSS);
        this.synchroLossCount = synchroLossCount;
        this.desynchronizedGenerators = Objects.requireNonNull(desynchronizedGenerators);
    }

    public int getSynchroLossCount() {
        return synchroLossCount;
    }

    public Map<String, Float> getDesynchronizedGenerators() {
        return desynchronizedGenerators;
    }

    @Override
    public boolean isOk() {
        return synchroLossCount == 0;
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);

        xmlWriter.writeStartElement("synchro-loss-count");
        xmlWriter.writeCharacters(Integer.toString(synchroLossCount));
        xmlWriter.writeEndElement();

        for (Map.Entry<String, Float> e : desynchronizedGenerators.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement(GENERATOR);
            xmlWriter.writeAttribute("id", id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("synchroLossCount", Integer.toString(synchroLossCount),
                               "desynchronizedGenerators", desynchronizedGenerators.toString());
    }

}
