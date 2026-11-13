/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Network;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Common class for writing CGMES XML files.
 *
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public abstract class AbstractCgmesWriter {

    protected final XMLStreamWriter writer;
    protected final CgmesExportContext context;

    protected AbstractCgmesWriter(XMLStreamWriter writer, CgmesExportContext context) {
        this.writer = writer;
        this.context = context;
    }

    /**
     * Write the RDF root element that includes rdf, md, cim and entsoe/eu namespaces definition.
     */
    public void writeRdfRoot() throws XMLStreamException {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        CgmesExportUtil.writeRdfRoot(context, writer);
    }

    /**
     * Write the network metadata into the FullModel element in accordance with IEC-61970-552.
     */
    public void writeModelDescription(Network network, CgmesSubset subset, CgmesMetadataModel model) throws XMLStreamException {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        CgmesExportUtil.writeModelDescription(network, subset, writer, model, context);
    }

    /**
     * Write the closing tag of the RDF root element.
     */
    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

}
