/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model.statement;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.List;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class CgmesObjectTest {

    private static final String CIM = "http://iec.ch/TC57/CIM100#";

    private static CgmesObject synchronousMachine() {
        return CgmesObject.builder("SynchronousMachine", "gen1")
            .literal("RotatingMachine.p", "-90")
            .resource("SynchronousMachine.operatingMode", CIM + "SynchronousMachineOperatingMode.generator")
            .build();
    }

    @Test
    void statements() {
        assertEquals(List.of(
                new CgmesStatement("gen1", CgmesStatement.TYPE, "SynchronousMachine", true),
                new CgmesStatement("gen1", "RotatingMachine.p", "-90", false),
                new CgmesStatement("gen1", "SynchronousMachine.operatingMode", CIM + "SynchronousMachineOperatingMode.generator", true)),
            synchronousMachine().statements());
    }

    @Test
    void writeXml() throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
        writer.setPrefix("cim", CIM);
        writer.setPrefix("rdf", RDF_NAMESPACE);
        new CgmesObjectXmlWriter(writer, CIM, subject -> "#_" + subject).write(synchronousMachine());
        writer.close();
        assertEquals("<cim:SynchronousMachine rdf:about=\"#_gen1\">"
                + "<cim:RotatingMachine.p>-90</cim:RotatingMachine.p>"
                + "<cim:SynchronousMachine.operatingMode rdf:resource=\"" + CIM + "SynchronousMachineOperatingMode.generator\"/>"
                + "</cim:SynchronousMachine>",
            stringWriter.toString());
    }
}
