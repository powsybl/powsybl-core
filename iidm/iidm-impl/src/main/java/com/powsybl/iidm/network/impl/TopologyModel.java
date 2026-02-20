/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Switch;

import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
interface TopologyModel {

    void invalidateCache(boolean exceptBusBreakerView);

    void invalidateCache();

    void attach(TerminalExt terminal, boolean test);

    void detach(TerminalExt terminal);

    boolean connect(TerminalExt terminal, Predicate<Switch> isTypeSwitchToOperate);

    boolean disconnect(TerminalExt terminal, Predicate<Switch> isSwitchOpenable);
}
