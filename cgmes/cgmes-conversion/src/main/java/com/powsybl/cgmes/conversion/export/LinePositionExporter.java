/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.iidm.network.extensions.LinePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class LinePositionExporter extends AbstractPositionExporter {

    private static final Logger LOG = LoggerFactory.getLogger(LinePositionExporter.class);

    public LinePositionExporter(TripleStore tripleStore, ExportContext context) {
        super(tripleStore, context);
    }

    public void exportPosition(Line line) {
        Objects.requireNonNull(line);
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        exportPosition(line.getId(), line.getNameOrId(), linePosition);
    }

    public void exportPosition(DanglingLine danglingLine) {
        Objects.requireNonNull(danglingLine);
        LinePosition<DanglingLine> linePosition = danglingLine.getExtension(LinePosition.class);
        exportPosition(danglingLine.getId(), danglingLine.getNameOrId(), linePosition);
    }

    private void exportPosition(String id, String name, LinePosition<?> linePosition) {
        if (linePosition == null) {
            LOG.warn("Cannot find position data of line {}, name {}: skipping export of line position", id, name);
            return;
        }
        String locationId = addLocation(id, name);
        for (int i = 0; i < linePosition.getCoordinates().size(); i++) {
            addLocationPoint(locationId, linePosition.getCoordinates().get(i), i + 1);
        }
    }

}
