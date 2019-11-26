/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import org.apache.commons.lang3.NotImplementedException;

import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
interface BusExt extends Bus {

    /**
     * Get terminals connected to this bus.
     */
    Iterable<TerminalExt> getConnectedTerminals();

    /**
     * Get terminals connected to this bus.
     */
    Stream<TerminalExt> getConnectedTerminalStream();

    /**
     * Get nodes in this bus.
     * @return Returns the nodes in array.
     */
    default int[] getNodes() {
        throw new NotImplementedException("Not implemented yet");
    }

    void setConnectedComponentNumber(int connectedComponentNumber);

    void setSynchronousComponentNumber(int componentNumber);

}
