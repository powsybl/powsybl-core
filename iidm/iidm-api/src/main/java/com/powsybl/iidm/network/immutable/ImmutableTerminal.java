/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

/**
 * An immutable {@link Terminal}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableTerminal implements Terminal {

    private final Terminal terminal;

    private final ImmutableCacheIndex cache;

    ImmutableTerminal(Terminal terminal, ImmutableCacheIndex cache) {
        this.terminal = Objects.requireNonNull(terminal);
        this.cache = Objects.requireNonNull(cache);
    }

    /**
     * Returns the mutable terminal
     * @return the mutable terminal which is wrapped
     */
    Terminal getTerminal() {
        return terminal;
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableVoltageLevel}
     */
    @Override
    public VoltageLevel getVoltageLevel() {
        return cache.getVoltageLevel(terminal.getVoltageLevel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBreakerView getNodeBreakerView() {
        return terminal.getNodeBreakerView(); // this nbv contains only primitive return type getter
    }

    /**
     * {@inheritDoc}
     * @return Returns an immutable {@link com.powsybl.iidm.network.Terminal.BusBreakerView}
     */
    @Override
    public BusBreakerView getBusBreakerView() {
        return new BusBreakerView() {
            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getBus() {
                return cache.getBus(terminal.getBusBreakerView().getBus());
            }

            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getConnectableBus() {
                return cache.getBus(terminal.getBusBreakerView().getConnectableBus());
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public void setConnectableBus(String busId) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }
        };
    }

    /**
     * {@inheritDoc}
     * @return Returns an immutable {@link com.powsybl.iidm.network.Terminal.BusView}
     */
    @Override
    public BusView getBusView() {
        return new BusView() {
            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getBus() {
                return cache.getBus(terminal.getBusView().getBus());
            }

            /**
             * {@inheritDoc}
             * @return an immutable {@link Bus}
             */
            @Override
            public Bus getConnectableBus() {
                return cache.getBus(terminal.getBusView().getConnectableBus());
            }
        };
    }

    /**
     * {@inheritDoc}
     * @return Returns an immutable {@link Connectable}
     */
    @Override
    public Connectable getConnectable() {
        Connectable connectable = terminal.getConnectable();
        return cache.getConnectable(connectable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getP() {
        return terminal.getP();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Terminal setP(double p) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getQ() {
        return terminal.getQ();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Terminal setQ(double q) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getI() {
        return terminal.getI();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public boolean connect() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public boolean disconnect() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return terminal.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        terminal.traverse(traverser);
    }

}
