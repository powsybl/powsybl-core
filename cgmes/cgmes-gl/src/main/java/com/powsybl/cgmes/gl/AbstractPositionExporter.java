/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.Coordinate;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;
import java.util.Objects;

import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public abstract class AbstractPositionExporter {

    private static final String LOCATION = "Location";
    private static final String POSITION_POINT = "PositionPoint";

    protected final XMLStreamWriter writer;
    protected final CgmesExportContext context;
    private final String coordinateSystemId;

    protected AbstractPositionExporter(XMLStreamWriter writer, CgmesExportContext context, String coordinateSystemId) {
        this.writer = Objects.requireNonNull(writer);
        this.context = Objects.requireNonNull(context);
        this.coordinateSystemId = Objects.requireNonNull(coordinateSystemId);
    }

    /**
     * @param sequenced whether the position points are ordered, writing their sequence number
     */
    protected void writeLocation(Identifiable<?> powerSystemResource, List<Coordinate> coordinates, boolean sequenced) {
        try {
            String cimNamespace = context.getCim().getNamespace();
            String locationId = context.getNamingStrategy().getCgmesId(refTyped(powerSystemResource), ref(LOCATION));
            CgmesExportUtil.writeStartIdName(LOCATION, locationId, powerSystemResource.getNameOrId(), cimNamespace, writer, context);
            CgmesExportUtil.writeReference("Location.CoordinateSystem", coordinateSystemId, cimNamespace, writer, context);
            CgmesExportUtil.writeReference("Location.PowerSystemResources", context.getNamingStrategy().getCgmesId(powerSystemResource), cimNamespace, writer, context);
            writer.writeEndElement();
            for (int i = 0; i < coordinates.size(); i++) {
                String positionPointId = context.getNamingStrategy().getCgmesId(refTyped(powerSystemResource), ref(POSITION_POINT), ref(i + 1));
                writePositionPoint(positionPointId, locationId, coordinates.get(i), sequenced ? i + 1 : 0, cimNamespace);
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writePositionPoint(String id, String locationId, Coordinate coordinate, int seq, String cimNamespace) throws XMLStreamException {
        CgmesExportUtil.writeStartId(POSITION_POINT, id, true, cimNamespace, writer, context);
        if (seq > 0) {
            writer.writeStartElement(cimNamespace, "PositionPoint.sequenceNumber");
            writer.writeCharacters(Integer.toString(seq));
            writer.writeEndElement();
        }
        writer.writeStartElement(cimNamespace, "PositionPoint.xPosition");
        writer.writeCharacters(Double.toString(coordinate.getLongitude()));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "PositionPoint.yPosition");
        writer.writeCharacters(Double.toString(coordinate.getLatitude()));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("PositionPoint.Location", locationId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

}
