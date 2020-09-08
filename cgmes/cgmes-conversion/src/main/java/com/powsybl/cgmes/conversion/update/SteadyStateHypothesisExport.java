/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at rte-france.com>
 */
public final class SteadyStateHypothesisExport {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        try {
            CgmesExport.writeRdfRoot(writer);

            if ("16".equals(network.getProperty(CgmesExport.CIM_VERSION))) {
                writeSshModelDescription(network, writer);
            }

            // FIXME(Luma) work in progress
            LOG.info("work in progress ...");
            writeSshEnergyConsumers(network, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSshModelDescription(Network network, XMLStreamWriter writer) throws XMLStreamException {
        // FIXME(Luma) pending
    }

    private static void writeSshEnergyConsumers(Network network, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            writeSshEnergyConsumer(writer, load.getId(), load.getP0(), load.getQ0());
        }
    }

    private static void writeSshEnergyConsumer(XMLStreamWriter writer, String id, double p, double q) throws XMLStreamException {
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EnergyConsumer");
        writer.writeAttribute(CgmesExport.RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EnergyConsumer.p");
        writer.writeCharacters(CgmesExport.format(p));
        writer.writeEndElement();
        writer.writeStartElement(CgmesExport.CIM_NAMESPACE, "EnergyConsumer.q");
        writer.writeCharacters(CgmesExport.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private SteadyStateHypothesisExport() {
    }
}
