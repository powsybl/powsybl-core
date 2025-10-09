/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class LinePositionImporter {

    private static final Logger LOG = LoggerFactory.getLogger(LinePositionImporter.class);

    private final Network network;

    private final CgmesGLModel cgmesGLModel;

    public LinePositionImporter(Network network, CgmesGLModel cgmesGLModel) {
        this.network = Objects.requireNonNull(network);
        this.cgmesGLModel = Objects.requireNonNull(cgmesGLModel);
    }

    public void importPositions() {
        Map<Identifiable<?>, SortedMap<Integer, Coordinate>> lineOrDanglingLineCoordinates = new HashMap<>();

        cgmesGLModel.getLinesPositions().forEach(propertyBag -> importPositions(propertyBag, lineOrDanglingLineCoordinates));

        lineOrDanglingLineCoordinates.forEach((lOrDl, coordinates) ->
                lOrDl.newExtension(LinePositionAdder.class).withCoordinates(coordinates.values().stream().toList()).add());
    }

    private void importPositions(PropertyBag linePositionData, Map<Identifiable<?>, SortedMap<Integer, Coordinate>> lineOrDanglingLineCoordinates) {
        Objects.requireNonNull(linePositionData);
        String crsUrn = linePositionData.getId("crsUrn");
        if (!CgmesGLUtils.checkCoordinateSystem(crsUrn)) {
            throw new PowsyblException("Unsupported coordinates system: " + crsUrn);
        }
        String lineId = linePositionData.getId("powerSystemResource");
        Identifiable<?> lineOrDanglingLine = getLineOrDanglingLine(lineId);
        if (lineOrDanglingLine != null) {
            lineOrDanglingLineCoordinates.computeIfAbsent(lineOrDanglingLine, k -> new TreeMap<>())
                    .put(linePositionData.asInt("seq"), new Coordinate(linePositionData.asDouble("y"), linePositionData.asDouble("x")));
                    // y <=> lat, x <=> lon
        } else {
            LOG.warn("Cannot find line/dangling {}, name {} in network {}: skipping line position", lineId, linePositionData.get("name"), network.getId());
        }
    }

    private Identifiable<?> getLineOrDanglingLine(String lineId) {
        Line line = network.getLine(lineId);
        if (line != null) {
            return line;
        }
        return network.getDanglingLine(lineId);
    }
}
