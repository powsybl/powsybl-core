/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import java.time.ZonedDateTime;

import java.util.Objects;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public final class BatteryNetworkFactory {

    private static final String VLGEN = "VLGEN";
    private static final String VLBAT = "VLBAT";

    private BatteryNetworkFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("fictitious", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        // 2 Substations
        Substation p1 = network.newSubstation()
                .setId("P1")
                .setCountry(Country.FR)
                .setTso("R")
                .setGeographicalTags("A")
                .add();
        Substation p2 = network.newSubstation()
                .setId("P2")
                .setCountry(Country.FR)
                .setTso("R")
                .setGeographicalTags("B")
                .add();

        // 2 VoltageLevels
        VoltageLevel vlgen = p1.newVoltageLevel()
                .setId(VLGEN)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlbat = p2.newVoltageLevel()
                .setId(VLBAT)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        // 2 Bus
        Bus ngen = vlgen.getBusBreakerView().newBus()
                .setId("NGEN")
                .add();
        Bus nbat = vlbat.getBusBreakerView().newBus()
                .setId("NBAT")
                .add();

        // 2 lines
        network.newLine()
                .setId("NHV1_NHV2_1")
                .setVoltageLevel1(vlgen.getId())
                .setBus1(ngen.getId())
                .setConnectableBus1(ngen.getId())
                .setVoltageLevel2(vlbat.getId())
                .setBus2(nbat.getId())
                .setConnectableBus2(nbat.getId())
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        network.newLine()
                .setId("NHV1_NHV2_2")
                .setVoltageLevel1(vlgen.getId())
                .setBus1(ngen.getId())
                .setConnectableBus1(ngen.getId())
                .setVoltageLevel2(vlbat.getId())
                .setBus2(nbat.getId())
                .setConnectableBus2(nbat.getId())
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();

        // Add Components
        Generator generator = vlgen.newGenerator()
                .setId("GEN")
                .setBus(ngen.getId())
                .setConnectableBus(ngen.getId())
                .setEnergySource(EnergySource.OTHER)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();
        generator.getTerminal().setP(-605);
        generator.getTerminal().setQ(-225);
        generator.newMinMaxReactiveLimits()
                .setMinQ(-9999.99)
                .setMaxQ(9999.99)
                .add();
        Battery battery = vlbat.newBattery()
                .setId("BAT")
                .setBus(nbat.getId())
                .setConnectableBus(nbat.getId())
                .setTargetP(9999.99)
                .setTargetQ(9999.99)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .add();
        battery.newMinMaxReactiveLimits()
                .setMinQ(-9999.99)
                .setMaxQ(9999.99)
                .add();
        battery.getTerminal().setP(-605);
        battery.getTerminal().setQ(-225);
        Battery battery2 = vlbat.newBattery()
                .setId("BAT2")
                .setBus(nbat.getId())
                .setConnectableBus(nbat.getId())
                .setTargetP(100)
                .setTargetQ(200)
                .setMinP(-200)
                .setMaxP(200)
                .add();
        battery2.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0)
                .setMinQ(-59.3)
                .setMaxQ(60.0)
                .endPoint()
                .beginPoint()
                .setP(70.0)
                .setMinQ(-54.55)
                .setMaxQ(46.25)
                .endPoint()
                .add();
        battery2.getTerminal().setP(-605);
        battery2.getTerminal().setQ(-225);
        vlbat.newLoad()
                .setId("LOAD")
                .setLoadType(LoadType.UNDEFINED)
                .setBus(nbat.getId())
                .setConnectableBus(nbat.getId())
                .setP0(600.0)
                .setQ0(200.0)
                .add();

        return network;
    }
}
