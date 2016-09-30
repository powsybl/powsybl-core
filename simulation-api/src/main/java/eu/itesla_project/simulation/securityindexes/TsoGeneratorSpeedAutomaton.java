/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.simulation.securityindexes;

import com.google.common.collect.ImmutableMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TsoGeneratorSpeedAutomaton extends AbstractSecurityIndex {

    static final String XML_NAME = "tso-generator-speed-automaton";

    private final List<String> onUnderSpeedDiconnectedGenerators;
    private final List<String> onOverSpeedDiconnectedGenerators;

    public static TsoGeneratorSpeedAutomaton fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        boolean under = false;
        boolean over = false;
        String text = null;
        List<String> onUnderSpeedDiconnectedGenerators = new ArrayList<>();
        List<String> onOverSpeedDiconnectedGenerators = new ArrayList<>();
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.START_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case "onUnderSpeedDisconnectedGenerators":
                            under = true;
                            break;
                        case "onOverSpeedDisconnectedGenerators":
                            over = true;
                            break;
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case "onUnderSpeedDisconnectedGenerators":
                            under = false;
                            break;
                        case "onOverSpeedDisconnectedGenerators":
                            over = false;
                            break;
                        case "gen":
                            if (under) {
                                onUnderSpeedDiconnectedGenerators.add(text);
                            } else if (over) {
                                onOverSpeedDiconnectedGenerators.add(text);
                            } else {
                                throw new AssertionError();
                            }
                            break;
                        case "index":
                            return new TsoGeneratorSpeedAutomaton(contingencyId, onUnderSpeedDiconnectedGenerators, onOverSpeedDiconnectedGenerators);
                    }
                    break;
            }
        }
        throw new AssertionError("Should not happened");
    }

    public TsoGeneratorSpeedAutomaton(String contingencyId, List<String> onUnderSpeedDiconnectedGenerators, List<String> onOverSpeedDiconnectedGenerators) {
        super(contingencyId, SecurityIndexType.TSO_GENERATOR_SPEED_AUTOMATON);
        this.onUnderSpeedDiconnectedGenerators = onUnderSpeedDiconnectedGenerators;
        this.onOverSpeedDiconnectedGenerators = onOverSpeedDiconnectedGenerators;
    }

    public List<String> getOnOverSpeedDiconnectedGenerators() {
        return onOverSpeedDiconnectedGenerators;
    }

    public List<String> getOnUnderSpeedDiconnectedGenerators() {
        return onUnderSpeedDiconnectedGenerators;
    }

    @Override
    public boolean isOk() {
        return (onUnderSpeedDiconnectedGenerators.size() + onOverSpeedDiconnectedGenerators.size()) == 0;
    }

    @Override
    protected void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);
        xmlWriter.writeStartElement("onUnderSpeedDisconnectedGenerators");
        for (String gen : onUnderSpeedDiconnectedGenerators) {
            xmlWriter.writeStartElement("gen");
            xmlWriter.writeCharacters(gen);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement("onOverSpeedDisconnectedGenerators");
        for (String gen : onOverSpeedDiconnectedGenerators) {
            xmlWriter.writeStartElement("gen");
            xmlWriter.writeCharacters(gen);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("onUnderSpeedDiconnectedGenerators", onUnderSpeedDiconnectedGenerators.toString(),
                               "onOverSpeedDiconnectedGenerators", onOverSpeedDiconnectedGenerators.toString());
    }
}
