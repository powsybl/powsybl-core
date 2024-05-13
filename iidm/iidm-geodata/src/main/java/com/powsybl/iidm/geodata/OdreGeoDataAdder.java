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

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public class OdreGeoDataAdder {

    protected OdreGeoDataAdder() {
    }

    public static void fillNetworkSubstationsGeoData(Network network, List<SubstationGeoData> substationsGeoData) {
        substationsGeoData.forEach(geoData -> {
            Substation foundStation = network.getSubstation(geoData.getId());
            if (foundStation != null) {
                foundStation.newExtension(SubstationPositionAdder.class)
                        .withCoordinate(geoData.getCoordinate())
                        .add();
            }
        });
    }

    public static void fillNetworkLinesGeoData(Network network, List<LineGeoData> linesGeoData) {
        linesGeoData.forEach(geoData -> {
            Line foundLine = network.getLine(geoData.getId());
            if (foundLine != null) {
                foundLine.newExtension(LinePositionAdder.class)
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
