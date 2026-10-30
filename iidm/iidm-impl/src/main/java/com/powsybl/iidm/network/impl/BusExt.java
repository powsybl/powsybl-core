/**
 * Copyright (c) 2016-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;

import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
interface BusExt extends Bus {

    @Override
    Iterable<TerminalExt> getConnectedTerminals();

    @Override
    Stream<TerminalExt> getConnectedTerminalStream();

    void setConnectedComponentNumber(int connectedComponentNumber);

    void setSynchronousComponentNumber(int componentNumber);

    int getQuickConnectedComponentNumber();

    int getQuickSynchronousComponentNumber();
}
