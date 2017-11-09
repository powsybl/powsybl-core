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
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiCriteriaVoltageStabilityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "multi-criteria-voltage-stability";

    private static final float LOCKED_TAP_CHANGER_LOAD_THRESHOLD = 0f;
    private static final float STOPPED_TAP_CHANGER_LOAD_THRESHOLD = 0f;
    private static final float UNDER_VOLTAGE_AUTOMATON_GENERATOR_THRESHOLD = 0f;

    private final Map<String, Float> lockedTapChangerLoads;

    private final Map<String, Float> stoppedTapChangerLoads;

    private final Map<String, Float> underVoltageAutomatonGenerators;

    private final Set<String> underVoltageBuses;

    public static MultiCriteriaVoltageStabilityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        String text = null;
        Map<String, Float> lockedTapChangerLoads = new LinkedHashMap<>();
        Map<String, Float> stoppedTapChangerLoads = new LinkedHashMap<>();
        Map<String, Float> underVoltageAutomatonGenerators = new LinkedHashMap<>();
        Set<String> underVoltageBuses = new LinkedHashSet<>();
        String id = null;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;
                case XMLEvent.START_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case "lockedTapChangerLoad":
                        case "stoppedTapChangerLoad":
                        case "underVoltageAutomatonGenerator":
                            id = xmlsr.getAttributeValue(null, "id");
                            break;
                        default:
                            throw new AssertionError("Unexpected element: " + xmlsr.getLocalName());
                    }
                    break;

                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case "lockedTapChangerLoad":
                            if (id == null) {
                                throw new AssertionError();
                            }
                            lockedTapChangerLoads.put(id, Float.parseFloat(text));
                            id = null;
                            break;
                        case "stoppedTapChangerLoad":
                            if (id == null) {
                                throw new AssertionError();
                            }
                            stoppedTapChangerLoads.put(id, Float.parseFloat(text));
                            id = null;
                            break;
                        case "underVoltageAutomatonGenerator":
                            if (id == null) {
                                throw new AssertionError();
                            }
                            underVoltageAutomatonGenerators.put(id, Float.parseFloat(text));
                            id = null;
                            break;
                        case "underVoltageBus":
                            underVoltageBuses.add(text);
                            break;
                        case "index":
                            return new MultiCriteriaVoltageStabilityIndex(contingencyId, lockedTapChangerLoads, stoppedTapChangerLoads,
                                                                          underVoltageAutomatonGenerators, underVoltageBuses);
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

    public MultiCriteriaVoltageStabilityIndex(String contingencyId, Map<String, Float> lockedTapChangerLoads,
                                              Map<String, Float> stoppedTapChangerLoads, Map<String, Float> underVoltageAutomatonGenerators,
                                              Set<String> underVoltageBuses) {
        super(contingencyId, SecurityIndexType.MULTI_CRITERIA_VOLTAGE_STABILITY);
        this.lockedTapChangerLoads = Objects.requireNonNull(lockedTapChangerLoads);
        this.stoppedTapChangerLoads = Objects.requireNonNull(stoppedTapChangerLoads);
        this.underVoltageAutomatonGenerators = Objects.requireNonNull(underVoltageAutomatonGenerators);
        this.underVoltageBuses = Objects.requireNonNull(underVoltageBuses);
    }

    public Map<String, Float> getLockedTapChangerLoads() {
        return lockedTapChangerLoads;
    }

    public Map<String, Float> getStoppedTapChangerLoads() {
        return stoppedTapChangerLoads;
    }

    public Map<String, Float> getUnderVoltageAutomatonGenerators() {
        return underVoltageAutomatonGenerators;
    }

    public Set<String> getUnderVoltageBuses() {
        return underVoltageBuses;
    }

    @Override
    public boolean isOk() {
        return lockedTapChangerLoads.entrySet().stream().mapToDouble(Map.Entry::getValue).sum() <= LOCKED_TAP_CHANGER_LOAD_THRESHOLD
                && stoppedTapChangerLoads.entrySet().stream().mapToDouble(Map.Entry::getValue).sum() <= STOPPED_TAP_CHANGER_LOAD_THRESHOLD
                && underVoltageAutomatonGenerators.entrySet().stream().mapToDouble(Map.Entry::getValue).sum() <= UNDER_VOLTAGE_AUTOMATON_GENERATOR_THRESHOLD
                && underVoltageBuses.isEmpty();
    }

    @Override
    protected void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);
        for (Map.Entry<String, Float> e : lockedTapChangerLoads.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement("lockedTapChangerLoad");
            xmlWriter.writeAttribute("id", id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
        for (Map.Entry<String, Float> e : stoppedTapChangerLoads.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement("stoppedTapChangerLoad");
            xmlWriter.writeAttribute("id", id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
        for (Map.Entry<String, Float> e : underVoltageAutomatonGenerators.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement("underVoltageAutomatonGenerators");
            xmlWriter.writeAttribute("id", id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
        for (String underVoltageBus : underVoltageBuses) {
            xmlWriter.writeStartElement("underVoltageBus");
            xmlWriter.writeCharacters(underVoltageBus);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("lockedTapChangerLoads", lockedTapChangerLoads.toString(),
                               "stoppedTapChangerLoads", stoppedTapChangerLoads.toString(),
                               "underVoltageAutomatonGenerators", underVoltageAutomatonGenerators.toString(),
                               "underVoltageBuses", underVoltageBuses.toString());
    }
}
