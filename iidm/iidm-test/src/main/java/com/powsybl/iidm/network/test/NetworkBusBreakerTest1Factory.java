/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class NetworkBusBreakerTest1Factory {

    private NetworkBusBreakerTest1Factory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault(), null);
    }

    public static Network create(String networkId) {
        return create(NetworkFactory.findDefault(), networkId);
    }

    public static Network create(NetworkFactory networkFactory, String nid) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork(id("network", nid), "test");
        Substation substation1 = network.newSubstation()
                .setId(id("substation1", nid))
                .setCountry(Country.FR)
                .setTso(id("TSO1", nid))
                .setGeographicalTags(id("region1", nid))
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId(id("voltageLevel1", nid))
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel.BusBreakerView topology1 = voltageLevel1.getBusBreakerView();
        Bus voltageLevel1Bus1 = topology1.newBus()
                .setId(id("voltageLevel1Bus1", nid))
                .add();
        Bus voltageLevel1Bus2 = topology1.newBus()
                .setId(id("voltageLevel1Bus2", nid))
                .add();
        topology1.newSwitch()
                .setId(id("voltageLevel1Breaker1", nid))
                .setOpen(false)
                .setBus1(voltageLevel1Bus1.getId())
                .setBus2(voltageLevel1Bus2.getId())
                .add();
        Load load1 = voltageLevel1.newLoad()
                .setId(id("load1", nid))
                .setBus(voltageLevel1Bus1.getId())
                .setP0(10)
                .setQ0(3)
                .add();
        load1.setP0(10);
        Generator generator1 = voltageLevel1.newGenerator()
                .setId(id("generator1", nid))
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(200.0)
                .setMaxP(900.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(900.0)
                .setTargetV(380.0)
                .setBus(voltageLevel1Bus2.getId())
                .add();
        generator1.newReactiveCapabilityCurve()
                .beginPoint().setP(200.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
                .beginPoint().setP(900.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
                .add();
        return network;
    }

    public static String id(String localId, String networkId) {
        return networkId != null ? "n" + networkId + "_" + localId : localId;
    }
}
