/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;

import java.util.Optional;

/**
 * This class gives some information for visualization tools, for a given connectable:
 * <ul>
 *     <li>its position relative to other equipments in its voltage level(s),</li>
 *     <li>its direction relative to the corresponding busbar section(s) (top or bottom - as the busbar section is usually displayed horizontally - or undefined),</li>
 *     <li>its display name(s).</li>
 * </ul>
 * This gives visualization tools suggestions for displaying the equipments within a voltage level.
 * The information is given through one, two or three {@link ConnectablePosition.Feeder} objects, as the connectable
 * might be between two or three busbar sections (which may or may not be in the same voltage level).
 * <p>
 * Note that, when this class is used in conjunction with {@link com.powsybl.iidm.network.extensions.BusbarSectionPosition},
 * the connectable positions should be in ascending order for ascending busbar section indices.
 * That is, the connectable positions should be in ascending order on a given physical busbar.
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ConnectablePosition<C extends Connectable<C>> extends Extension<C> {

    String NAME = "position";

    enum Direction {
        TOP,
        BOTTOM,
        UNDEFINED
    }

    @Override
    default String getName() {
        return NAME;
    }

    interface Feeder {
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
    Feeder getFeeder();

    /**
     * First feeder in case the connectable has two or three
     */
    Feeder getFeeder1();

    /**
     * Second feeder in case the connectable has two or three
     */
    Feeder getFeeder2();

    /**
     * Third feeder in case the connectable has three (three windings transformers)
     */
    Feeder getFeeder3();

    static void check(Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        if (feeder == null && feeder1 == null && feeder2 == null && feeder3 == null) {
            throw new IllegalArgumentException("invalid feeder");
        }
        if (feeder != null && (feeder1 != null || feeder2 != null || feeder3 != null)) {
            throw new IllegalArgumentException("feeder and feeder 1|2|3 are exclusives");
        }
    }

}
