/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
final class GLTestUtils {

    static final Coordinate SUBSTATION_1 = new Coordinate(51.380348205566406, 0.5492960214614868);
    static final Coordinate SUBSTATION_2 = new Coordinate(52.00010299682617, 0.30759671330451965);
    static final Coordinate LINE_1 = new Coordinate(51.529258728027344, 0.5132722854614258);
    static final Coordinate LINE_2 = new Coordinate(51.944923400878906, 0.4120868146419525);

    private GLTestUtils() {
    }

    static Network getNetwork() {
        Network network = Network.create("Network", "test");
        network.setCaseDate(ZonedDateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation1 = network.newSubstation()
                .setId("Substation1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        network.newLine()
                .setId("Line")
                .setVoltageLevel1(voltageLevel1.getId())
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setVoltageLevel2(voltageLevel2.getId())
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        return network;
    }

    static void checkNetwork(Network network) {
        Substation substation1 = network.getSubstation("Substation1");
        SubstationPosition substation1Position = substation1.getExtension(SubstationPosition.class);
        assertEquals(SUBSTATION_1.getLatitude(), substation1Position.getCoordinate().getLatitude(), 0);
        assertEquals(SUBSTATION_1.getLongitude(), substation1Position.getCoordinate().getLongitude(), 0);

        Substation substation2 = network.getSubstation("Substation2");
        SubstationPosition substation2Position = substation2.getExtension(SubstationPosition.class);
        assertEquals(SUBSTATION_2.getLatitude(), substation2Position.getCoordinate().getLatitude(), 0);
        assertEquals(SUBSTATION_2.getLongitude(), substation2Position.getCoordinate().getLongitude(), 0);

        Line line = network.getLine("Line");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertEquals(SUBSTATION_1.getLatitude(), linePosition.getCoordinates().get(0).getLatitude(), 0);
        assertEquals(SUBSTATION_1.getLongitude(), linePosition.getCoordinates().get(0).getLongitude(), 0);

        assertEquals(LINE_1.getLatitude(), linePosition.getCoordinates().get(1).getLatitude(), 0);
        assertEquals(LINE_1.getLongitude(), linePosition.getCoordinates().get(1).getLongitude(), 0);

        assertEquals(LINE_2.getLatitude(), linePosition.getCoordinates().get(2).getLatitude(), 0);
        assertEquals(LINE_2.getLongitude(), linePosition.getCoordinates().get(2).getLongitude(), 0);

        assertEquals(SUBSTATION_2.getLatitude(), linePosition.getCoordinates().get(3).getLatitude(), 0);
        assertEquals(SUBSTATION_2.getLongitude(), linePosition.getCoordinates().get(3).getLongitude(), 0);
    }
}
