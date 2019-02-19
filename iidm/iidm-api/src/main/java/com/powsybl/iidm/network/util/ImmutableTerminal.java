/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableTerminal implements Terminal {

    private final ImmutableCacheIndex cache;

    private Terminal terminal;

    ImmutableTerminal(Terminal terminal, ImmutableCacheIndex cache) {
        this.terminal = Objects.requireNonNull(terminal);
        this.cache = Objects.requireNonNull(cache);
    }

    Terminal getTerminal() {
        return terminal;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return cache.getVoltageLevel(terminal.getVoltageLevel());
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
                return cache.getBus(terminal.getBusBreakerView().getBus());
            }

            @Override
            public Bus getConnectableBus() {
                return cache.getBus(terminal.getBusBreakerView().getConnectableBus());
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
                return cache.getBus(terminal.getBusView().getBus());
            }

            @Override
            public Bus getConnectableBus() {
                return cache.getBus(terminal.getBusView().getConnectableBus());
            }
        };
    }

    @Override
    public Connectable getConnectable() {
        Connectable connectable = terminal.getConnectable();
        return cache.getConnectable(connectable);
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
