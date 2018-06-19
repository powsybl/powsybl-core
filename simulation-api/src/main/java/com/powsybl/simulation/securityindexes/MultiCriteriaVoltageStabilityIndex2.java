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
public class MultiCriteriaVoltageStabilityIndex2 extends AbstractSecurityIndex {

    static final String XML_NAME = "multi-criteria-voltage-stability2";

    private static final String TAG_BUS = "bus";
    private static final String TAG_CONVERGE = "converge";
    private static final String TAG_CRITERIA1 = "criteria1";
    private static final String TAG_CRITERIA2 = "criteria2";
    private static final String TAG_CRITERIA3 = "criteria3";
    private static final String TAG_ID = "id";
    private static final String TAG_INDEX = "index";
    private static final String TAG_NAME = "name";

    private static final float CRITERIA1_THRESHOLD = 200f;
    private static final float CRITERIA2_THRESHOLD = 1500f;

    private enum CriteriaState {
        ONE,
        TWO,
        THREE
    }

    private boolean converge;

    private final Map<String, Float> criteria1;

    private final Map<String, Float> criteria2;

    private final Set<String> criteria3;

    public static MultiCriteriaVoltageStabilityIndex2 fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        Boolean converge = null;
        Map<String, Float> criteria1 = new LinkedHashMap<>();
        Map<String, Float> criteria2 = new LinkedHashMap<>();
        Set<String> criteria3 = new LinkedHashSet<>();
        CriteriaState state = null;

        String text = null;
        String id = null;
        while (xmlsr.hasNext()) {
            int eventType = xmlsr.next();
            switch (eventType) {
                case XMLEvent.CHARACTERS:
                    text = xmlsr.getText();
                    break;

                case XMLEvent.START_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case TAG_CRITERIA1:
                            state = CriteriaState.ONE;
                            break;

                        case TAG_CRITERIA2:
                            state = CriteriaState.TWO;
                            break;

                        case TAG_CRITERIA3:
                            state = CriteriaState.THREE;
                            break;

                        case TAG_BUS:
                            id = xmlsr.getAttributeValue(null, TAG_ID);
                            break;

                        case TAG_CONVERGE:
                        case TAG_INDEX:
                            // nothing to do
                            break;

                        default:
                            throw new AssertionError("Unexpected element: " + xmlsr.getLocalName());
                    }
                    break;

                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case TAG_CONVERGE:
                            converge = Boolean.parseBoolean(text);
                            break;

                        case TAG_CRITERIA1:
                        case TAG_CRITERIA2:
                        case TAG_CRITERIA3:
                            state = null;
                            break;

                        case TAG_BUS:
                            Objects.requireNonNull(state, "state is null");

                            switch (state) {
                                case ONE:
                                    criteria1.put(id, Float.parseFloat(text));
                                    break;
                                case TWO:
                                    criteria2.put(id, Float.parseFloat(text));
                                    break;
                                case THREE:
                                    criteria3.add(text);
                                    break;
                                default:
                                    throw new AssertionError("Unexpected CriteriaState value: " + state);
                            }
                            break;

                        case TAG_INDEX:
                            return new MultiCriteriaVoltageStabilityIndex2(contingencyId, converge, criteria1, criteria2, criteria3);

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

    public MultiCriteriaVoltageStabilityIndex2(String contingencyId, boolean converge, Map<String, Float> criteria1, Map<String, Float> criteria2, Set<String> criteria3) {
        super(contingencyId, SecurityIndexType.MULTI_CRITERIA_VOLTAGE_STABILITY2);
        this.converge = converge;
        this.criteria1 = Objects.requireNonNull(criteria1);
        this.criteria2 = Objects.requireNonNull(criteria2);
        this.criteria3 = Objects.requireNonNull(criteria3);
    }

    public boolean isConverge() {
        return converge;
    }

    public static boolean areCriteriaOk(Map<String, Float> criteria1, Map<String, Float> criteria2, Set<String> criteria3) {
        return criteria1.entrySet().stream().mapToDouble(Map.Entry::getValue).sum() <= CRITERIA1_THRESHOLD
                && criteria2.entrySet().stream().mapToDouble(Map.Entry::getValue).sum() <= CRITERIA2_THRESHOLD
                && criteria3.isEmpty();
    }

    public Map<String, Float> getCriteria1() {
        return criteria1;
    }

    public Map<String, Float> getCriteria2() {
        return criteria2;
    }

    public Set<String> getCriteria3() {
        return criteria3;
    }

    @Override
    public boolean isOk() {
        return converge && areCriteriaOk(criteria1, criteria2, criteria3);
    }

    @Override
    protected void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement(TAG_INDEX);
        xmlWriter.writeAttribute(TAG_NAME, XML_NAME);

        xmlWriter.writeStartElement(TAG_CONVERGE);
        xmlWriter.writeCharacters(Boolean.toString(converge));
        xmlWriter.writeEndElement();

        toXml(xmlWriter, TAG_CRITERIA1, criteria1);
        toXml(xmlWriter, TAG_CRITERIA2, criteria2);

        xmlWriter.writeStartElement(TAG_CRITERIA3);
        for (String underVoltageBus : criteria3) {
            xmlWriter.writeStartElement(TAG_BUS);
            xmlWriter.writeCharacters(underVoltageBus);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    private static void toXml(XMLStreamWriter xmlWriter, String xmlCriteriaTagName, Map<String, Float> criteria) throws XMLStreamException {
        xmlWriter.writeStartElement(xmlCriteriaTagName);
        for (Map.Entry<String, Float> e : criteria.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement(TAG_BUS);
            xmlWriter.writeAttribute(TAG_ID, id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of(TAG_CONVERGE, Boolean.toString(converge),
                               TAG_CRITERIA1, criteria1.toString(),
                               TAG_CRITERIA2, criteria2.toString(),
                               TAG_CRITERIA3, criteria3.toString());
    }
}
