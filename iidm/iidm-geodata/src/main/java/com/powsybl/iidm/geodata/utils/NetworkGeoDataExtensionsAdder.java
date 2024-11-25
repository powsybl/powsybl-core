/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public final class NetworkGeoDataExtensionsAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkGeoDataExtensionsAdder.class);

    private NetworkGeoDataExtensionsAdder() {
    }

    public static void fillNetworkSubstationsGeoData(Network network, Map<String, Coordinate> substationsCoordinates) {
        substationsCoordinates.forEach((substationId, coordinate) -> {
            Substation foundStation = network.getSubstation(substationId);
            if (foundStation != null) {
                foundStation.newExtension(SubstationPositionAdder.class)
                        .withCoordinate(coordinate)
                        .add();
            }
        });
    }

    public static void fillNetworkLinesGeoData(Network network, Map<String, List<Coordinate>> linesGeoData) {
        AtomicInteger unknownLines = new AtomicInteger();
        linesGeoData.forEach((lineId, lineCoordinates) -> {
            Line line = network.getLine(lineId);
            if (line != null) {
                line.newExtension(LinePositionAdder.class)
                        .withCoordinates(LineCoordinatesOrdering.order(line, lineCoordinates))
                        .add();
            } else {
                unknownLines.getAndIncrement();
            }
        });
        LOGGER.warn("{} unknown lines discarded", unknownLines.get());
    }
}
