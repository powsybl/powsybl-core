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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TsoGeneratorSpeedAutomaton extends AbstractTsoGeneratorAutomaton {

    static final String XML_NAME = "tso-generator-speed-automaton";

    private static final String ON_UNDER_SPEED_DISCONNECTED_GENERATORS = "onUnderSpeedDisconnectedGenerators";
    private static final String ON_OVER_SPEED_DISCONNECTED_GENERATORS = "onOverSpeedDisconnectedGenerators";

    private final List<String> onUnderSpeedDiconnectedGenerators;
    private final List<String> onOverSpeedDiconnectedGenerators;

    public static TsoGeneratorSpeedAutomaton fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        LimitsXmlParsingState state = null;
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
                        case ON_UNDER_SPEED_DISCONNECTED_GENERATORS:
                            state = LimitsXmlParsingState.UNDER;
                            break;

                        case ON_OVER_SPEED_DISCONNECTED_GENERATORS:
                            state = LimitsXmlParsingState.OVER;
                            break;

                        case GEN:
                        case INDEX:
                            // nothing to do
                            break;

                        default:
                            throw new AssertionError("Unexpected element: " + xmlsr.getLocalName());
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case ON_UNDER_SPEED_DISCONNECTED_GENERATORS:
                        case ON_OVER_SPEED_DISCONNECTED_GENERATORS:
                            state = null;
                            break;

                        case GEN:
                            LimitsXmlParsingState.addGenerator(state, text, onUnderSpeedDiconnectedGenerators, onOverSpeedDiconnectedGenerators);
                            break;

                        case INDEX:
                            return new TsoGeneratorSpeedAutomaton(contingencyId, onUnderSpeedDiconnectedGenerators, onOverSpeedDiconnectedGenerators);

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
        toXml(xmlWriter, XML_NAME,
            ON_UNDER_SPEED_DISCONNECTED_GENERATORS, onUnderSpeedDiconnectedGenerators,
            ON_OVER_SPEED_DISCONNECTED_GENERATORS, onOverSpeedDiconnectedGenerators);
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of(ON_UNDER_SPEED_DISCONNECTED_GENERATORS, onUnderSpeedDiconnectedGenerators.toString(),
                               ON_OVER_SPEED_DISCONNECTED_GENERATORS, onOverSpeedDiconnectedGenerators.toString());
    }
}
