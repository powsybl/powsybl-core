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

    private static final String TAG_ID = "id";
    private static final String TAG_INDEX = "index";
    private static final String TAG_LOCKED_TAP_CHANGER_LOAD = "lockedTapChangerLoad";
    private static final String TAG_NAME = "name";
    private static final String TAG_STOPPED_TAP_CHANGER_LOAD = "stoppedTapChangerLoad";
    private static final String TAG_UNDER_VOLTAGE_AUTOMATON_GENERATOR = "underVoltageAutomatonGenerator";
    private static final String TAG_UNDER_BUS_VOLTAGE = "underVoltageBus";

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
                        case TAG_LOCKED_TAP_CHANGER_LOAD:
                        case TAG_STOPPED_TAP_CHANGER_LOAD:
                        case TAG_UNDER_VOLTAGE_AUTOMATON_GENERATOR:
                            id = xmlsr.getAttributeValue(null, TAG_ID);
                            break;
                        case TAG_UNDER_BUS_VOLTAGE:
                        case TAG_INDEX:
                            // nothing to do
                            break;
                        default:
                            throw new AssertionError("Unexpected element: " + xmlsr.getLocalName());
                    }
                    break;

                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case TAG_LOCKED_TAP_CHANGER_LOAD:
                            assertIdNonNull(id);
                            lockedTapChangerLoads.put(id, Float.parseFloat(text));
                            id = null;
                            break;
                        case TAG_STOPPED_TAP_CHANGER_LOAD:
                            assertIdNonNull(id);
                            stoppedTapChangerLoads.put(id, Float.parseFloat(text));
                            id = null;
                            break;
                        case TAG_UNDER_VOLTAGE_AUTOMATON_GENERATOR:
                            assertIdNonNull(id);
                            underVoltageAutomatonGenerators.put(id, Float.parseFloat(text));
                            id = null;
                            break;
                        case TAG_UNDER_BUS_VOLTAGE:
                            underVoltageBuses.add(text);
                            break;
                        case TAG_INDEX:
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

    private static void assertIdNonNull(String id) {
        if (id == null) {
            throw new AssertionError();
        }
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
        xmlWriter.writeStartElement(TAG_INDEX);
        xmlWriter.writeAttribute(TAG_NAME, XML_NAME);
        toXml(xmlWriter, TAG_LOCKED_TAP_CHANGER_LOAD, lockedTapChangerLoads);
        toXml(xmlWriter, TAG_STOPPED_TAP_CHANGER_LOAD, stoppedTapChangerLoads);
        toXml(xmlWriter, TAG_UNDER_VOLTAGE_AUTOMATON_GENERATOR, underVoltageAutomatonGenerators);

        for (String underVoltageBus : underVoltageBuses) {
            xmlWriter.writeStartElement(TAG_UNDER_BUS_VOLTAGE);
            xmlWriter.writeCharacters(underVoltageBus);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    private static void toXml(XMLStreamWriter xmlWriter, String xmlTagName, Map<String, Float> values) throws XMLStreamException {
        for (Map.Entry<String, Float> e : values.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement(xmlTagName);
            xmlWriter.writeAttribute(TAG_ID, id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of(TAG_LOCKED_TAP_CHANGER_LOAD, lockedTapChangerLoads.toString(),
                               TAG_STOPPED_TAP_CHANGER_LOAD, stoppedTapChangerLoads.toString(),
                               TAG_UNDER_VOLTAGE_AUTOMATON_GENERATOR, underVoltageAutomatonGenerators.toString(),
                               TAG_UNDER_BUS_VOLTAGE, underVoltageBuses.toString());
    }
}
