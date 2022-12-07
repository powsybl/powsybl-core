/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;

import java.util.Optional;

/**
 * This class gives the position of a connectable relative to other equipments in the network.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ConnectablePosition<C extends Connectable<C>> extends Extension<C> {

    String NAME = "position";

    public enum Direction {
        TOP,
        BOTTOM,
        UNDEFINED
    }

    @Override
    default String getName() {
        return NAME;
    }

    public interface Feeder {
        Optional<String> getName();

        Feeder setName(String name);

        Optional<Integer> getOrder();

        Feeder setOrder(int order);

        Feeder removeOrder();

        Direction getDirection();

        Feeder setDirection(Direction direction);
    }

    /**
     * Feeder in case the connectable has only one (injections)
     */
    public Feeder getFeeder();

    /**
     * First feeder in case the connectable has two or three
     */
    public Feeder getFeeder1();

    /**
     * Second feeder in case the connectable has two or three
     */
    public Feeder getFeeder2();

    /**
     * Third feeder in case the connectable has three (three windings transformers)
     */
    public Feeder getFeeder3();

    public static void check(Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        if (feeder == null && feeder1 == null && feeder2 == null && feeder3 == null) {
            throw new IllegalArgumentException("invalid feeder");
        }
        if (feeder != null && (feeder1 != null || feeder2 != null || feeder3 != null)) {
            throw new IllegalArgumentException("feeder and feeder 1|2|3 are exclusives");
        }
    }

}
