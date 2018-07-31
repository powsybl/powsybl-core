/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.stream.Stream;

/**
 * A set of connected bus in the network.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Component {

    /**
     * Get the number of the component.
     * <p>
     * The biggest one has the number zero and the smallest has the highest number.
     */
    int getNum();

    /**
     * Get the number of bus in the component.
     */
    int getSize();

    /**
     * Get buses in the component.
     */
    Iterable<Bus> getBuses();

    /**
     * Get buses in the component.
     */
    Stream<Bus> getBusStream();

}
