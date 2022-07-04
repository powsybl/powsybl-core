/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConnectablePositionImpl<C extends Connectable<C>> extends AbstractExtension<C>
        implements ConnectablePosition<C> {

    public static class FeederImpl implements Feeder {

        private String name;

        private Integer order;

        private Direction direction;

        public FeederImpl(String name) {
            this(name, null, null);
        }

        public FeederImpl(String name, int order) {
            this(name, order, null);
        }

        public FeederImpl(String name, Direction direction) {
            this(name, null, direction);
        }

        public FeederImpl(String name, Integer order, Direction direction) {
            this.name = Objects.requireNonNull(name);
            this.order = order;
            this.direction = Objects.requireNonNullElse(direction, Direction.UNDEFINED);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Feeder setName(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        @Override
        public Optional<Integer> getOrder() {
            return Optional.ofNullable(order);
        }

        @Override
        public Feeder setOrder(int order) {
            this.order = order;
            return this;
        }

        @Override
        public Feeder removeOrder() {
            this.order = null;
            return this;
        }

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public Feeder setDirection(Direction direction) {
            this.direction = Objects.requireNonNull(direction);
            return this;
        }
    }

    private FeederImpl feeder;
    private FeederImpl feeder1;
    private FeederImpl feeder2;
    private FeederImpl feeder3;

    public ConnectablePositionImpl(C connectable, FeederImpl feeder, FeederImpl feeder1, FeederImpl feeder2, FeederImpl feeder3) {
        super(connectable);
        ConnectablePosition.check(feeder, feeder1, feeder2, feeder3);
        this.feeder = feeder;
        this.feeder1 = feeder1;
        this.feeder2 = feeder2;
        this.feeder3 = feeder3;
    }

    @Override
    public FeederImpl getFeeder() {
        return feeder;
    }

    @Override
    public FeederImpl getFeeder1() {
        return feeder1;
    }

    @Override
    public FeederImpl getFeeder2() {
        return feeder2;
    }

    @Override
    public FeederImpl getFeeder3() {
        return feeder3;
    }
}
