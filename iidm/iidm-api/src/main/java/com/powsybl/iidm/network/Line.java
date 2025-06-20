/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.function.Predicate;

/**
 * An AC line.
 * <p>
 * The equivalent &#960; model used is:
 * <div>
 *    <object data="doc-files/line.svg" type="image/svg+xml"></object>
 * </div>
 * To create a line, see {@link LineAdder}
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see LineAdder
 */
public interface Line extends Branch<Line>, Connectable<Line>, MutableLineCharacteristics<Line> {

    /**
     * @deprecated tie lines are not lines anymore.
     */
    @Deprecated(since = "5.2.0")
    default boolean isTieLine() {
        return false;
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.LINE;
    }

    boolean connect(boolean propagateDisconnectionIfNeeded);

    default boolean connect(Predicate<Switch> isTypeSwitchToOperate, boolean propagateDisconnectionIfNeeded) {
        return connect(isTypeSwitchToOperate, null, propagateDisconnectionIfNeeded);
    }

    boolean connect(Predicate<Switch> isTypeSwitchToOperate, ThreeSides side, boolean propagateDisconnectionIfNeeded);

    boolean disconnect(boolean propagateDisconnectionIfNeeded);

    default boolean disconnect(Predicate<Switch> isSwitchOpenable, boolean propagateDisconnectionIfNeeded) {
        return disconnect(isSwitchOpenable, null, propagateDisconnectionIfNeeded);
    }

    boolean disconnect(Predicate<Switch> isSwitchOpenable, ThreeSides side, boolean propagateDisconnectionIfNeeded);
}
