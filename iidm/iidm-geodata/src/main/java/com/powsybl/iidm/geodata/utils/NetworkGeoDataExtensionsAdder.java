/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import com.powsybl.iidm.geodata.elements.LineGeoData;
import com.powsybl.iidm.geodata.elements.SubstationGeoData;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;

import java.util.Collection;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public final class NetworkGeoDataExtensionsAdder {

    private NetworkGeoDataExtensionsAdder() {
    }

    public static void fillNetworkSubstationsGeoData(Network network, Collection<SubstationGeoData> substationsGeoData) {
        substationsGeoData.forEach(geoData -> {
            Substation foundStation = network.getSubstation(geoData.getId());
            if (foundStation != null) {
                foundStation.newExtension(SubstationPositionAdder.class)
                        .withCoordinate(geoData.getCoordinate())
                        .add();
            }
        });
    }

    public static void fillNetworkLinesGeoData(Network network, Collection<LineGeoData> linesGeoData) {
        linesGeoData.forEach(geoData -> {
            Line foundLine = network.getLine(geoData.id());
            if (foundLine != null) {
                foundLine.newExtension(LinePositionAdder.class)
                        .withCoordinates(geoData.coordinates())
                        .add();
            }
        });
    }

}
