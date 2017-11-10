/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class SecurityIndexParser {

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private SecurityIndexParser() {
    }

    public static List<SecurityIndex> fromXml(String contingencyId, Reader reader) {
        List<SecurityIndex> indexes = new ArrayList<>();
        try {
            XMLStreamReader xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(reader);
            while (xmlsr.hasNext()) {
                int eventType = xmlsr.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        switch (xmlsr.getLocalName()) {
                            case "index":
                                switch (xmlsr.getAttributeValue(null, "name")) {
                                    case OverloadSecurityIndex.XML_NAME:
                                        indexes.add(OverloadSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case UnderOverVoltageSecurityIndex.XML_NAME:
                                        indexes.add(UnderOverVoltageSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case SmallSignalSecurityIndex.XML_NAME:
                                        indexes.add(SmallSignalSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TransientSecurityIndex.XML_NAME:
                                        indexes.add(TransientSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoOverloadSecurityIndex.XML_NAME:
                                        indexes.add(TsoOverloadSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoOvervoltageSecurityIndex.XML_NAME:
                                        indexes.add(TsoOvervoltageSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoUndervoltageSecurityIndex.XML_NAME:
                                        indexes.add(TsoUndervoltageSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoFrequencySecurityIndex.XML_NAME:
                                        indexes.add(TsoFrequencySecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoSynchroLossSecurityIndex.XML_NAME:
                                        indexes.add(TsoSynchroLossSecurityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoGeneratorVoltageAutomaton.XML_NAME:
                                        indexes.add(TsoGeneratorVoltageAutomaton.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoGeneratorSpeedAutomaton.XML_NAME:
                                        indexes.add(TsoGeneratorSpeedAutomaton.fromXml(contingencyId, xmlsr));
                                        break;
                                    case TsoDisconnectedGenerator.XML_NAME:
                                        indexes.add(TsoDisconnectedGenerator.fromXml(contingencyId, xmlsr));
                                        break;
                                    case MultiCriteriaVoltageStabilityIndex.XML_NAME:
                                        indexes.add(MultiCriteriaVoltageStabilityIndex.fromXml(contingencyId, xmlsr));
                                        break;
                                    case MultiCriteriaVoltageStabilityIndex2.XML_NAME:
                                        indexes.add(MultiCriteriaVoltageStabilityIndex2.fromXml(contingencyId, xmlsr));
                                        break;
                                    default:
                                        throw new AssertionError("Unexpected attribute value: " + xmlsr.getAttributeValue(null, "name"));
                                }
                                break;

                            default:
                                throw new AssertionError("Unexpected element: " + xmlsr.getLocalName());
                        }
                        break;

                    default:
                        break;
                }
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
        return indexes;
    }

}
