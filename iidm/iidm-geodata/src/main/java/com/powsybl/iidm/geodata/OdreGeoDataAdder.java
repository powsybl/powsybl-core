/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata;

import com.powsybl.iidm.geodata.dto.LineGeoData;
import com.powsybl.iidm.geodata.dto.SubstationGeoData;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public class OdreGeoDataAdder {

    protected OdreGeoDataAdder() {
    }

    public static void fillNetworkSubstationsGeoData(Network network, List<SubstationGeoData> substationsGeoData) {
        substationsGeoData.forEach(geoData -> {
            Optional<Substation> foundStation = network.getSubstationStream()
                    .filter(substation -> substation.getId().equals(geoData.getId()))
                    .findFirst();
            if (foundStation.isPresent()) {
                Substation station = foundStation.get();
                station.newExtension(SubstationPositionAdder.class)
                        .withCoordinate(geoData.getCoordinate())
                        .add();
            }
        });
    }

    public static void fillNetworkLinesGeoData(Network network, List<LineGeoData> linesGeoData) {
        linesGeoData.forEach(geoData -> {
            Optional<Line> foundLine = network.getLineStream()
                    .filter(line -> line.getId().equals(geoData.getId()))
                    .findFirst();
            if (foundLine.isPresent()) {
                Line line = foundLine.get();
                line.newExtension(LinePositionAdder.class)
                        .withCoordinates(geoData.getCoordinates())
                        .add();
            }
        });
    }

    public static void fillNetworkSubstationsGeoDataFromFile(Network network, Path path) {
        fillNetworkSubstationsGeoData(network, OdreGeoDataCsvLoader.getSubstationsGeoData(path));
    }

    public static void fillNetworkLinesGeoDataFromFiles(Network network, Path aerialLinesFilePath,
                                                        Path undergroundLinesFilePath, Path substationPath) {
        fillNetworkLinesGeoData(network,
                OdreGeoDataCsvLoader.getLinesGeoData(aerialLinesFilePath, undergroundLinesFilePath, substationPath));
    }
}
