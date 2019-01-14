/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.network.util.ImmutableNetwork.createUnmodifiableNetworkException;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableTerminal implements Terminal {

    Terminal terminal;

    public ImmutableTerminal(Terminal terminal) {
        this.terminal = Objects.requireNonNull(terminal);
    }

    public static ImmutableTerminal ofNullable(Terminal terminal) {
        return terminal == null ? null : new ImmutableTerminal(terminal);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return ImmutableVoltageLevel.ofNullable(terminal.getVoltageLevel());
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return terminal.getNodeBreakerView(); // NodeBreakerView only contains getter
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
                createUnmodifiableNetworkException();
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
        switch (terminal.getConnectable().getType()) {
            case TWO_WINDINGS_TRANSFORMER:
                return ImmutableTwoWindingsTransformer.ofNullable((TwoWindingsTransformer) terminal.getConnectable());
            case THREE_WINDINGS_TRANSFORMER:
                return ImmutableThreeWindingsTransformer.ofNullable((ThreeWindingsTransformer) terminal.getConnectable());
            default:
                return terminal.getConnectable();
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
        terminal.traverse(traverser);
    }
}
