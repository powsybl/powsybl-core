/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.BoundaryLineFilter;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class CgmesGLExporter {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesGLExporter.class);

    private static final String COORDINATE_SYSTEM_NAME = "WGS84";

    private final Network network;
    private final CgmesExportContext context;

    /**
     * @param context the context of the CGMES export of the other subsets: sharing it ensures that the GL subset
     *                refers to the same identifiers (power system resources, EQ model) as the exported EQ subset
     */
    public CgmesGLExporter(Network network, CgmesExportContext context) {
        this.network = Objects.requireNonNull(network);
        this.context = Objects.requireNonNull(context);
    }

    public CgmesGLExporter(Network network) {
        this(network, new CgmesExportContext(network));
    }

    public void exportData(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        String fileName = dataSource.getBaseName() + "_" + CgmesSubset.GEOGRAPHICAL_LOCATION.getIdentifier() + ".xml";
        try (OutputStream out = new BufferedOutputStream(dataSource.newOutputStream(fileName, false))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", out);
            write(writer);
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void write(XMLStreamWriter writer) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);
        CgmesExportUtil.writeModelDescription(network, CgmesSubset.GEOGRAPHICAL_LOCATION, writer, initializeModel(), context);
        String coordinateSystemId = writeCoordinateSystem(cimNamespace, writer);
        exportSubstationsPosition(new SubstationPositionExporter(writer, context, coordinateSystemId));
        exportLinesPosition(new LinePositionExporter(writer, context, coordinateSystemId));
        writer.writeEndDocument();
    }

    private CgmesMetadataModel initializeModel() {
        CgmesMetadataModel model = CgmesExport.initializeModelForExport(network, CgmesSubset.GEOGRAPHICAL_LOCATION, context, true, false);
        // Profile of the CIM version of the export, replacing the one of the imported GL model if any
        model.setProfile(CgmesGLUtils.glProfileUri(context.getCim()));
        if (context.updateDependencies()) {
            String eqModelId = CgmesExport.initializeModelForExport(network, CgmesSubset.EQUIPMENT, context, true, false).getId();
            model.clearDependencies().addDependentOn(eqModelId);
        }
        return model;
    }

    private String writeCoordinateSystem(String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        String id = context.getNamingStrategy().getCgmesId(refTyped(network), ref("CoordinateSystem"));
        CgmesExportUtil.writeStartIdName("CoordinateSystem", id, COORDINATE_SYSTEM_NAME, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "CoordinateSystem.crsUrn");
        writer.writeCharacters(CgmesGLUtils.COORDINATE_SYSTEM_URN);
        writer.writeEndElement();
        writer.writeEndElement();
        return id;
    }

    private void exportSubstationsPosition(SubstationPositionExporter positionExporter) {
        LOG.info("Exporting Substations Position");
        network.getSubstationStream().forEach(positionExporter::exportPosition);
    }

    private void exportLinesPosition(LinePositionExporter positionExporter) {
        LOG.info("Exporting Lines Position");
        network.getLineStream().forEach(positionExporter::exportPosition);
        LOG.info("Exporting Boundary Lines Position");
        network.getBoundaryLineStream(BoundaryLineFilter.UNPAIRED).forEach(positionExporter::exportPosition);
    }

}
