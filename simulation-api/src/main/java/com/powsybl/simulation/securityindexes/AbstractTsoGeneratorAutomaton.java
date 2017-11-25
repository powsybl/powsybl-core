/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public abstract class AbstractTsoGeneratorAutomaton extends AbstractSecurityIndex {

    protected static final String GEN = "gen";
    protected static final String INDEX = "index";
    protected static final String NAME = "name";

    protected AbstractTsoGeneratorAutomaton(String contingencyId, SecurityIndexType securityIndexType) {
        super(contingencyId, securityIndexType);
    }

    protected static void toXml(XMLStreamWriter xmlWriter, String xmlTagName, String xmlUnderTagName, List<String> underGeneratorNames, String xmlOverTagName, List<String> overGeneratorNames) throws XMLStreamException {
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement(INDEX);
        xmlWriter.writeAttribute(NAME, xmlTagName);
        xmlWriter.writeStartElement(xmlUnderTagName);
        for (String gen : underGeneratorNames) {
            xmlWriter.writeStartElement(GEN);
            xmlWriter.writeCharacters(gen);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeStartElement(xmlOverTagName);
        for (String gen : overGeneratorNames) {
            xmlWriter.writeStartElement(GEN);
            xmlWriter.writeCharacters(gen);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

}
