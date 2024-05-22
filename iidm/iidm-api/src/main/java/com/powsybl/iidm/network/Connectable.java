/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An equipment that is part of a substation topology.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Connectable<I extends Connectable<I>> extends Identifiable<I> {

    List<? extends Terminal> getTerminals();

    /**
     * Remove the connectable from the voltage level (dangling switches are kept).
     */
    void remove();

    boolean connect();

    boolean connect(Predicate<Switch> isTypeSwitchToOperate);

    boolean connect(Predicate<Switch> isTypeSwitchToOperate, Optional<ThreeSides> side);

    boolean disconnect();

    boolean disconnect(Predicate<Switch> isSwitchOpenable);

    boolean disconnect(Predicate<Switch> isSwitchOpenable, Optional<ThreeSides> side);
}
