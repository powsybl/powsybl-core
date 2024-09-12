/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import com.google.common.collect.Lists;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class LineCoordinatesOrdering {

    private static final Logger LOGGER = LoggerFactory.getLogger(LineCoordinatesOrdering.class);

    private LineCoordinatesOrdering() {

    }

    public static List<Coordinate> order(Line line, List<Coordinate> lineCoordinates) {

        Optional<Coordinate> s1 = getCoordinate(line, TwoSides.ONE);
        Optional<Coordinate> s2 = getCoordinate(line, TwoSides.TWO);
        Coordinate firstCoordinate = lineCoordinates.get(0);
        Coordinate lastCoordinate = lineCoordinates.get(lineCoordinates.size() - 1);

        if (s1.isPresent()) {
            if (s2.isPresent()) {
                return orderCoordinates(s1.get(), s2.get(), firstCoordinate, lastCoordinate, lineCoordinates, line.getId());
            } else {
                return orderCoordinates(s1.get(), firstCoordinate, lastCoordinate, lineCoordinates);
            }
        } else {
            if (s2.isPresent()) {
                return orderCoordinates(s2.get(), lastCoordinate, firstCoordinate, lineCoordinates);
            } else {
                return lineCoordinates; // no information on substations positions -> considering it's in the right order
            }
        }
    }

    private static List<Coordinate> orderCoordinates(Coordinate s1, Coordinate s2, Coordinate firstCoordinate, Coordinate lastCoordinate,
                                                     List<Coordinate> lineCoordinates, String lineId) {
        final double substation1lineStart = DistanceCalculator.distance(s1, firstCoordinate);
        final double substation2lineStart = DistanceCalculator.distance(s2, firstCoordinate);
        final double substation1lineEnd = DistanceCalculator.distance(s1, lastCoordinate);
        final double substation2lineEnd = DistanceCalculator.distance(s2, lastCoordinate);
        if ((substation1lineStart < substation2lineStart) == (substation1lineEnd < substation2lineEnd)) {
            LOGGER.warn("line {} has both first and last coordinate nearest to {}",
                    lineId, substation1lineStart < substation2lineStart ? TwoSides.ONE : TwoSides.TWO);
            // reverse the list if line end closer to substation1 than line start
            return substation1lineStart <= substation1lineEnd ? lineCoordinates : Lists.reverse(lineCoordinates);
        }
        // main case: the line start is closer to one substation, the line end is closer to the other substation
        // reverse the list if the start is closer to the second substation
        return substation1lineStart <= substation2lineStart ? lineCoordinates : Lists.reverse(lineCoordinates);
    }

    private static List<Coordinate> orderCoordinates(Coordinate substation, Coordinate coordA, Coordinate coordB, List<Coordinate> coordinates) {
        if (DistanceCalculator.distance(substation, coordA) > DistanceCalculator.distance(substation, coordB)) {
            return Lists.reverse(coordinates);
        } else {
            return coordinates;
        }
    }

    private static Optional<Coordinate> getCoordinate(Line line, TwoSides side) {
        return line.getTerminal(side).getVoltageLevel().getSubstation()
                .map(s -> (SubstationPosition) s.getExtension(SubstationPosition.class))
                .map(SubstationPosition::getCoordinate);
    }
}
