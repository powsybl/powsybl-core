/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.geodata.elements.SubstationGeoData;
import com.powsybl.iidm.geodata.utils.InputUtils;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;

import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillNetworkLinesGeoData;
import static com.powsybl.iidm.geodata.utils.NetworkGeoDataExtensionsAdder.fillNetworkSubstationsGeoData;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public class OdreGeoDataAdder {

    protected OdreGeoDataAdder() {
    }

    public static void fillNetworkSubstationsGeoDataFromFile(Network network, Path path, OdreConfig odreConfig) throws IOException {
        fillNetworkSubstationsGeoData(network, OdreGeoDataCsvLoader.getSubstationsGeoData(path, odreConfig));
    }

    public static void fillNetworkGeoDataFromFiles(Network network, Path aerialLinesFilePath,
                                                   Path undergroundLinesFilePath, Path substationPath, OdreConfig odreConfig) throws IOException {
        try (Reader substationReader = InputUtils.toReader(substationPath)) {
            Map<String, SubstationGeoData> substations = GeographicDataParser.parseSubstations(substationReader, odreConfig);
            fillNetworkSubstationsGeoData(network, substations.values());
            fillNetworkLinesGeoData(network,
                    OdreGeoDataCsvLoader.getLinesGeoData(aerialLinesFilePath, undergroundLinesFilePath, substations, odreConfig));
        }
    }
}
