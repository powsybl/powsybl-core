/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
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

    public void importPosition() {
        Map<Line, SortedMap<Integer, Coordinate>> lineCoordinates = new HashMap<>();
        Map<DanglingLine, SortedMap<Integer, Coordinate>> danglingLineCoordinates = new HashMap<>();

        cgmesGLModel.getLinesPositions().forEach(propertyBag -> importPosition(propertyBag, lineCoordinates, danglingLineCoordinates));

        for (Map.Entry<Line, SortedMap<Integer, Coordinate>> e : lineCoordinates.entrySet()) {
            Line line = e.getKey();
            SortedMap<Integer, Coordinate> coordinates = e.getValue();
            line.newExtension(LinePositionAdder.class).withCoordinates(new ArrayList<>(coordinates.values())).add();
        }

        for (Map.Entry<DanglingLine, SortedMap<Integer, Coordinate>> e : danglingLineCoordinates.entrySet()) {
            DanglingLine danglingLine = e.getKey();
            SortedMap<Integer, Coordinate> coordinates = e.getValue();
            danglingLine.newExtension(LinePositionAdder.class).withCoordinates(new ArrayList<>(coordinates.values())).add();
        }
    }

    private void importPosition(PropertyBag linePositionData, Map<Line, SortedMap<Integer, Coordinate>> lineCoordinates,
                                Map<DanglingLine, SortedMap<Integer, Coordinate>> danglingLineCoordinates) {
        Objects.requireNonNull(linePositionData);
        if (!CgmesGLUtils.checkCoordinateSystem(linePositionData.getId("crsName"), linePositionData.getId("crsUrn"))) {
            throw new PowsyblException("Unsupported coodinates system: " + linePositionData.getId("crsName"));
        }
        String lineId = linePositionData.getId("powerSystemResource");
        Line line = network.getLine(lineId);
        if (line != null) {
            lineCoordinates.computeIfAbsent(line, k -> new TreeMap<>())
                    .put(linePositionData.asInt("seq"), new Coordinate(linePositionData.asDouble("y"), linePositionData.asDouble("x")));
                     // y <=> lat, x <=> lon
        } else {
            DanglingLine danglingLine = network.getDanglingLine(lineId);
            if (danglingLine != null) {
                danglingLineCoordinates.computeIfAbsent(danglingLine, k -> new TreeMap<>())
                        .put(linePositionData.asInt("seq"), new Coordinate(linePositionData.asDouble("y"), linePositionData.asDouble("x")));
                        // y <=> lat, x <=> lon
            } else {
                LOG.warn("Cannot find line/dangling {}, name {} in network {}: skipping line position", lineId, linePositionData.get("name"), network.getId());
            }
        }
    }

}
