/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.TwoWindingsTransformer;

final class BusZ0Calculator implements TopologyVisitor {

    private Bus bus;
    private Z0Bus z0Bus;

    private BusZ0Calculator(Bus bus, Z0Bus z0Bus) {
        this.bus = bus;
        this.z0Bus = z0Bus;

        bus.visitConnectedEquipments(this);
    }

    static Z0Buses calc(Network network) {

        Z0Buses z0Buses = new Z0Buses();
        network.getBusView().getBusStream().forEach(bus -> {
            if (!z0Buses.busInZ0Buses(bus)) {
                Z0Bus z0Bus = calcZ0Bus(bus);
                if (z0Bus.size() > 1) {
                    z0Buses.addZ0Bus(z0Bus);
                }
            }
        });

        return z0Buses;
    }

    private static Z0Bus calcZ0Bus(Bus bus) {

        Z0Bus z0Bus = new Z0Bus();
        z0Bus.addBus(bus);

        int pos = 0;
        while (pos < z0Bus.size()) {
            Bus b = z0Bus.getBus(pos);
            BusZ0Calculator busZ0Calculator = new BusZ0Calculator(b, z0Bus);
            pos++;
        }

        return z0Bus;
    }

    @Override
    public void visitBusbarSection(BusbarSection section) {

    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {

    }

    @Override
    public void visitGenerator(Generator generator) {

    }

    @Override
    public void visitLine(Line line, Side side) {

        Side otherSide = side.equals(Side.ONE) ? Side.TWO : Side.ONE;
        Bus other = line.getTerminal(otherSide).getBusView().getBus();

        if (bus.getV() == other.getV() && bus.getAngle() == other.getAngle()) {
            z0Bus.addBus(other);
        }
    }

    @Override
    public void visitLoad(Load load) {

    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {

    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {

    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
            ThreeWindingsTransformer.Side side) {

    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Side side) {

    }
}
