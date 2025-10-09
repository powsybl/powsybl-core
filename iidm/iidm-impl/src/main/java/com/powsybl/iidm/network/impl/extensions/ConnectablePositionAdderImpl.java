/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
public class ConnectablePositionAdderImpl<C extends Connectable<C>>
        extends AbstractExtensionAdder<C, ConnectablePosition<C>>
        implements ConnectablePositionAdder<C> {

    private ConnectablePositionImpl.FeederImpl feeder;
    private ConnectablePositionImpl.FeederImpl feeder1;
    private ConnectablePositionImpl.FeederImpl feeder2;
    private ConnectablePositionImpl.FeederImpl feeder3;

    ConnectablePositionAdderImpl(C connectable) {
        super(connectable);
    }

    private abstract static class AbstractFeederImplAdder<C extends Connectable<C>> implements FeederAdder<C> {
        protected String name;

        protected Integer order;

        protected ConnectablePosition.Direction direction;

        public FeederAdder<C> withName(String name) {
            this.name = name;
            return this;
        }

        public FeederAdder<C> withOrder(int order) {
            this.order = order;
            return this;
        }

        public FeederAdder<C> withDirection(ConnectablePosition.Direction direction) {
            this.direction = direction;
            return this;
        }

    }

    @Override
    public ConnectablePositionImpl<C> createExtension(C extendable) {
        return new ConnectablePositionImpl<>(extendable, feeder, feeder1, feeder2, feeder3);
    }

    @Override
    public FeederAdder<C> newFeeder() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder = new ConnectablePositionImpl.FeederImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public FeederAdder<C> newFeeder1() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder1 = new ConnectablePositionImpl.FeederImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public FeederAdder<C> newFeeder2() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder2 = new ConnectablePositionImpl.FeederImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public FeederAdder<C> newFeeder3() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder3 = new ConnectablePositionImpl.FeederImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

}
