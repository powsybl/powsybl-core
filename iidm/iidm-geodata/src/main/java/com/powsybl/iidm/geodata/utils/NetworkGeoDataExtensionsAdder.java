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
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
import com.powsybl.iidm.network.extensions.SubstationPosition;
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

    public static void fillNetworkSubstationsGeoData(Network network, Map<String, Coordinate> substationsCoordinates, boolean forceGeoDataComputation) {
        AtomicInteger unknownSubstations = new AtomicInteger();
        network.getSubstations().forEach(substation -> fillSubstationGeoData(substation, substationsCoordinates, forceGeoDataComputation, unknownSubstations));
        LOGGER.info("{} substations with no geographic data found", unknownSubstations.get());
    }

    public static void fillNetworkLinesGeoData(Network network, Map<String, List<Coordinate>> linesCoordinates, boolean forceGeoDataComputation) {
        AtomicInteger unknownLines = new AtomicInteger();
        network.getLines().forEach(line -> fillLineGeoData(line, linesCoordinates, forceGeoDataComputation, unknownLines));
        LOGGER.info("{} lines with no geographic data found", unknownLines.get());
    }

    private static void fillSubstationGeoData(Substation substation, Map<String, Coordinate> substationsCoordinates,
                                              boolean forceGeoDataComputation, AtomicInteger unknownSubstations) {
        if (substationsCoordinates.containsKey(substation.getId())) {
            SubstationPosition substationPosition = substation.getExtension(SubstationPosition.class);
            if (substationPosition != null) {
                if (forceGeoDataComputation) {
                    substation.removeExtension(SubstationPosition.class);
                    addSubstationPositionExtension(substation, substationsCoordinates.get(substation.getId()));
                }
            } else {
                addSubstationPositionExtension(substation, substationsCoordinates.get(substation.getId()));
            }
        } else {
            unknownSubstations.getAndIncrement();
        }
    }

    private static void addSubstationPositionExtension(Substation substation, Coordinate coordinates) {
        substation.newExtension(SubstationPositionAdder.class)
                    .withCoordinate(coordinates)
                    .add();
    }

    private static void fillLineGeoData(com.powsybl.iidm.network.Line line, Map<String, List<Coordinate>> linesCoordinates,
                                        boolean forceGeoDataComputation, AtomicInteger unknownLines) {
        if (linesCoordinates.containsKey(line.getId())) {
            LinePosition linePosition = line.getExtension(LinePosition.class);
            if (linePosition != null) {
                if (forceGeoDataComputation) {
                    line.removeExtension(LinePosition.class);
                    addLinePositionExtension(line, linesCoordinates.get(line.getId()));
                }
            } else {
                addLinePositionExtension(line, linesCoordinates.get(line.getId()));
            }
        } else {
            unknownLines.getAndIncrement();
        }
    }

    private static void addLinePositionExtension(Line line, List<Coordinate> coordinates) {
        line.newExtension(LinePositionAdder.class)
                .withCoordinates(LineCoordinatesOrdering.order(line, coordinates))
                .add();
    }
}
