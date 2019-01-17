/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableTerminal implements Terminal {

    Terminal terminal;

    ImmutableTerminal(Terminal terminal) {
        this.terminal = Objects.requireNonNull(terminal);
    }

    static ImmutableTerminal ofNullable(Terminal terminal) {
        return terminal == null ? null : new ImmutableTerminal(terminal);
    }

    Terminal getTerminal() {
        return terminal;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return ImmutableVoltageLevel.ofNullable(terminal.getVoltageLevel());
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return terminal.getNodeBreakerView(); // this nbv contains only primitive return type getter
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return new BusBreakerView() {
            @Override
            public Bus getBus() {
                return ImmutableBus.ofNullable(terminal.getBusBreakerView().getBus());
            }

            @Override
            public Bus getConnectableBus() {
                return ImmutableBus.ofNullable(terminal.getBusBreakerView().getConnectableBus());
            }

            @Override
            public void setConnectableBus(String busId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }
        };
    }

    @Override
    public BusView getBusView() {
        return new BusView() {
            @Override
            public Bus getBus() {
                return ImmutableBus.ofNullable(terminal.getBusView().getBus());
            }

            @Override
            public Bus getConnectableBus() {
                return ImmutableBus.ofNullable(terminal.getBusView().getConnectableBus());
            }
        };
    }

    @Override
    public Connectable getConnectable() {
        Connectable connectable = terminal.getConnectable();
        switch (connectable.getType()) {
            case BUSBAR_SECTION:
                return connectable;
            case LINE:
                return ImmutableFactory.ofNullableLine((Line) connectable);
            case TWO_WINDINGS_TRANSFORMER:
                return ImmutableTwoWindingsTransformer.ofNullable((TwoWindingsTransformer) connectable);
            case THREE_WINDINGS_TRANSFORMER:
                return ImmutableThreeWindingsTransformer.ofNullable((ThreeWindingsTransformer) connectable);
            case GENERATOR:
                return ImmutableGenerator.ofNullable((Generator) connectable);
            case LOAD:
                return ImmutableLoad.ofNullable((Load) connectable);
            case SHUNT_COMPENSATOR:
                return ImmutableShuntCompensator.ofNullable((ShuntCompensator) connectable);
            case DANGLING_LINE:
                return ImmutableDanglingLine.ofNullable((DanglingLine) connectable);
            case STATIC_VAR_COMPENSATOR:
                return ImmutableStaticVarCompensator.ofNullable((StaticVarCompensator) connectable);
            case HVDC_CONVERTER_STATION:
                return ImmutableFactory.ofNullableHvdcConverterStation((HvdcConverterStation) connectable);
            default:
                throw new IllegalArgumentException(connectable.getType().name() + " is not valid to be immutablized");
        }
    }

    @Override
    public double getP() {
        return terminal.getP();
    }

    @Override
    public Terminal setP(double p) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getQ() {
        return terminal.getQ();
    }

    @Override
    public Terminal setQ(double q) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getI() {
        return terminal.getI();
    }

    @Override
    public boolean connect() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean disconnect() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean isConnected() {
        return terminal.isConnected();
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        // TO REVIEW
        terminal.traverse(traverser);
    }

}
