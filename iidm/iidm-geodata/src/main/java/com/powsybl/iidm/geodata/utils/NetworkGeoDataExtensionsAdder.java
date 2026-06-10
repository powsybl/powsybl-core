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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public final class NetworkGeoDataExtensionsAdder {

    private NetworkGeoDataExtensionsAdder() {
    }

    public static void fillLineGeoData(Network network, String id, List<Coordinate> coordinates,
                                       boolean forceGeoDataComputation, AtomicInteger linesWithNewData,
                                       AtomicInteger linesWithUpdatedData, AtomicInteger unknownLines) {
        Line line = network.getLine(id);
        if (line != null) {
            LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
            if (linePosition != null) {
                if (forceGeoDataComputation) {
                    line.removeExtension(LinePosition.class);
                    addLinePositionExtension(line, coordinates);
                    linesWithUpdatedData.getAndIncrement();
                }
            } else {
                addLinePositionExtension(line, coordinates);
                linesWithNewData.getAndIncrement();
            }
        } else {
            unknownLines.getAndIncrement();
        }
    }

    public static void fillSubstationGeoData(Network network, String id, Coordinate coordinate,
                                             boolean forceGeoDataComputation, AtomicInteger substationsWithNewData,
                                             AtomicInteger substationsWithUpdatedData, AtomicInteger unknownSubstations) {
        Substation substation = network.getSubstation(id);
        if (substation != null) {
            SubstationPosition substationPosition = substation.getExtension(SubstationPosition.class);
            if (substationPosition != null) {
                if (forceGeoDataComputation) {
                    substation.removeExtension(SubstationPosition.class);
                    addSubstationPositionExtension(substation, coordinate);
                    substationsWithUpdatedData.getAndIncrement();
                }
            } else {
                addSubstationPositionExtension(substation, coordinate);
                substationsWithNewData.getAndIncrement();
            }
        } else {
            unknownSubstations.getAndIncrement();
        }
    }

    private static void addLinePositionExtension(Line line, List<Coordinate> coordinates) {
        line.newExtension(LinePositionAdder.class)
            .withCoordinates(LineCoordinatesOrdering.order(line, coordinates))
            .add();
    }

    private static void addSubstationPositionExtension(Substation substation, Coordinate coordinates) {
        substation.newExtension(SubstationPositionAdder.class)
            .withCoordinate(coordinates)
            .add();
    }
}
