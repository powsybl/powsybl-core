/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LinePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamWriter;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class LinePositionExporter extends AbstractPositionExporter {

    private static final Logger LOG = LoggerFactory.getLogger(LinePositionExporter.class);

    public LinePositionExporter(XMLStreamWriter writer, CgmesExportContext context, String coordinateSystemId) {
        super(writer, context, coordinateSystemId);
    }

    public void exportPosition(Line line) {
        Objects.requireNonNull(line);
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        exportPosition(line, linePosition);
    }

    public void exportPosition(BoundaryLine boundaryLine) {
        Objects.requireNonNull(boundaryLine);
        LinePosition<BoundaryLine> linePosition = boundaryLine.getExtension(LinePosition.class);
        exportPosition(boundaryLine, linePosition);
    }

    private void exportPosition(Identifiable<?> line, LinePosition<?> linePosition) {
        if (linePosition == null) {
            LOG.warn("Cannot find position data of line {}, name {}: skipping export of line position", line.getId(), line.getNameOrId());
            return;
        }
        writeLocation(line, linePosition.getCoordinates(), true);
    }

}
