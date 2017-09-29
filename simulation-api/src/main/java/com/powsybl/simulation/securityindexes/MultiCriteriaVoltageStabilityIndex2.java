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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiCriteriaVoltageStabilityIndex2 extends AbstractSecurityIndex {

    static final String XML_NAME = "multi-criteria-voltage-stability2";

    private static final float CRITERIA1_THRESHOLD = 200f;
    private static final float CRITERIA2_THRESHOLD = 1500f;

    private boolean converge;

    private final Map<String, Float> criteria1;

    private final Map<String, Float> criteria2;

    private final Set<String> criteria3;

    public static MultiCriteriaVoltageStabilityIndex2 fromXml(String contingencyId, XMLStreamReader xmlsr) throws XMLStreamException {
        Boolean converge = null;
        Map<String, Float> criteria1 = null;
        Map<String, Float> criteria2 = null;
        Set<String> criteria3 = null;

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
                        case "criteria1":
                            criteria1 = new LinkedHashMap<>();
                            break;

                        case "criteria2":
                            criteria2 = new LinkedHashMap<>();
                            break;

                        case "criteria3":
                            criteria3 = new LinkedHashSet<>();
                            break;

                        case "bus":
                            id = xmlsr.getAttributeValue(null, "id");
                            break;
                    }
                    break;

                case XMLEvent.END_ELEMENT:
                    switch (xmlsr.getLocalName()) {
                        case "converge":
                            converge = Boolean.parseBoolean(text);
                            break;

                        case "criteria1":
                            criteria1 = null;
                            break;

                        case "criteria2":
                            criteria2 = null;
                            break;

                        case "criteria3":
                            criteria3 = null;
                            break;

                        case "bus":
                            if (criteria1 != null) {
                                criteria1.put(id, Float.parseFloat(text));
                            } else if (criteria2 != null) {
                                criteria2.put(id, Float.parseFloat(text));
                            } else if (criteria3 != null) {
                                criteria3.add(id);
                            } else {
                                throw new AssertionError();
                            }
                            break;

                        case "index":
                            return new MultiCriteriaVoltageStabilityIndex2(contingencyId, converge, criteria1, criteria2, criteria3);
                    }
                    break;
            }
        }
        throw new AssertionError("Should not happened");
    }

    public MultiCriteriaVoltageStabilityIndex2(String contingencyId, boolean converge, Map<String, Float> criteria1, Map<String, Float> criteria2, Set<String> criteria3) {
        super(contingencyId, SecurityIndexType.MULTI_CRITERIA_VOLTAGE_STABILITY2);
        this.converge = converge;
        this.criteria1 = criteria1;
        this.criteria2 = criteria2;
        this.criteria3 = criteria3;
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
        xmlWriter.writeStartElement("index");
        xmlWriter.writeAttribute("name", XML_NAME);

        xmlWriter.writeStartElement("converge");
        xmlWriter.writeCharacters(Boolean.toString(converge));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("criteria1");
        for (Map.Entry<String, Float> e : criteria1.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement("bus");
            xmlWriter.writeAttribute("id", id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("criteria2");
        for (Map.Entry<String, Float> e : criteria2.entrySet()) {
            String id = e.getKey();
            float p = e.getValue();
            xmlWriter.writeStartElement("bus");
            xmlWriter.writeAttribute("id", id);
            xmlWriter.writeCharacters(Float.toString(p));
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("criteria3");
        for (String underVoltageBus : criteria3) {
            xmlWriter.writeStartElement("bus");
            xmlWriter.writeCharacters(underVoltageBus);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("converge", Boolean.toString(converge),
                               "criteria1", criteria1.toString(),
                               "criteria2", criteria2.toString(),
                               "criteria3", criteria3.toString());
    }
}
